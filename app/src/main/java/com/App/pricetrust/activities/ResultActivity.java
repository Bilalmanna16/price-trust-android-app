package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.App.pricetrust.R;
import com.App.pricetrust.database.DBHelper;
import com.App.pricetrust.ml.TrustScoreMapper;
import com.App.pricetrust.network.ApiClient;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.BarChart;

import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.*;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private TextView tvProductName, tvProductPrice, tvTrustScore, tvStatus;
    private TextView tvAvg, tvMin, tvMax, tvDiff;

    private MaterialCardView cardTrustScore;
    private ProgressBar progressTrust;

    private LineChart priceChart;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 🔥 Toolbar (REAL back working)
        MaterialToolbar toolbar = findViewById(R.id.resultToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnClickListener(v -> {
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        });



        // 🔥 Bind Views
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvTrustScore = findViewById(R.id.tvTrustScore);
        tvStatus = findViewById(R.id.tvStatus);

        tvAvg = findViewById(R.id.tvAvg);
        tvMin = findViewById(R.id.tvMin);
        tvMax = findViewById(R.id.tvMax);
        tvDiff = findViewById(R.id.tvDiff);

        cardTrustScore = findViewById(R.id.cardTrustScore);
        progressTrust = findViewById(R.id.progressTrust);

        priceChart = findViewById(R.id.priceChart);
        barChart = findViewById(R.id.barChart);
        if (barChart == null) {
            throw new RuntimeException("BAR CHART IS NULL");
        }

        if (priceChart == null) throw new RuntimeException("priceChart NULL");
        if (tvProductName == null) throw new RuntimeException("tvProductName NULL");
        if (cardTrustScore == null) throw new RuntimeException("cardTrustScore NULL");

        // 🔥 Intent Data
        Intent intent = getIntent();
        String productName = intent.getStringExtra("product_name");
        double productPrice = intent.getDoubleExtra("product_price", -1);

        if (productName == null || productPrice <= 0) {
            finish();
            return;
        }

        tvProductName.setText(productName);
        tvProductPrice.setText("₹" + productPrice);

        DBHelper dbHelper = new DBHelper(this);

        // ---------------- DATA ----------------

        List<Double> rawPrices = dbHelper.getPricesForProduct(productName);
        final List<Double> historicalPricesFinal = new ArrayList<>();

        if (rawPrices != null) {
            for (Double price : rawPrices) {
                if (price != null && price > 0) {
                    historicalPricesFinal.add(price);
                }
            }
        }

        // fallback
        if (historicalPricesFinal.size() < 3) {
            historicalPricesFinal.add(productPrice * 0.9);
            historicalPricesFinal.add(productPrice);
            historicalPricesFinal.add(productPrice * 1.1);
        }

        // 🔥 Charts + Stats
        setupChart(historicalPricesFinal);
        showStats(historicalPricesFinal, productPrice);

        // ---------------- LOGIC ----------------

        double localScore =
                TrustScoreMapper.calculateTrustScore(productPrice, historicalPricesFinal);

        progressTrust.setVisibility(View.VISIBLE);
        tvTrustScore.setVisibility(View.INVISIBLE);

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    // ---------------- UI ----------------

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

    // ---------------- LINE CHART ----------------

    private void setupChart(List<Double> prices) {

        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < prices.size(); i++) {
            entries.add(new Entry(i, prices.get(i).floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Price Trend");

        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        priceChart.setData(lineData);

        Description desc = new Description();
        desc.setText("");
        priceChart.setDescription(desc);

        priceChart.getAxisRight().setEnabled(false);
        priceChart.getXAxis().setDrawGridLines(false);
        priceChart.getAxisLeft().setDrawGridLines(false);

        priceChart.invalidate();
    }

    // ---------------- STATS ----------------

    private void showStats(List<Double> prices, double currentPrice) {

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;

        for (double p : prices) {
            min = Math.min(min, p);
            max = Math.max(max, p);
            sum += p;
        }

        double avg = sum / prices.size();
        double diff = currentPrice - avg;

        tvAvg.setText("₹" + (int) avg);
        tvMin.setText("₹" + (int) min);
        tvMax.setText("₹" + (int) max);

        tvDiff.setText("Diff: ₹" + (int) diff);

        // color logic
        if (diff > 0) {
            tvDiff.setTextColor(ContextCompat.getColor(this, R.color.trust_red));
        } else {
            tvDiff.setTextColor(ContextCompat.getColor(this, R.color.trust_green));
        }

        setupBarChart(min, avg, max);
    }

    // ---------------- BAR CHART ----------------

    private void setupBarChart(double min, double avg, double max) {

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) min));
        entries.add(new BarEntry(1, (float) avg));
        entries.add(new BarEntry(2, (float) max));

        BarDataSet dataSet = new BarDataSet(entries, "Stats");
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);

        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }
}