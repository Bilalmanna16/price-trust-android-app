package com.App.pricetrust.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.App.pricetrust.R;
import com.App.pricetrust.database.DBHelper;
import com.App.pricetrust.database.PriceContract;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout tilProductName, tilProductPrice;
    private TextInputEditText etProductName, etProductPrice;
    private MaterialButton btnAnalyze, btnHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_Pricetrust);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        tilProductName = findViewById(R.id.tilProductName);
        tilProductPrice = findViewById(R.id.tilProductPrice);

        etProductName = findViewById(R.id.etProductName);
        etProductPrice = findViewById(R.id.etProductPrice);

        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnHistory = findViewById(R.id.btnHistory);

        btnAnalyze.setOnClickListener(v -> handleAnalyze());

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );
    }

    private void handleAnalyze() {

        tilProductName.setError(null);
        tilProductPrice.setError(null);

        String productName = etProductName.getText().toString().trim();
        String priceText = etProductPrice.getText().toString().trim();

        if (TextUtils.isEmpty(productName)) {
            tilProductName.setError("Enter product name");
            return;
        }

        String[] allowedProducts = {
                "shoes","laptop","phone","mobile","tablet","watch",
                "headphones","earphones","bag","camera","tv",
                "keyboard","mouse","monitor","printer"
        };

        boolean validProduct = false;

        for(String p : allowedProducts){
            if(productName.toLowerCase().contains(p)){
                validProduct = true;
                break;
            }
        }

        if(!validProduct){
            tilProductName.setError("Enter a real product (e.g. shoes, laptop)");
            return;
        }

        if (TextUtils.isEmpty(priceText)) {
            tilProductPrice.setError("Enter price");
            return;
        }

        double price;

        try {
            price = Double.parseDouble(priceText);
        } catch (Exception e) {
            tilProductPrice.setError("Invalid price");
            return;
        }

        if (price < 100) {
            tilProductPrice.setError("Price must be ≥ ₹100");
            return;
        }

        try {

            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(PriceContract.PriceEntry.COLUMN_PRODUCT_NAME, productName);
            values.put(PriceContract.PriceEntry.COLUMN_PRICE, price);
            values.put(PriceContract.PriceEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());

            db.insert(PriceContract.PriceEntry.TABLE_NAME, null, values);
            db.close();

        } catch (Exception ignored) {}

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("product_name", productName);
        intent.putExtra("product_price", price);

        startActivity(intent);
    }
}