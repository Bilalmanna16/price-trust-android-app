package com.App.pricetrust.network;

import com.App.pricetrust.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ApiClient {

    public static JSONObject callMLApi(
            String productName,
            double currentPrice,
            List<Double> historicalPrices
    ) throws Exception {

        URL url = new URL(Constants.ML_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Build JSON request
        JSONObject requestJson = new JSONObject();
        requestJson.put("product_name", productName);
        requestJson.put("current_price", currentPrice);

        JSONArray historyArray = new JSONArray();
        for (double price : historicalPrices) {
            historyArray.put(price);
        }
        requestJson.put("historical_prices", historyArray);

        // Send request
        OutputStream os = conn.getOutputStream();
        os.write(requestJson.toString().getBytes());
        os.flush();
        os.close();

        // Read response
        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        br.close();
        conn.disconnect();

        return new JSONObject(response.toString());
    }
}
