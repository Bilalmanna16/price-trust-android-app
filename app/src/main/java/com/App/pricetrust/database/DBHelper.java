package com.App.pricetrust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "PriceTrust.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "prices";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product TEXT, " +
                "price REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 🔥 INSERT METHOD (THIS WAS MISSING)
    public void insertPrice(String product, double price) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("product", product);
        values.put("price", price);

        db.insert(TABLE_NAME, null, values);
    }

    // 🔥 GET HISTORY
    public List<String> getAllPriceEntries() {

        List<String> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String product = cursor.getString(1);
                double price = cursor.getDouble(2);

                list.add(product + " - ₹" + price);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    // 🔥 GET PRICES FOR CHART
    public List<Double> getPricesForProduct(String productName) {

        List<Double> prices = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT price FROM " + TABLE_NAME + " WHERE product LIKE ?",
                new String[]{"%" + productName + "%"}
        );

        if (cursor.moveToFirst()) {
            do {
                prices.add(cursor.getDouble(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return prices;
    }

    public void deleteEntry(String product, double price) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(
                TABLE_NAME,
                "product = ? AND price = ?",
                new String[]{product, String.valueOf(price)}
        );
    }
}