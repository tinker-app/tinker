package com.example.tinker;

import android.util.Log;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecommendationEngine {
    private final RealMatrix U; // SVD latent factors
    private RealVector userVector;
    private List<Product> candidateProducts;
    private int num_likes = 0;
    private double alpha = 0.4; // SVD weight
    private double beta = 0.3;  // Dislike penalty
    private final double[] featureWeights = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
    private final double[] featurePreferences = new double[7];
    private final double learningRate = 0.1;
    private final Map<String, Integer> productNameToRowIndex = new HashMap<>();
    private List<Product> cart = new ArrayList<>();
    private final double lambda = 0.5; // MMR relevance-diversity tradeoff

    public RecommendationEngine(RealMatrix U, List<Product> candidateProducts) {
        this.U = U;
        this.candidateProducts = candidateProducts;
        this.userVector = new ArrayRealVector(U.getColumnDimension());

        for (int i = 0; i < candidateProducts.size(); i++) {
            productNameToRowIndex.put(candidateProducts.get(i).getName(), i);
            Log.e("ERROR", "Null product name at index: " + i);

        }
        Log.d("DEBUG", "Product index mapping: " + productNameToRowIndex);

        initializeFeaturePreferences();
    }

    private void initializeFeaturePreferences() {
        double[] defaults = {0.7, 0.5, 0.6, 0.5, 0.7, 0.6, 0.5};
        System.arraycopy(defaults, 0, featurePreferences, 0, defaults.length);
    }

    public void handleSwipe(Product product, boolean isRightSwipe) {
        Log.d("DEBUG", "handleSwipe() called for product: " + product.getName() + ", Right Swipe: " + isRightSwipe);
        Integer productIndex = productNameToRowIndex.get(product.getName());
        if (productIndex == null){  Log.e("ERROR", "Product index not found for: " + product.getName());
            return;}


        // Update SVD vector
        RealVector productVector = U.getRowVector(productIndex);
        if (isRightSwipe) {
            cart.add(product);
            updateUserVectorForLike(productVector);
        } else {
            userVector = userVector.subtract(productVector.mapMultiply(beta));


        }

        updateFeatureWeights(product, isRightSwipe);
        Log.d("DEBUG", "Before calling updateRecommendations()");
        updateRecommendations();
        Log.d("DEBUG", "After calling updateRecommendations()");
    }

    private void updateFeatureWeights(Product product, boolean isRightSwipe) {
        double[] productFeatures = product.getAttributes();
        for (int i = 0; i < 7; i++) {
            double featureDiff = Math.abs(productFeatures[i] - featurePreferences[i]);
            double similarity = 1 - featureDiff;
            double adjustment = learningRate * (isRightSwipe ? similarity : -similarity);
            featureWeights[i] = Math.max(0.1, Math.min(0.9, featureWeights[i] + adjustment));

            if (isRightSwipe) featurePreferences[i] = (featurePreferences[i] + productFeatures[i]) / 2;
        }
    }

    private void updateUserVectorForLike(RealVector productVector) {
        if (num_likes == 0) userVector = productVector;
        else {
            userVector = userVector.mapMultiply(num_likes)
                    .add(productVector)
                    .mapDivide(num_likes + 1);
        }
        num_likes++;
    }

    private void updateRecommendations() {
        Log.d("CANDIDATES", "Before update: " + candidateProducts.size());
        List<Product> sortedByRelevance = candidateProducts.stream()
                .sorted((a, b) -> Double.compare(calculateScore(b), calculateScore(a)))
                .collect(Collectors.toList());

        List<Product> mmrSelected = new ArrayList<>();
        while (!sortedByRelevance.isEmpty() && mmrSelected.size() < candidateProducts.size()) {
            Product bestCandidate = null;
            double bestMMRScore = Double.NEGATIVE_INFINITY;

            for (Product candidate : sortedByRelevance) {
                double relevance = calculateScore(candidate);
                double diversity = mmrSelected.stream()
                        .mapToDouble(p -> cosineSimilarity(getProductVector(candidate), getProductVector(p)))
                        .average().orElse(0);

                double mmrScore = lambda * relevance - (1 - lambda) * diversity;
                if (mmrScore > bestMMRScore) {
                    bestMMRScore = mmrScore;
                    bestCandidate = candidate;
                }
            }

            if (bestCandidate != null) {
                mmrSelected.add(bestCandidate);
                sortedByRelevance.remove(bestCandidate);
            }
        }
        candidateProducts = mmrSelected;
        Log.d("MMR", "Top 5: " + mmrSelected.subList(0, Math.min(5, mmrSelected.size())).stream()
                .map(Product::getName)
                .collect(Collectors.toList()));
        Log.d("CANDIDATES", "Before update: " + candidateProducts.size());
    }

    private double calculateScore(Product product) {
        Integer productIndex = productNameToRowIndex.get(product.getName());
        if (productIndex == null) return 0;

        RealVector productVector = U.getRowVector(productIndex);
        double[] features = product.getAttributes();

        double svdScore = userVector.dotProduct(productVector);
        double featureScore = 0;

        for (int i = 0; i < 7; i++) {
            featureScore += featureWeights[i] * (1 - Math.abs(features[i] - featurePreferences[i]));
        }

        double baseScore = alpha * svdScore + (1 - alpha) * featureScore;


        if (cart.contains(product)) {
            baseScore *= 0.7;
        }

        return baseScore;
    }

    private RealVector getProductVector(Product product) {
        Integer productIndex = productNameToRowIndex.get(product.getName());
        return (productIndex != null) ? U.getRowVector(productIndex) : new ArrayRealVector(U.getColumnDimension());
    }

    private double cosineSimilarity(RealVector v1, RealVector v2) {
        double dotProduct = v1.dotProduct(v2);
        double norm1 = v1.getNorm();
        double norm2 = v2.getNorm();
        return (norm1 == 0 || norm2 == 0) ? 0 : dotProduct / (norm1 * norm2);
    }

    public Product getRecommendedProduct() {
        return candidateProducts.get(0);
    }
}
