package com.App.pricetrust.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
}
