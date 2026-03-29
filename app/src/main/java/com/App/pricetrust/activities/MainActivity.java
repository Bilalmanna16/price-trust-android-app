package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.App.pricetrust.HistoryFragment;
import com.App.pricetrust.HomeFragment;
import com.App.pricetrust.R;
import com.App.pricetrust.database.DBHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<String> allowed = Arrays.asList(
            "shoes","laptop","phone","mobile","tablet",
            "watch","headphones","camera","tv","monitor",
            "keyboard","mouse","printer"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Default fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment selected = null;

            if (item.getItemId() == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_history) {
                selected = new HistoryFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, selected)
                    .commit();

            return true;
        });
    }

    // 🔥 Called from HomeFragment
    public void handleAnalyze(String name, double price) {

        // Save to DB
        DBHelper db = new DBHelper(this);
        db.insertPrice(name, price);

        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("product_name", name);
        i.putExtra("product_price", price);

        startActivity(i);
    }
}