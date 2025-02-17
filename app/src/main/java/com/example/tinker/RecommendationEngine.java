package com.example.tinker;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendationEngine {
    private final RealMatrix U; // SVD latent factors
    private RealVector userVector;
    private List<Product> candidateProducts;
    private int num_likes = 0;
    private double alpha = 0.7; // SVD weight
    private double beta = 0.3;  // Dislike penalty
    private final double[] featureWeights = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
    private final double[] featurePreferences = new double[6];
    private final double learningRate = 0.1;


    public RecommendationEngine(RealMatrix U, List<Product> candidateProducts) {
        this.U = U;
        this.candidateProducts = candidateProducts;
        this.userVector = new ArrayRealVector(U.getColumnDimension());

        initializeFeaturePreferences();
    }

    private void initializeFeaturePreferences() {
        if (candidateProducts.isEmpty()) {
            Arrays.fill(featurePreferences, 0.5);
            return;
        }

        double[] sums = new double[6];
        for (Product p : candidateProducts) {
            double[] features = p.getAttributes();
            for (int i = 0; i < 6; i++) sums[i] += features[i];
        }

        for (int i = 0; i < 6; i++) featurePreferences[i] = sums[i] / candidateProducts.size();
    }

    public void handleSwipe(Product product, boolean isRightSwipe) {
        int productIndex = candidateProducts.indexOf(product);
        if (productIndex == -1) return;

        // Update SVD vector
        RealVector productVector = U.getRowVector(productIndex);
        if (isRightSwipe) updateUserVectorForLike(productVector);
        else userVector = userVector.subtract(productVector.mapMultiply(beta));

        updateFeatureWeights(product, isRightSwipe);
        updateRecommendations();
    }

    private void updateFeatureWeights(Product product, boolean isRightSwipe) {
        double[] productFeatures = product.getAttributes();
        for (int i = 0; i < 6; i++) {
            double featureDiff = Math.abs(productFeatures[i] - featurePreferences[i]);
            double similarity = 1 - featureDiff;
            double adjustment = learningRate * (isRightSwipe ? similarity : -similarity);
            featureWeights[i] = Math.max(0, Math.min(1, featureWeights[i] + adjustment));

            if (isRightSwipe) featurePreferences[i] = (featurePreferences[i] + productFeatures[i]) / 2;
        }
    }

    private void updateUserVectorForLike(RealVector productVector) {
        if (num_likes == 0) userVector = productVector;
        else {
            userVector = userVector.mapMultiply(num_likes)
                    .add(productVector)
                    .mapDivide(num_likes + 1);
        } num_likes++;
    }

    private void updateRecommendations() {
        candidateProducts = candidateProducts.stream()
                .sorted((a, b) -> Double.compare(calculateScore(b), calculateScore(a)))
                .collect(Collectors.toList());
    }

    private double calculateScore(Product product) {
        int productIndex = candidateProducts.indexOf(product);
        RealVector productVector = U.getRowVector(productIndex);
        double[] features = product.getAttributes();

        double svdScore = userVector.dotProduct(productVector);

        double featureScore = 0;
        for (int i = 0; i < 6; i++)
            featureScore += featureWeights[i] * (1 - Math.abs(features[i] - featurePreferences[i]));

        return alpha * svdScore + (1 - alpha) * featureScore;
    }

    public Product getRecommendedProduct() {
        return candidateProducts.get(0);
    }
}