package com.example.tinker;

import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class SVDMatchingAlgorithm implements MatchingAlgorithm {
    private final RealMatrix U; // SVD latent factors
    private RealVector userVector;
    private List<ProductSerializable> candidateProducts;
    private int num_likes = 0;
    private final double alpha = 0.4; // SVD weight
    private final double beta = 0.3;  // Dislike penalty
    private final double[] featureWeights = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
    private final double[] featurePreferences = new double[6];
    private final double learningRate = 0.1;
    private final Map<String, Integer> productNameToRowIndex = new HashMap<>();
    private final List<ProductSerializable> cart = new ArrayList<>();
    private final double lambda = 0.5; // MMR relevance-diversity tradeoff

    public SVDMatchingAlgorithm(List<ProductSerializable> candidateProducts) {
        RealMatrix m = MatrixUtils.createRealMatrix(getProductFeatureMatrix(candidateProducts));
        SingularValueDecomposition svd = new SingularValueDecomposition(m);
        this.U = svd.getU();

        this.candidateProducts = candidateProducts;
        this.userVector = new ArrayRealVector(U.getColumnDimension());

        for (int i = 0; i < candidateProducts.size(); i++) {
            productNameToRowIndex.put(candidateProducts.get(i).getName(), i);
        } initializeFeaturePreferences();
    }

    @Override
    public void handleSwipe(ProductSerializable product, boolean isRightSwipe) {
        Integer productIndex = productNameToRowIndex.get(product.getName());
        if (productIndex == null){
            return;
        }

        // Update SVD vector
        RealVector productVector = U.getRowVector(productIndex);
        if (isRightSwipe) {
            cart.add(product);
            updateUserVectorForLike(productVector);
        } else {
            userVector = userVector.subtract(productVector.mapMultiply(beta));
        }
        updateFeatureWeights(product, isRightSwipe);
        updateRecommendations();
    }

    @Override
    public ProductSerializable getRecommendedProduct() {
        return candidateProducts.get(0);
    }

    public static double[][] getProductFeatureMatrix(List<ProductSerializable> products) {
        int numProducts = products.size();
        double[][] matrix = new double[numProducts][6];
        for (int i = 0; i < numProducts; i++) matrix[i] = products.get(i).getAttributes();
        return matrix;
    }

    private void initializeFeaturePreferences() {
        double[] defaults = {0.7, 0.5, 0.6, 0.5, 0.7, 0.6};
        System.arraycopy(defaults, 0, featurePreferences, 0, defaults.length);
    }

    private void updateFeatureWeights(ProductSerializable product, boolean isRightSwipe) {
        double[] productFeatures = product.getAttributes();
        for (int i = 0; i < 6; i++) {
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
        } num_likes++;
    }

    private void updateRecommendations() {
        List<ProductSerializable> sortedByRelevance = candidateProducts.stream()
                .sorted((a, b) -> Double.compare(calculateScore(b), calculateScore(a)))
                .collect(Collectors.toList());

        List<ProductSerializable> mmrSelected = new ArrayList<>();
        while (!sortedByRelevance.isEmpty() && mmrSelected.size() < candidateProducts.size()) {
            ProductSerializable bestCandidate = null;
            double bestMMRScore = Double.NEGATIVE_INFINITY;

            for (ProductSerializable candidate : sortedByRelevance) {
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
    }

    private double calculateScore(ProductSerializable product) {
        Integer productIndex = productNameToRowIndex.get(product.getName());
        if (productIndex == null) return 0;

        RealVector productVector = U.getRowVector(productIndex);
        double[] features = product.getAttributes();

        double svdScore = userVector.dotProduct(productVector);
        double featureScore = 0;

        for (int i = 0; i < 6; i++) {
            featureScore += featureWeights[i] * (1 - Math.abs(features[i] - featurePreferences[i]));
        }

        double baseScore = alpha * svdScore + (1 - alpha) * featureScore;

        if (cart.contains(product)) {
            baseScore *= 0.7;
        } return baseScore;
    }

    private RealVector getProductVector(ProductSerializable product) {
        Integer productIndex = productNameToRowIndex.get(product.getName());
        return (productIndex != null) ? U.getRowVector(productIndex) : new ArrayRealVector(U.getColumnDimension());
    }

    private double cosineSimilarity(RealVector v1, RealVector v2) {
        double dotProduct = v1.dotProduct(v2);
        double norm1 = v1.getNorm();
        double norm2 = v2.getNorm();
        return (norm1 == 0 || norm2 == 0) ? 0 : dotProduct / (norm1 * norm2);
    }
}
