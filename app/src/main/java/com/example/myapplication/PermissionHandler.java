package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;


public class PermissionHandler {

    private final Activity activity;

    public PermissionHandler(Activity activity) {
        this.activity = activity;
    }

    public boolean hasMicrophonePermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasSmsPermissions() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasContactsPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestMicrophonePermission(ActivityResultLauncher<String> launcher) {
        if (!hasMicrophonePermission()) {
            launcher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    public void requestCameraPermission(ActivityResultLauncher<String> launcher) {
        if (!hasCameraPermission()) {
            launcher.launch(Manifest.permission.CAMERA);
        }
    }

    public void requestLocationPermission(ActivityResultLauncher<String[]> launcher) {
        if (!hasLocationPermission()) {
            launcher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    public void requestSmsPermissions(ActivityResultLauncher<String[]> launcher) {
        if (!hasSmsPermissions()) {
            launcher.launch(new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
            });
        }
    }

    public void requestContactsPermissions(ActivityResultLauncher<String[]> launcher) {
        if (!hasContactsPermission()) {
            launcher.launch(new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
            });
        }
    }

    public void handleContactsPermissionsResult(Boolean readContactsGranted, Boolean writeContactsGranted) {
    }

    public void handleLocationPermissionsResult(Boolean fineLocationGranted, Boolean coarseLocationGranted, FusedLocationProviderClient fusedLocationClient, LocationCallback locationCallback) {
    }

    public void handleSmsPermissionsResult(Boolean sendSmsGranted, Boolean receiveSmsGranted, Boolean readSmsGranted) {
    }
}
