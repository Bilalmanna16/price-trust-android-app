package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.App.pricetrust.R;
import com.App.pricetrust.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {

    TextInputEditText etEmail, etPassword;
    MaterialButton btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim().toLowerCase();
            String pass = etPassword.getText().toString().trim();

            android.util.Log.d("AUTH_DEBUG", "Signup clicked");

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 TEMP SIMPLE VALIDATION (no blocking bugs)
            if (pass.length() < 4) {
                Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthManager.saveUser(this, email, pass);

            Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();

            // 🔥 go to login (IMPORTANT)
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}