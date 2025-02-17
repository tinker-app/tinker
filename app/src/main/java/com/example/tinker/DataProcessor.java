package com.example.tinker;

import java.util.ArrayList;
import java.util.List;

public class DataProcessor {
    public static double[][] getProductFeatureMatrix(List<Product> products) {
        int numProducts = products.size();
        double[][] matrix = new double[numProducts][6];

        for (int i = 0; i < numProducts; i++) matrix[i] = products.get(i).getAttributes();

        return matrix;
    }
}