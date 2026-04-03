package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.App.pricetrust.R;
import com.App.pricetrust.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {

    TextInputEditText etEmail, etPassword;
    TextView tvLength, tvUpper, tvSymbol;
    MaterialButton btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        tvLength = findViewById(R.id.tvRuleLength);
        tvUpper = findViewById(R.id.tvRuleUpper);
        tvSymbol = findViewById(R.id.tvRuleSymbol);

        btnSignup = findViewById(R.id.btnSignup);

        etPassword.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        btnSignup.setOnClickListener(v -> {

            String email = etEmail.getText().toString();
            String pass = etPassword.getText().toString();

            if (isValid(pass)) {
                AuthManager.saveUser(this, email, pass);
                AuthManager.setLoggedIn(this, true);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private void validatePassword(String pass) {

        boolean length = pass.length() >= 6;
        boolean upper = pass.matches(".*[A-Z].*");
        boolean symbol = pass.matches(".*[^a-zA-Z0-9].*");

        update(tvLength, length);
        update(tvUpper, upper);
        update(tvSymbol, symbol);
    }

    private boolean isValid(String pass) {
        return pass.length() >= 6 &&
                pass.matches(".*[A-Z].*") &&
                pass.matches(".*[^a-zA-Z0-9].*");
    }

    private void update(TextView tv, boolean ok) {
        tv.setTextColor(ContextCompat.getColor(this,
                ok ? android.R.color.holo_green_dark : android.R.color.darker_gray));
    }
}