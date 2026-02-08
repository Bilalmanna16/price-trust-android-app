package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.App.pricetrust.R;
import com.App.pricetrust.database.DBHelper;
import com.App.pricetrust.ml.TrustScoreMapper;
import java.util.List;


public class ResultActivity extends AppCompatActivity {

    private TextView tvProductName, tvProductPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Bind views
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);

        // Receive data from MainActivity
        Intent intent = getIntent();
        String productName = intent.getStringExtra("product_name");
        double productPrice = intent.getDoubleExtra("product_price", 0.0);

        // Display data
        tvProductName.setText("Product: " + productName);
        tvProductPrice.setText("Price: ₹" + productPrice);

        DBHelper dbHelper = new DBHelper(this);
        List<Double> historicalPrices =
                dbHelper.getPricesForProduct(productName);

        double trustScore = TrustScoreMapper.calculateTrustScore(
                productPrice,
                historicalPrices
        );
        TextView tvTrustScore = findViewById(R.id.tvTrustScore);
        tvTrustScore.setText("Trust Score: " + trustScore);
    }
}
