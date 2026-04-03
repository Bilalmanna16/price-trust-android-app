package com.App.pricetrust.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {

    private static final String PREF = "auth_pref";

    // 🔥 SAVE USER (Signup)
    public static void saveUser(Context context, String email, String password) {

        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        sp.edit()
                .putString("email", email.trim().toLowerCase()) // normalize
                .putString("password", password.trim())
                .apply();
    }

    // 🔥 LOGIN
    public static boolean login(Context context, String email, String password) {

        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        String savedEmail = sp.getString("email", null);
        String savedPassword = sp.getString("password", null);

        if (savedEmail == null || savedPassword == null) return false;

        return savedEmail.equals(email.trim().toLowerCase()) &&
                savedPassword.equals(password.trim());
    }

    // 🔥 SESSION
    public static void setLoggedIn(Context context, boolean value) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("loggedIn", value)
                .apply();
    }

    public static boolean isLoggedIn(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean("loggedIn", false);
    }

    public static void logout(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}