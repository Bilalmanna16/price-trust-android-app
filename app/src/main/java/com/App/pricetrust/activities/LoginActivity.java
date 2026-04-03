package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.App.pricetrust.R;
import com.App.pricetrust.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etEmail, etPassword;
    MaterialButton btnLogin, btnGoSignup;
    TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoSignup = findViewById(R.id.btnGoSignup);
        tvError = findViewById(R.id.tvError);

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString();
            String pass = etPassword.getText().toString();

            if (email.isEmpty() || pass.isEmpty()) {
                tvError.setText("All fields required");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            boolean success = AuthManager.login(this, email, pass);

            if (success) {
                AuthManager.setLoggedIn(this, true); // 🔥 IMPORTANT

                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        btnGoSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }
}