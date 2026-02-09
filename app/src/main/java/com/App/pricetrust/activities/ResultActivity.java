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

    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvTrustScore;
    private TextView tvStatus;
    private TextView tvExplanation;
    private TextView tvConfidence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Bind views
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvTrustScore = findViewById(R.id.tvTrustScore);
        tvStatus = findViewById(R.id.tvStatus);
        tvExplanation = findViewById(R.id.tvExplanation);
        tvConfidence = findViewById(R.id.tvConfidence);

        // Receive data from MainActivity
        Intent intent = getIntent();
        String productName = intent.getStringExtra("product_name");
        double productPrice = intent.getDoubleExtra("product_price", 0.0);

        // Display product info
        tvProductName.setText(productName);
        tvProductPrice.setText("₹" + productPrice);

        // Fetch historical prices from SQLite
        DBHelper dbHelper = new DBHelper(this);
        List<Double> historicalPrices =
                dbHelper.getPricesForProduct(productName);

        // Local (offline) trust score
        double localTrustScore = TrustScoreMapper.calculateTrustScore(
                productPrice,
                historicalPrices
        );

        // Initial UI state
        tvTrustScore.setText("--");
        tvStatus.setText("Analyzing...");
        tvExplanation.setText("Analyzing price based on past data.");
        tvConfidence.setText("Confidence: Medium");

        // Call ML API in background thread
        new Thread(() -> {
            try {
                JSONObject mlResponse = ApiClient.callMLApi(
                        productName,
                        productPrice,
                        historicalPrices
                );

                double mlScore = mlResponse.getDouble("ml_trust_score");
                String category = mlResponse.getString("category");

                // Hybrid score (local + ML)
                double finalScore =
                        (0.6 * localTrustScore) + (0.4 * mlScore);

                new Handler(Looper.getMainLooper()).post(() -> {
                    tvTrustScore.setText(String.valueOf(Math.round(finalScore)));
                    tvStatus.setText(category);
                    tvExplanation.setText(
                            "This price was compared with your previous prices for this product."
                    );
                    tvConfidence.setText(
                            "Confidence: Based on historical price patterns"
                    );
                });

            } catch (Exception e) {
                // Fallback to offline score
                new Handler(Looper.getMainLooper()).post(() -> {
                    tvTrustScore.setText(String.valueOf(Math.round(localTrustScore)));
                    tvStatus.setText("Offline Mode");
                    tvExplanation.setText(
                            "Trust score calculated locally using your past price history."
                    );
                    tvConfidence.setText("Confidence: Medium");
                });
            }
        }).start();
    }
}
