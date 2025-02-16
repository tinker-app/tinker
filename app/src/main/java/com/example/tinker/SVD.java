package com.example.tinker;

import org.apache.commons.math3.linear.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SVD {
    private Map<String, RealMatrix> uMatrices = new HashMap<>(); // Key: category

    public void precomputeSVD() {
        String[] categories = {"mobile", "laptop", "headphone"};
        for (String category : categories) {
            List<Product> products = MockData.getMockProducts(category);
            double[][] featureMatrix = DataProcessor.getProductFeatureMatrix(products);
            RealMatrix A = MatrixUtils.createRealMatrix(featureMatrix);
            SingularValueDecomposition svd = new SingularValueDecomposition(A);
            RealMatrix U = svd.getU(); // Left singular vectors
            uMatrices.put(category, U);
        }
    }

    public RealMatrix getUForCategory(String category) {
        return uMatrices.get(category);
    }
}
