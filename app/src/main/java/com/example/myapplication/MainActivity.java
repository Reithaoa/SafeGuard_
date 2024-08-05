package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private Button btnSpeak, btnListen, btnCreateProfile, btnTrackMe, btnSOS, btnHelp;
    private EditText etInput;
    private TextView tvLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private MapView mapView;
    private GoogleMap gMap;
    private Spinner spinnerMenu;
    private TextView tvUserName;



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

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        Spinner spinnerMenu = findViewById(R.id.spinnerMenu);
        List<String> menuOptions = Arrays.asList(getResources().getStringArray(R.array.menu_options));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.menu_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMenu.setAdapter(adapter);



        // Edge-to-Edge display setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        btnSpeak = findViewById(R.id.btnSpeak);
        btnListen = findViewById(R.id.btnListen);
        etInput = findViewById(R.id.etInput);
        tvLocation = findViewById(R.id.tvLocation);
        btnCreateProfile = findViewById(R.id.btnCreateProfile);
        btnTrackMe = findViewById(R.id.btnTrackMe);
        btnSOS = findViewById(R.id.btnSOS);
        btnHelp = findViewById(R.id.btnHelp);

        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button btnSOS = findViewById(R.id.btnSOS);
        btnSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationAndSend();
            }
        });

        // Initialize Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                Toast.makeText(MainActivity.this, "Speech recognition error: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    etInput.setText(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        // Button listeners
        btnSpeak.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            if (intent.resolveActivity(getPackageManager()) != null) {
                speechRecognizer.startListening(intent);
            } else {
                Toast.makeText(MainActivity.this, "Speech recognition is not supported on your device.", Toast.LENGTH_SHORT).show();
            }
        });

        btnListen.setOnClickListener(v -> {
            String text = etInput.getText().toString();
            if (!text.isEmpty()) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                Toast.makeText(MainActivity.this, "Please enter text to listen.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            profileActivityLauncher.launch(intent);
        });

        btnTrackMe.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        btnSOS.setOnClickListener(v -> {
            shareLocation();
        });

        btnHelp.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Help");
            builder.setMessage("This is a safety app to help you share your location in case of emergencies.\n\n" +
                    "1. Speak: Convert your speech to text.\n" +
                    "2. Listen: Convert text to speech.\n" +
                    "3. Create Profile: Create your profile and add emergency contacts.\n" +
                    "4. Track Me: Start location updates to get your real-time location.\n" +
                    "5. SOS: Send your current location to your emergency contacts.\n" +
                    "6. Help: Show this help message.");
            builder.setPositiveButton("OK", null);
            builder.show();
        });

        // Request necessary permissions
        requestSmsPermissionsLauncher.launch(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS});

        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        updateLocation(location);
                    }
                }
            }
        };
    }

    private void getLocationAndSend() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    sendLocationToContacts(location);
                } else {
                    Toast.makeText(MainActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void sendLocationToContacts(Location location) {
        String message = "Emergency! I'm at: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();

        List<String> emergencyContacts = getEmergencyContacts();  // Implement this method to get stored contacts

        for (String contact : emergencyContacts) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(contact, null, message, null, null);
        }

        Toast.makeText(this, "Location sent to emergency contacts!", Toast.LENGTH_SHORT).show();
    }
    private void saveEmergencyContact(String contact) {
        SharedPreferences sharedPreferences = getSharedPreferences("EmergencyContacts", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("contact1", contact);
        editor.apply();
    }

    private List<String> getEmergencyContacts() {
        SharedPreferences sharedPreferences = getSharedPreferences("EmergencyContacts", MODE_PRIVATE);
        List<String> contacts = new ArrayList<>();
        contacts.add(sharedPreferences.getString("contact1", null));
        return contacts;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndSend();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }




    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setZoomControlsEnabled(true);

        // Check for location permission and get the current location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            updateLocation(location);
                            gMap.setMyLocationEnabled(true);
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            gMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        }
                    });
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocation(Location location) {
        if (location != null) {
            String locationText = "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude();
            tvLocation.setText(locationText);

            if (gMap != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                gMap.clear();
                gMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void shareLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            List<String> emergencyContacts = getEmergencyContacts();
            if (emergencyContacts.isEmpty()) {
                Toast.makeText(this, "No emergency contacts found", Toast.LENGTH_SHORT).show();
                return;
            }

            String message = "I am here: " + tvLocation.getText().toString();

            for (String contact : emergencyContacts) {
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                smsIntent.setData(Uri.parse("smsto:" + contact));
                smsIntent.putExtra("sms_body", message);
                if (smsIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(smsIntent);
                } else {
                    Toast.makeText(this, "No SMS app found for contact: " + contact, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "SMS permissions are required to share location", Toast.LENGTH_SHORT).show();
        }
    }

//    private List<String> getEmergencyContacts() {
//        List<String> contacts = new ArrayList<>();
//        contacts.add("0833381053");  // Example contact
//        contacts.add("0678121390");  // Example contact
//        return contacts;
//    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
