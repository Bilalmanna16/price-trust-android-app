package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.App.pricetrust.HistoryFragment;
import com.App.pricetrust.HomeFragment;
import com.App.pricetrust.R;
import com.App.pricetrust.auth.AuthManager;
import com.App.pricetrust.database.DBHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<String> allowed = Arrays.asList(

            // 📱 Electronics
            "phone","mobile","smartphone","iphone","android",
            "laptop","macbook","notebook","ultrabook",
            "tablet","ipad",
            "monitor","display","screen",
            "tv","television","smart tv",

            // 🎧 Accessories
            "headphones","earphones","earbuds","airpods",
            "speaker","bluetooth speaker",
            "keyboard","mechanical keyboard",
            "mouse","gaming mouse",

            // ⌚ Wearables
            "watch","smartwatch","fitness band",

            // 📷 Camera
            "camera","dslr","mirrorless camera",
            "tripod","lens",

            // 🎮 Gaming
            "gaming console","playstation","xbox",
            "controller","gaming chair",

            // 👟 Fashion
            "shoes","sneakers","boots","sandals",
            "tshirt","shirt","jeans","jacket","hoodie",

            // 🏠 Home Appliances
            "refrigerator","fridge","washing machine",
            "microwave","oven","air conditioner","ac",
            "fan","cooler",

            // 🍳 Kitchen
            "mixer","blender","grinder",
            "cookware","pan","pressure cooker",

            // 🪑 Furniture
            "chair","table","desk","sofa","bed",

            // 📚 Office / Study
            "printer","scanner","router","wifi router",
            "pen","notebook","books",

            // 🚗 Automotive
            "car","bike","helmet",
            "car accessories","bike accessories",

            // 💄 Personal Care
            "perfume","trimmer","shaver",
            "hair dryer","skincare","cosmetics",

            // 🧸 Misc
            "bag","backpack","wallet",
            "bottle","water bottle","umbrella"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 FIX: auth check MUST be here
        if (!AuthManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

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

        DBHelper db = new DBHelper(this);
        db.insertPrice(name, price);

        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("product_name", name);
        i.putExtra("product_price", price);

        startActivity(i);
    }
}