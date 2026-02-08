package com.App.pricetrust.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.App.pricetrust.R;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.App.pricetrust.database.DBHelper;
import com.App.pricetrust.database.PriceContract;


public class MainActivity extends AppCompatActivity {

    private EditText etProductName, etProductPrice;
    private Button btnAnalyze, btnHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI elements
        etProductName = findViewById(R.id.etProductName);
        etProductPrice = findViewById(R.id.etProductPrice);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnHistory = findViewById(R.id.btnHistory);

        // Analyze button click
        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAnalyze();
            }
        });

        // History button click
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistoryScreen();
            }
        });
    }

    private void handleAnalyze() {
        String productName = etProductName.getText().toString().trim();
        String priceText = etProductPrice.getText().toString().trim();

        if (productName.isEmpty() || priceText.isEmpty()) {
            Toast.makeText(this, "Please enter product name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save price entry to SQLite
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PriceContract.PriceEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(PriceContract.PriceEntry.COLUMN_PRICE, price);
        values.put(PriceContract.PriceEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());

        db.insert(PriceContract.PriceEntry.TABLE_NAME, null, values);
        db.close();

        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("product_name", productName);
        intent.putExtra("product_price", price);
        startActivity(intent);
    }

    private void openHistoryScreen() {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        startActivity(intent);
    }
}
