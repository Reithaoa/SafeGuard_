package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

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
    private DrawerLayout drawerLayout;
    private DatabaseHelper databaseHelper;

    private PermissionHandler permissionHandler;

    private final ActivityResultLauncher<Intent> profileActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Profile created successfully", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> requestSmsPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean sendSmsGranted = result.get(Manifest.permission.SEND_SMS);
                Boolean receiveSmsGranted = result.get(Manifest.permission.RECEIVE_SMS);
                Boolean readSmsGranted = result.get(Manifest.permission.READ_SMS);

                if (sendSmsGranted != null && receiveSmsGranted != null && readSmsGranted != null) {
                    permissionHandler.handleSmsPermissionsResult(sendSmsGranted, receiveSmsGranted, readSmsGranted);
                }
            });

    private final ActivityResultLauncher<String[]> requestLocationPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if (fineLocationGranted != null && coarseLocationGranted != null) {
                    permissionHandler.handleLocationPermissionsResult(fineLocationGranted, coarseLocationGranted, fusedLocationClient, locationCallback);
                }
            });

    private final ActivityResultLauncher<String[]> requestContactsPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean readContactsGranted = result.get(Manifest.permission.READ_CONTACTS);
                Boolean writeContactsGranted = result.get(Manifest.permission.WRITE_CONTACTS);

                if (readContactsGranted != null && writeContactsGranted != null) {
                    permissionHandler.handleContactsPermissionsResult(readContactsGranted, writeContactsGranted);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseHelper = new DatabaseHelper(this);
        permissionHandler = new PermissionHandler(this);

        requestNecessaryPermissions();

        //Remove background name
//        getTitle();
//        getSupportActionBar().hide();
//        setContentView(R.layout.activity_main);
        // Initialize the drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Initialize the toolbar and set it as the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.hamburgerMenu).setOnClickListener(v -> openDrawer());

        // Set up the navigation view item selected listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
        btnSpeak = findViewById(R.id.btnSpeak);
        btnListen = findViewById(R.id.btnListen);
        etInput = findViewById(R.id.etInput);
        tvLocation = findViewById(R.id.tvLocation);
//        btnCreateProfile = findViewById(R.id.btnCreateProfile);
        btnTrackMe = findViewById(R.id.btnTrackMe);
        btnSOS = findViewById(R.id.btnSOS);
        btnHelp = findViewById(R.id.btnHelp);

        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnSOS.setOnClickListener(v -> getLocationAndSend());

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

//        btnCreateProfile.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
//            profileActivityLauncher.launch(intent);
//        });

        btnTrackMe.setOnClickListener(v -> {
            if (permissionHandler.hasLocationPermission()) {
                List<Contact> contacts = databaseHelper.getAllContacts();
                if (!contacts.isEmpty()) {
                    showContactSelectionDialog(contacts);
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "No emergency contacts found", Toast.LENGTH_SHORT).show();
                }
            } else {
                permissionHandler.requestLocationPermission(requestLocationPermissionsLauncher);
            }
        });

        btnSOS.setOnClickListener(v -> {
            if (permissionHandler.hasSmsPermissions()) {
                List<Contact> contacts = databaseHelper.getAllContacts();
                if (contacts.isEmpty()) {
                    Toast.makeText(this, "No emergency contacts found", Toast.LENGTH_SHORT).show();
                } else {
                    if (permissionHandler.hasLocationPermission()) {
                        try {
                            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                                if (location != null) {
                                    String message = "Emergency! I'm at: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                                    for (Contact contact : contacts) {
                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendTextMessage(contact.getPhone(), null, message, null, null);
                                    }
                                    Toast.makeText(MainActivity.this, "SOS message sent", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (SecurityException e) {
                            Toast.makeText(MainActivity.this, "Location permission is required to send SOS message.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        permissionHandler.requestLocationPermission(requestLocationPermissionsLauncher);
                    }
                }
            } else {
                permissionHandler.requestSmsPermissions(requestSmsPermissionsLauncher);
            }
        });

        btnHelp.setOnClickListener(v -> showHelpDialog());
    }

    private void requestNecessaryPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (!permissionHandler.hasLocationPermission()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionHandler.hasSmsPermissions()) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS);
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS);
            permissionsToRequest.add(Manifest.permission.READ_SMS);
        }

        if (!permissionsToRequest.isEmpty()) {
            String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
            requestContactsPermissionsLauncher.launch(permissionsArray);
            requestLocationPermissionsLauncher.launch(permissionsArray);
            requestSmsPermissionsLauncher.launch(permissionsArray);

        }
    }

    private void showContactSelectionDialog(List<Contact> contacts) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Contact");

        String[] contactNames = new String[contacts.size()];
        for (int i = 0; i < contacts.size(); i++) {
            contactNames[i] = contacts.get(i).getName();
        }

        builder.setItems(contactNames, (dialog, which) -> {
            Contact selectedContact = contacts.get(which);
            String message = "Emergency! I'm at: https://maps.google.com/?q=" + gMap.getCameraPosition().target.latitude + "," + gMap.getCameraPosition().target.longitude;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(selectedContact.getPhone(), null, message, null, null);
            Toast.makeText(MainActivity.this, "SOS message sent to " + selectedContact.getName(), Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    gMap.clear();
                    gMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    private void getLocationAndSend() {
        if (permissionHandler.hasLocationPermission()) {
            try {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        String message = "Emergency! I'm at: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (SecurityException e) {
                Toast.makeText(MainActivity.this, "Location permission is required.", Toast.LENGTH_SHORT).show();
            }
        } else {
            permissionHandler.requestLocationPermission(requestLocationPermissionsLauncher);
        }
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help");
        builder.setMessage("Here's how to use the app:\n\n" +
                "1. Speak into the mic to input text.\n" +
                "2. Click 'Listen' to hear the spoken text.\n" +
                "3. Click 'Track Me' to track your location and send it to emergency contacts.\n" +
                "4. Click 'SOS' to send an emergency message with your location to contacts.\n" +
                "5. Click 'Help' to see this help message.");

        builder.setPositiveButton("OK", null);
        builder.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            profileActivityLauncher.launch(intent);
        } else if (id == R.id.nav_map) {
            // Handle map item
        } else if (id == R.id.nav_settings) {
            // Handle settings item
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Enable the My Location layer
            gMap.setMyLocationEnabled(true);

            // Get the user's current location and move the camera
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }
            });
        }
    }

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
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }
}
