package com.App.pricetrust.database;

public final class PriceContract {

    private PriceContract() {
        // Prevent instantiation
    }

    public static class PriceEntry {

        public static final String TABLE_NAME = "price_history";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
