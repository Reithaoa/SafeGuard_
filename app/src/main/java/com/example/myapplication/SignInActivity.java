package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import util.UserPhoneNumber;

public class SignInActivity extends AppCompatActivity {

    private EditText etPhoneNumber, etOtp, ctrcode;
    private Button btnSendOtp, btnVerifyOtp;
    private String verificationId;
    private FirebaseAuth mAuth;
    private Spinner countries;
    private TextView tvTopMessage, confirmCountry, otpMessage,confirmOtp;
    private String number, code;
    public static String selectedCountry;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etOtp = findViewById(R.id.etOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        countries = findViewById(R.id.countries);
        ctrcode = findViewById(R.id.ctrCode);
        tvTopMessage = findViewById(R.id.tvTopMessage);
        confirmCountry= findViewById(R.id.confirmCountry);
        confirmOtp = findViewById(R.id.confirmOtp);
        otpMessage= findViewById(R.id.otpMessage);



//        initializing the countries array
        List<String> countriesList = new UserPhoneNumber().getCountries(); // loading all the countries
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,countriesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countries.setAdapter(adapter); // setting the countries to the spinner


        countries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCountry = countries.getSelectedItem().toString();
                ctrcode.setText(String.format("+%s", new UserPhoneNumber().getRegionCode(selectedCountry)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });



        // add check for invalid region code
        ctrcode.setOnFocusChangeListener((view, hasFocus) ->{
            if(!hasFocus){ // we have lost focus of the editor
                ctrcode.setText(validateCountryCode(ctrcode));
                setCountry(ctrcode.getText().toString(),countriesList);
            }
        });

        btnSendOtp.setOnClickListener(v -> {
            number = etPhoneNumber.getText().toString();
            code = ctrcode.getText().toString();
            String phoneNumber = new UserPhoneNumber(number,code).toString();
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(SignInActivity.this, "Enter mobile number", Toast.LENGTH_SHORT).show();
                return;
            }
            sendOtp("+" + phoneNumber);
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String code = etOtp.getText().toString();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(SignInActivity.this, "Enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyOtp(code);
        });
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        // Auto-retrieval or Instant verification
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(SignInActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        mainSignInWidgetsDisappear(false);
                        otpSignInWidgets(true);
                        Toast.makeText(SignInActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void otpSignInWidgets(Boolean visibility){
        int visible = (visibility) ? View.VISIBLE : View.GONE;
        String newMessage = confirmOtp.getText().toString() + " (" + code + ") " + number;
        confirmOtp.setText(newMessage);

        etOtp.setVisibility(visible);
        btnVerifyOtp.setVisibility(visible);
        otpMessage.setVisibility(visible);
        confirmOtp.setVisibility(visible);
    }

    private void mainSignInWidgetsDisappear(Boolean visibility){
        int visible = (visibility) ? View.VISIBLE : View.GONE;
        etPhoneNumber.setVisibility(visible);
        btnSendOtp.setVisibility(visible);
        countries.setVisibility(visible);
        ctrcode.setVisibility(visible);
        tvTopMessage.setVisibility(visible);
        confirmCountry.setVisibility(visible);
    }


    private String validateCountryCode(EditText text){
        ArrayList<String> code = new ArrayList<>(Arrays.asList(text.getText().toString().split("")));
        if(!code.get(0).equals("+")){
            code.add(0,"+");
            StringBuilder string = new StringBuilder();
            for (String c : code){
                string.append(c);
            }
           return string.toString();
        }

        return text.getText().toString();

    }

    private void setCountry(String code,List<String> countryList){
        int indexOfCountry = countryList.indexOf(new UserPhoneNumber().getCountry(code));
        countries.setSelection(indexOfCountry);
    }

    private void verifyOtp(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SignInActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(SignInActivity.this, "Sign in failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
