package com.example.tinker;

import java.util.List;

public class DataProcessor {
    public static double[][] getProductFeatureMatrix(List<Product> products) {
        int numProducts = products.size();
        double[][] matrix = new double[numProducts][5];

        for (int i = 0; i < numProducts; i++) {
            Product p = products.get(i);

            matrix[i][0] = p.getPrice();
            matrix[i][1] = p.getRam();
            matrix[i][2] = p.getRating();
            matrix[i][3] = p.getSize();
            matrix[i][4] = p.getBrand();
        }

        return matrix;
    }
}