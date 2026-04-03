package com.App.pricetrust;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.App.pricetrust.activities.LoginActivity;
import com.App.pricetrust.activities.MainActivity;
import com.App.pricetrust.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextInputLayout tilName, tilPrice;
    private TextInputEditText etName, etPrice;
    private MaterialButton btnAnalyze, btnLogout;

    private final List<String> allowed = Arrays.asList(
            "shoes","laptop","phone","mobile","tablet",
            "watch","headphones","camera","tv","monitor",
            "keyboard","mouse","printer"
    );

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilName = view.findViewById(R.id.tilProductName);
        tilPrice = view.findViewById(R.id.tilProductPrice);
        etName = view.findViewById(R.id.etProductName);
        etPrice = view.findViewById(R.id.etProductPrice);
        btnAnalyze = view.findViewById(R.id.btnAnalyze);

        // 🔥 LOGOUT BUTTON
        btnLogout = view.findViewById(R.id.btnLogout);

        btnAnalyze.setOnClickListener(v -> validateAndProceed());

        btnLogout.setOnClickListener(v -> {
            AuthManager.logout(requireContext());
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void validateAndProceed() {

        tilName.setError(null);
        tilPrice.setError(null);

        String name = etName.getText().toString().toLowerCase().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Enter product name");
            return;
        }

        boolean match = false;
        for (String p : allowed) {
            if (name.contains(p)) {
                match = true;
                break;
            }
        }

        if (!match) {
            tilName.setError("Only real product names allowed");
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            tilPrice.setError("Enter valid price");
            return;
        }

        double price;

        try {
            price = Double.parseDouble(priceStr);
        } catch (Exception e) {
            tilPrice.setError("Invalid number");
            return;
        }

        if (price < 500) {
            tilPrice.setError("Unrealistic price");
            return;
        }

        if (price > 1000000) {
            tilPrice.setError("Too high");
            return;
        }

        ((MainActivity) requireActivity()).handleAnalyze(name, price);
    }
}