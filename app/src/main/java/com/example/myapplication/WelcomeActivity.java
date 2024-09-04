package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;

public class WelcomeActivity extends AppCompatActivity {
//    private Button btnEnableLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Find the welcome message TextView
        TextView welcomeMessage = findViewById(R.id.welcomeMessage);
        welcomeMessage.setText("Welcome to SafeGuard, where your safety is guaranteed");

//        btnEnableLocation = findViewById(R.id.btnEnableLocation);

        // Display the welcome screen for 6 seconds and then check if location services are enabled
        new Handler().postDelayed(() -> {
            if (isLocationEnabled()) {
                startSignInActivity();
            } else {
                showLocationEnableDialog();
                startSignInActivity();
            }

        }, 6000); // 6000 milliseconds = 6 seconds
    }

    private boolean isLocationEnabled() {
        int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

    private void showLocationEnableDialog() {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable Location Services");
        builder.setMessage("To use SafeGuard, enable location services.");
        builder.setPositiveButton("Enable", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Toast.makeText(this, "Location services must be enabled to use SafeGuard", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.setCancelable(false);
        builder.show();*/
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setIntervalMillis(5)
                .build();

    }

    private void startSignInActivity() {
        Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
