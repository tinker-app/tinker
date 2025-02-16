package com.example.tinker;

import java.util.List;

public class DataProcessor {
    public static double[][] getProductFeatureMatrix(List<Product> products) {
        int numProducts = products.size();
        double[][] matrix = new double[numProducts][3];

        // Find min/max for normalization
        double minPrice = 500, maxPrice = 2500;
        int minYear = 2020, maxYear = 2023;

        for (int i = 0; i < numProducts; i++) {
            Product p = products.get(i);

            matrix[i][0] = (p.getPrice() - minPrice) / (maxPrice - minPrice);

            matrix[i][1] = (p.getReleaseYear() - minYear) / (double) (maxYear - minYear);

            matrix[i][2] = BrandValue.getScore(p.getBrand());
        }
        return matrix;
    }
}
