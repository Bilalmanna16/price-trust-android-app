package com.App.pricetrust.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;


public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PriceTrust.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_TABLE =
                "CREATE TABLE " + PriceContract.PriceEntry.TABLE_NAME + " (" +
                        PriceContract.PriceEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PriceContract.PriceEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                        PriceContract.PriceEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                        PriceContract.PriceEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
                        ");";

        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PriceContract.PriceEntry.TABLE_NAME);
        onCreate(db);
    }

    public List<String> getAllPriceEntries() {

        List<String> priceList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                PriceContract.PriceEntry.COLUMN_PRODUCT_NAME,
                PriceContract.PriceEntry.COLUMN_PRICE,
                PriceContract.PriceEntry.COLUMN_TIMESTAMP
        };

        Cursor cursor = db.query(
                PriceContract.PriceEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                PriceContract.PriceEntry.COLUMN_TIMESTAMP + " DESC"
        );

        while (cursor.moveToNext()) {
            String productName = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            PriceContract.PriceEntry.COLUMN_PRODUCT_NAME));

            double price = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(
                            PriceContract.PriceEntry.COLUMN_PRICE));

            priceList.add(productName + " - ₹" + price);
        }

        cursor.close();
        db.close();

        return priceList;
    }
}
