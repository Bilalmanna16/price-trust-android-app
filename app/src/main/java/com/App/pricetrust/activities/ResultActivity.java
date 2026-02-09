package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
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

    private TextView tvProductName, tvProductPrice, tvTrustScore;
    private TextView tvStatus, tvExplanation, tvConfidence;
    private MaterialCardView cardTrustScore;
    private ProgressBar progressTrust;

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
        progressTrust = findViewById(R.id.progressTrust);

        // Receive data
        Intent intent = getIntent();
        String productName = intent.getStringExtra("product_name");
        double productPrice = intent.getDoubleExtra("product_price", 0.0);

        tvProductName.setText(productName);
        tvProductPrice.setText("₹" + productPrice);

        // Fetch historical prices
        DBHelper dbHelper = new DBHelper(this);
        List<Double> historicalPrices =
                dbHelper.getPricesForProduct(productName);

        // Local trust score (offline)
        double localTrustScore = TrustScoreMapper.calculateTrustScore(
                productPrice, historicalPrices
        );

        // Initial loading UI
        progressTrust.setVisibility(View.VISIBLE);
        tvTrustScore.setVisibility(View.INVISIBLE);
        tvStatus.setText("Analyzing...");
        tvExplanation.setText("Analyzing price based on past data.");
        tvConfidence.setText("Confidence: Medium");

        // ML call in background
        new Thread(() -> {
            try {
                JSONObject mlResponse = ApiClient.callMLApi(
                        productName, productPrice, historicalPrices
                );

                double mlScore = mlResponse.getDouble("ml_trust_score");
                double finalScore = (0.6 * localTrustScore) + (0.4 * mlScore);

                new Handler(Looper.getMainLooper()).post(() -> {
                    progressTrust.setVisibility(View.GONE);
                    tvTrustScore.setVisibility(View.VISIBLE);

                    double roundedScore = Math.round(finalScore);
                    tvTrustScore.setText(String.valueOf((int) roundedScore));

                    applyTrustColor(
                            roundedScore,
                            cardTrustScore,
                            tvTrustScore,
                            tvStatus
                    );

                    tvExplanation.setText(
                            getExplanationText(roundedScore, false)
                    );

                    tvConfidence.setText(
                            getConfidenceText(
                                    roundedScore,
                                    historicalPrices.size(),
                                    false
                            )
                    );
                });

            } catch (Exception e) {
                // Offline fallback
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressTrust.setVisibility(View.GONE);
                    tvTrustScore.setVisibility(View.VISIBLE);

                    double roundedScore = Math.round(localTrustScore);
                    tvTrustScore.setText(String.valueOf((int) roundedScore));

                    applyTrustColor(
                            roundedScore,
                            cardTrustScore,
                            tvTrustScore,
                            tvStatus
                    );

                    tvExplanation.setText(
                            getExplanationText(roundedScore, true)
                    );

                    tvConfidence.setText(
                            getConfidenceText(
                                    roundedScore,
                                    historicalPrices.size(),
                                    true
                            )
                    );
                });
            }
        }).start();
    }

    // Color-coded UI helper
    private void applyTrustColor(
            double score,
            MaterialCardView card,
            TextView scoreView,
            TextView statusView
    ) {
        int textColor, bgColor;
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

    // Dynamic explanation helper
    private String getExplanationText(double score, boolean isOffline) {

        if (isOffline) {
            if (score >= 75) {
                return "This price closely matches your previous purchase history.";
            } else if (score >= 50) {
                return "This price is slightly different from your usual range.";
            } else {
                return "This price is far from your historical prices and may be risky.";
            }
        } else {
            if (score >= 75) {
                return "This price is well within the normal range based on machine learning analysis.";
            } else if (score >= 50) {
                return "This price shows moderate deviation from typical pricing patterns.";
            } else {
                return "Machine learning detected this price as highly abnormal compared to past data.";
            }
        }
    }

    // Confidence level helper
    private String getConfidenceText(
            double score,
            int dataCount,
            boolean isOffline
    ) {
        String level;

        if (dataCount < 3) {
            level = "Low";
        } else if (dataCount <= 6) {
            level = "Medium";
        } else {
            level = "High";
        }

        // Increase confidence for extreme scores
        if (score >= 75 || score < 50) {
            if (level.equals("Medium")) level = "High";
            else if (level.equals("Low")) level = "Medium";
        }

        if (isOffline) {
            return "Confidence: " + level + " (based on local history)";
        } else {
            return "Confidence: " + level + " (ML-assisted analysis)";
        }
    }
}
