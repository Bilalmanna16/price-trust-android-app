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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private TextView tvProductName, tvProductPrice, tvTrustScore;
    private TextView tvStatus, tvExplanation, tvConfidence;
    private TextView tvKMeansScore, tvIsolationScore, tvMlReason;

    private MaterialCardView cardTrustScore;
    private ProgressBar progressTrust;
    private LineChart priceChart;

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
        tvKMeansScore = findViewById(R.id.tvKMeansScore);
        tvIsolationScore = findViewById(R.id.tvIsolationScore);
        tvMlReason = findViewById(R.id.tvMlReason);

        cardTrustScore = findViewById(R.id.cardTrustScore);
        progressTrust = findViewById(R.id.progressTrust);
        priceChart = findViewById(R.id.priceChart);

        // Receive data
        Intent intent = getIntent();
        String productName = intent.getStringExtra("product_name");
        double productPrice = intent.getDoubleExtra("product_price", 0.0);

        tvProductName.setText(productName);
        tvProductPrice.setText("₹" + productPrice);

        // Fetch historical prices
        DBHelper dbHelper = new DBHelper(this);
        List<Double> historicalPrices = dbHelper.getPricesForProduct(productName);

        // Chart
        if (historicalPrices != null && historicalPrices.size() > 0) {
            showPriceTrendChart(historicalPrices, productPrice);
        } else {
            priceChart.setVisibility(View.GONE);
        }

        // Local trust score
        double localTrustScore =
                TrustScoreMapper.calculateTrustScore(productPrice, historicalPrices);

        // Loading UI
        progressTrust.setVisibility(View.VISIBLE);
        tvTrustScore.setVisibility(View.INVISIBLE);
        tvStatus.setText("Analyzing...");

        // ML call
        new Thread(() -> {
            try {
                JSONObject mlResponse = ApiClient.callMLApi(
                        productName, productPrice, historicalPrices
                );

                double mlScore = mlResponse.getDouble("ml_trust_score");
                double kmeansScore = mlResponse.getDouble("kmeans_score");
                double isolationScore = mlResponse.getDouble("isolation_score");
                String mlReason = mlResponse.getString("reason");

                double finalScore = (0.6 * localTrustScore) + (0.4 * mlScore);

                new Handler(Looper.getMainLooper()).post(() -> {
                    progressTrust.setVisibility(View.GONE);
                    tvTrustScore.setVisibility(View.VISIBLE);

                    double roundedScore = Math.round(finalScore);
                    tvTrustScore.setText(String.valueOf((int) roundedScore));

                    applyTrustColor(roundedScore, cardTrustScore, tvTrustScore, tvStatus);

                    tvKMeansScore.setText("K-Means Score: " + kmeansScore);
                    tvIsolationScore.setText("Isolation Forest Score: " + isolationScore);
                    tvMlReason.setText(mlReason);

                    tvExplanation.setText(getExplanationText(roundedScore, false));
                    tvConfidence.setText(
                            getConfidenceText(roundedScore, historicalPrices.size(), false)
                    );
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressTrust.setVisibility(View.GONE);
                    tvTrustScore.setVisibility(View.VISIBLE);

                    double roundedScore = Math.round(localTrustScore);
                    tvTrustScore.setText(String.valueOf((int) roundedScore));

                    applyTrustColor(roundedScore, cardTrustScore, tvTrustScore, tvStatus);

                    tvKMeansScore.setText("K-Means Score: N/A");
                    tvIsolationScore.setText("Isolation Forest Score: N/A");
                    tvMlReason.setText("ML service unavailable. Using local analysis.");

                    tvExplanation.setText(getExplanationText(roundedScore, true));
                    tvConfidence.setText(
                            getConfidenceText(roundedScore, historicalPrices.size(), true)
                    );
                });
            }
        }).start();
    }

    // -------- CHART --------

    private void showPriceTrendChart(List<Double> historicalPrices, double currentPrice) {
        List<Entry> historyEntries = new ArrayList<>();

        for (int i = 0; i < historicalPrices.size(); i++) {
            historyEntries.add(new Entry(i, historicalPrices.get(i).floatValue()));
        }

        LineDataSet historySet = new LineDataSet(historyEntries, "History");
        historySet.setColor(ContextCompat.getColor(this, R.color.trust_yellow));
        historySet.setCircleColor(ContextCompat.getColor(this, R.color.trust_yellow));
        historySet.setDrawValues(false);

        List<Entry> currentEntry = new ArrayList<>();
        currentEntry.add(new Entry(historicalPrices.size(), (float) currentPrice));

        LineDataSet currentSet = new LineDataSet(currentEntry, "Current");
        currentSet.setColor(ContextCompat.getColor(this, R.color.trust_red));
        currentSet.setCircleColor(ContextCompat.getColor(this, R.color.trust_red));
        currentSet.setDrawValues(false);
        currentSet.setLineWidth(0f);

        priceChart.setData(new LineData(historySet, currentSet));
        priceChart.getDescription().setEnabled(false);
        priceChart.getLegend().setEnabled(false);

        XAxis xAxis = priceChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        priceChart.getAxisRight().setEnabled(false);
        priceChart.invalidate();
    }

    // -------- HELPERS --------

    private void applyTrustColor(double score, MaterialCardView card,
                                 TextView scoreView, TextView statusView) {
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

    private String getExplanationText(double score, boolean isOffline) {
        if (isOffline) {
            return score >= 75 ? "This price closely matches your past purchases."
                    : score >= 50 ? "This price slightly deviates from your history."
                    : "This price is far from your usual range.";
        } else {
            return score >= 75 ? "ML found this price well within the normal range."
                    : score >= 50 ? "ML detected moderate deviation."
                    : "ML flagged this price as highly abnormal.";
        }
    }

    private String getConfidenceText(double score, int count, boolean isOffline) {
        String level = count < 3 ? "Low" : count <= 6 ? "Medium" : "High";

        if (score >= 75 || score < 50) {
            if (level.equals("Medium")) level = "High";
            else if (level.equals("Low")) level = "Medium";
        }

        return "Confidence: " + level + (isOffline ? " (local)" : " (ML-assisted)");
    }
}
