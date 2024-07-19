// MainActivity.java

package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private Button btnSpeak, btnListen, btnCreateProfile, btnTrackMe, btnSOS, btnHelp;
    private EditText etInput;
    private TextView tvLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private final ActivityResultLauncher<Intent> profileActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Handle the result from ProfileActivity
                    Toast.makeText(MainActivity.this, "Profile created successfully", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> requestSmsPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean sendSmsGranted = result.containsKey(Manifest.permission.SEND_SMS) && result.get(Manifest.permission.SEND_SMS);
                Boolean receiveSmsGranted = result.containsKey(Manifest.permission.RECEIVE_SMS) && result.get(Manifest.permission.RECEIVE_SMS);
                Boolean readSmsGranted = result.containsKey(Manifest.permission.READ_SMS) && result.get(Manifest.permission.READ_SMS);

                if (sendSmsGranted != null && receiveSmsGranted != null && readSmsGranted != null) {
                    if (sendSmsGranted && receiveSmsGranted && readSmsGranted) {
                        Toast.makeText(this, "SMS permissions granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "SMS permissions are required to send alerts", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "Location permission is needed for real-time tracking and emergency alerts", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Edge-to-Edge display setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        btnSpeak = findViewById(R.id.btnSpeak);
        btnListen = findViewById(R.id.btnListen);
        etInput = findViewById(R.id.etInput);
        btnCreateProfile = findViewById(R.id.btnCreateProfile);
        tvLocation = findViewById(R.id.tvLocation);
        btnTrackMe = findViewById(R.id.btnTrackMe);
        btnSOS = findViewById(R.id.btnSOS);
        btnHelp = findViewById(R.id.btnHelp);

        // Initialize SpeechRecognizer and TextToSpeech
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new SpeechRecognitionListener());

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
            }
        });

        // Set up location client and callback
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationUI(location);
                }
            }
        };

        // Request SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            // Request SMS permissions
            requestSmsPermissionsLauncher.launch(new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
            });
        }

        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            startLocationUpdates();
        }

        // Set up button click listeners
        btnSpeak.setOnClickListener(v -> speakText(etInput.getText().toString()));
        btnListen.setOnClickListener(v -> listenToSpeech());
        btnCreateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            profileActivityLauncher.launch(intent);
        });
        btnTrackMe.setOnClickListener(v -> shareLocation());
        btnSOS.setOnClickListener(v -> sendSOSAlert());
        btnHelp.setOnClickListener(v -> showHelpContacts());
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // Update interval in milliseconds
        locationRequest.setFastestInterval(5000); // Fastest update interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateLocationUI(Location location) {
        String locationText = "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude();
        tvLocation.setText(locationText);
    }

    private void shareLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            // Example emergency contact number
            String emergencyContact = "1234567890";
            String message = "I am here: " + tvLocation.getText().toString();

            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + emergencyContact));
            smsIntent.putExtra("sms_body", message);
            if (smsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(smsIntent);
            } else {
                Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "SMS permissions are required to share location", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSOSAlert() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            // Example emergency contact number
            String emergencyContact = "1234567890";
            String message = "SOS! I need help. My current location is: " + tvLocation.getText().toString();

            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + emergencyContact));
            smsIntent.putExtra("sms_body", message);
            if (smsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(smsIntent);
            } else {
                Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "SMS permissions are required to send SOS alert", Toast.LENGTH_SHORT).show();
        }
    }

    private void showHelpContacts() {
        String[] helpContacts = {
                "Police: 10111",
                "Ambulance: 10177",
                "Fire: 112",
                "Women's Helpline: 181"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help Contacts")
                .setItems(helpContacts, (dialog, which) -> {
                    // Handle click on a help contact
                    String contact = helpContacts[which];
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + contact.split(": ")[1]));
                    if (dialIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(dialIntent);
                    } else {
                        Toast.makeText(this, "No dialer app found", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }

    private void speakText(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void listenToSpeech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
        speechRecognizer.startListening(intent);
    }

    private class SpeechRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {}

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            Toast.makeText(MainActivity.this, "Recognition error: " + error, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                etInput.setText(matches.get(0));
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }
}
