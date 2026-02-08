package com.App.pricetrust.ml;

import java.util.List;

public class TrustScoreMapper {

    public static double calculateTrustScore(
            double currentPrice,
            List<Double> historicalPrices
    ) {

        // Not enough data → neutral trust
        if (historicalPrices == null || historicalPrices.size() < 3) {
            return 70.0;
        }

        double mean = calculateMean(historicalPrices);
        double stdDev = calculateStdDev(historicalPrices, mean);

        // Avoid division by zero
        if (stdDev == 0) {
            return 70.0;
        }

        double zScore = Math.abs((currentPrice - mean) / stdDev);

        // Map z-score to trust score
        if (zScore < 1) {
            return 90.0;
        } else if (zScore < 2) {
            return 65.0;
        } else {
            return 30.0;
        }
    }

    private static double calculateMean(List<Double> values) {
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    private static double calculateStdDev(List<Double> values, double mean) {
        double sum = 0;
        for (double v : values) {
            sum += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sum / values.size());
    }
}
