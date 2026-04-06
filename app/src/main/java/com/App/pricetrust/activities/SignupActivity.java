package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.App.pricetrust.R;
import com.App.pricetrust.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {

    TextInputEditText etEmail, etPassword;
    TextInputLayout tilEmail, tilPassword;

    MaterialButton btnSignup;

    TextView tvLength, tvUpper, tvSymbol;

    boolean isEmailValid = false;
    boolean isPasswordValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        tvLength = findViewById(R.id.tvRuleLength);
        tvUpper = findViewById(R.id.tvRuleUpper);
        tvSymbol = findViewById(R.id.tvRuleSymbol);

        btnSignup = findViewById(R.id.btnSignup);

        // 🔥 EMAIL LIVE VALIDATION
        etEmail.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String email = s.toString().trim();

                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.setError(null);
                    isEmailValid = true;
                } else {
                    tilEmail.setError("Invalid email");
                    isEmailValid = false;
                }

                updateButtonState();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        // 🔥 PASSWORD LIVE VALIDATION
        etPassword.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String pass = s.toString();

                boolean length = pass.length() >= 6;
                boolean upper = pass.matches(".*[A-Z].*");
                boolean symbol = pass.matches(".*[^a-zA-Z0-9].*");

                updateRule(tvLength, length);
                updateRule(tvUpper, upper);
                updateRule(tvSymbol, symbol);

                isPasswordValid = length && upper && symbol;

                updateButtonState();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        btnSignup.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim().toLowerCase();
            String pass = etPassword.getText().toString().trim();

            AuthManager.saveUser(this, email, pass);

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void updateRule(TextView tv, boolean ok) {
        tv.setTextColor(ContextCompat.getColor(this,
                ok ? android.R.color.holo_green_dark : android.R.color.darker_gray));
    }

    private void updateButtonState() {
        btnSignup.setEnabled(isEmailValid && isPasswordValid);
    }
}