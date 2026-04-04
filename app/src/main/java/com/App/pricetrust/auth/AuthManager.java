package com.App.pricetrust.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AuthManager {

    private static final String PREF = "auth_pref";

    public static void saveUser(Context context, String email, String password) {

        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();

        editor.putString("email", email.trim().toLowerCase());
        editor.putString("password", password.trim());

        boolean success = editor.commit(); // 🔥 force save

        Log.d("AUTH_DEBUG", "Saving Email: " + email);
        Log.d("AUTH_DEBUG", "Saving Pass: " + password);
        Log.d("AUTH_DEBUG", "SAVE SUCCESS: " + success);
    }

    public static boolean login(Context context, String email, String password) {

        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        String savedEmail = sp.getString("email", null);
        String savedPassword = sp.getString("password", null);

        Log.d("AUTH_DEBUG", "Saved Email: " + savedEmail);
        Log.d("AUTH_DEBUG", "Input Email: " + email);
        Log.d("AUTH_DEBUG", "Saved Pass: " + savedPassword);
        Log.d("AUTH_DEBUG", "Input Pass: " + password);

        if (savedEmail == null || savedPassword == null) return false;

        return savedEmail.equals(email.trim().toLowerCase()) &&
                savedPassword.equals(password.trim());
    }

    public static void setLoggedIn(Context context, boolean value) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("loggedIn", value)
                .commit();
    }

    public static boolean isLoggedIn(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean("loggedIn", false);
    }

    public static void logout(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }
}