// ProfileActivity.java
//package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etSurname, etAge, etHeight, etRace, etEmergencyContact1, etEmergencyContact2;
    private Button btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etRace = findViewById(R.id.etRace);
        etEmergencyContact1 = findViewById(R.id.etEmergencyContact1);
        etEmergencyContact2 = findViewById(R.id.etEmergencyContact2);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString();
        String surname = etSurname.getText().toString();
        String age = etAge.getText().toString();
        String height = etHeight.getText().toString();
        String race = etRace.getText().toString();
        String emergencyContact1 = etEmergencyContact1.getText().toString();
        String emergencyContact2 = etEmergencyContact2.getText().toString();

        // Add validation checks and save the profile information
        if (name.isEmpty() || surname.isEmpty() || age.isEmpty() || height.isEmpty() || race.isEmpty() || emergencyContact1.isEmpty() || emergencyContact2.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        } else {
            // Save profile information (e.g., using SharedPreferences, SQLite, or a server)
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        }
    }
}

