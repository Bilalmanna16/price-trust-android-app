package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.App.pricetrust.R;
import com.App.pricetrust.database.DBHelper;
import com.App.pricetrust.ml.TrustScoreMapper;
import com.App.pricetrust.network.ApiClient;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvTrustScore;
    private TextView tvStatus;
    private TextView tvExplanation;
    private TextView tvConfidence;

    private MaterialCardView cardTrustScore;

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
        cardTrustScore = findViewById(R.id.cardTrustScore);

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

                // Hybrid score (local + ML)
                double finalScore =
                        (0.6 * localTrustScore) + (0.4 * mlScore);

                new Handler(Looper.getMainLooper()).post(() -> {
                    double roundedScore = Math.round(finalScore);

                    tvTrustScore.setText(String.valueOf((int) roundedScore));
                    applyTrustColor(
                            roundedScore,
                            cardTrustScore,
                            tvTrustScore,
                            tvStatus
                    );

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
                    double roundedScore = Math.round(localTrustScore);

                    tvTrustScore.setText(String.valueOf((int) roundedScore));
                    applyTrustColor(
                            roundedScore,
                            cardTrustScore,
                            tvTrustScore,
                            tvStatus
                    );

                    tvExplanation.setText(
                            "Trust score calculated locally using your past price history."
                    );
                    tvConfidence.setText("Confidence: Medium");
                });
            }
        }).start();
    }

    // Helper method for color-coded trust UI
    private void applyTrustColor(
            double score,
            MaterialCardView card,
            TextView scoreView,
            TextView statusView
    ) {
        int textColor;
        int bgColor;
        String status;

        if (score >= 75) {
            textColor = R.color.trust_green;
            bgColor = R.color.trust_green_bg;
            status = "Fair";
        } else if (score >= 50) {
            textColor = R.color.trust_yellow;
            bgColor = R.color.trust_yellow_bg;
            status = "Moderate";
        } else {
            textColor = R.color.trust_red;
            bgColor = R.color.trust_red_bg;
            status = "Suspicious";
        }

        scoreView.setTextColor(ContextCompat.getColor(this, textColor));
        statusView.setTextColor(ContextCompat.getColor(this, textColor));
        card.setCardBackgroundColor(ContextCompat.getColor(this, bgColor));
        statusView.setText(status);
    }
}
