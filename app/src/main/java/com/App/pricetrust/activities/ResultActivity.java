package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.App.pricetrust.R;
import com.App.pricetrust.database.DBHelper;
import com.App.pricetrust.ml.TrustScoreMapper;
import com.App.pricetrust.network.ApiClient;

import org.json.JSONObject;

import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private TextView tvProductName, tvProductPrice, tvTrustScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Bind views
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvTrustScore = findViewById(R.id.tvTrustScore);

        // Receive data from MainActivity
        Intent intent = getIntent();
        String productName = intent.getStringExtra("product_name");
        double productPrice = intent.getDoubleExtra("product_price", 0.0);

        // Display basic data
        tvProductName.setText("Product: " + productName);
        tvProductPrice.setText("Price: ₹" + productPrice);

        // Fetch historical prices
        DBHelper dbHelper = new DBHelper(this);
        List<Double> historicalPrices =
                dbHelper.getPricesForProduct(productName);

        // Local trust score (offline baseline)
        double localTrustScore = TrustScoreMapper.calculateTrustScore(
                productPrice,
                historicalPrices
        );

        tvTrustScore.setText("Calculating trust score...");

        // Call ML API in background
        new Thread(() -> {
            try {
                JSONObject mlResponse = ApiClient.callMLApi(
                        productName,
                        productPrice,
                        historicalPrices
                );

                double mlScore = mlResponse.getDouble("ml_trust_score");
                String category = mlResponse.getString("category");

                double finalScore =
                        (0.6 * localTrustScore) + (0.4 * mlScore);

                new Handler(Looper.getMainLooper()).post(() -> {
                    tvTrustScore.setText(
                            "Trust Score: " + finalScore + "\nStatus: " + category
                    );
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    tvTrustScore.setText(
                            "Trust Score (Offline): " + localTrustScore
                    );
                });
            }
        }).start();
    }
}
