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
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private TextView tvProductName, tvProductPrice, tvTrustScore, tvStatus;
    private MaterialCardView cardTrustScore;
    private ProgressBar progressTrust;
    private LineChart priceChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Toolbar (Back Navigation)
        MaterialToolbar toolbar = findViewById(R.id.resultToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind views
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvTrustScore = findViewById(R.id.tvTrustScore);
        tvStatus = findViewById(R.id.tvStatus);

        cardTrustScore = findViewById(R.id.cardTrustScore);
        progressTrust = findViewById(R.id.progressTrust);
        priceChart = findViewById(R.id.priceChart);

        // Get Intent Data
        Intent intent = getIntent();

        String productName = intent.getStringExtra("product_name");
        double productPrice = intent.getDoubleExtra("product_price", -1);

        // Safety check
        if (productName == null || productPrice <= 0) {
            finish();
            return;
        }

        tvProductName.setText(productName);
        tvProductPrice.setText("₹" + productPrice);

        DBHelper dbHelper = new DBHelper(this);

        // ---------------- FIXED DATA PIPELINE ----------------

        List<Double> rawPrices = dbHelper.getPricesForProduct(productName);

        final List<Double> historicalPricesFinal = new ArrayList<>();

        // Clean + null safety
        if (rawPrices != null) {
            for (Double price : rawPrices) {
                if (price != null && price > 0) {
                    historicalPricesFinal.add(price);
                }
            }
        }

        // Smart fallback (IMPORTANT)
        if (historicalPricesFinal.size() < 3) {
            historicalPricesFinal.add(productPrice * 0.9);
            historicalPricesFinal.add(productPrice);
            historicalPricesFinal.add(productPrice * 1.1);
        }

        // ----------------------------------------------------

        // Local trust score
        double localScore =
                TrustScoreMapper.calculateTrustScore(productPrice, historicalPricesFinal);

        // Show loading
        progressTrust.setVisibility(View.VISIBLE);
        tvTrustScore.setVisibility(View.INVISIBLE);

        // ML call
        new Thread(() -> {

            try {

                JSONObject res = ApiClient.callMLApi(
                        productName,
                        productPrice,
                        historicalPricesFinal
                );

                double mlScore = res.getDouble("ml_trust_score");

                double finalScore = (0.6 * localScore) + (0.4 * mlScore);

                new Handler(Looper.getMainLooper()).post(() ->
                        updateUI(finalScore)
                );

            } catch (Throwable e) {

                new Handler(Looper.getMainLooper()).post(() ->
                        updateUI(localScore)
                );
            }

        }).start();
    }

    // ---------------- UI UPDATE ----------------

    private void updateUI(double score) {

        progressTrust.setVisibility(View.GONE);
        tvTrustScore.setVisibility(View.VISIBLE);

        int rounded = (int) Math.round(score);
        tvTrustScore.setText(String.valueOf(rounded));

        if (rounded >= 75) {
            tvStatus.setText("Fair");
            cardTrustScore.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.trust_green_bg));
        } else if (rounded >= 50) {
            tvStatus.setText("Moderate");
            cardTrustScore.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.trust_yellow_bg));
        } else {
            tvStatus.setText("Suspicious");
            cardTrustScore.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.trust_red_bg));
        }
    }
}