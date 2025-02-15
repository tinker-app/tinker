package com.example.tinker;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendationEngine {
    private RealMatrix U; // Latent factors for the current category
    private RealVector userVector;
    private List<Product> candidateProducts;
    private int numLikes = 0;
    private double alpha = 0.7; // SVD vs. price weight
    private double beta = 0.3; // Negative feedback strength
    private double userBudget;
    private double maxPriceDeviation = 500; // Max price difference for full score adjust as needed later hardcoded for now

    public RecommendationEngine(RealMatrix U, List<Product> candidateProducts, double userBudget) {
        this.U = U;
        this.candidateProducts = candidateProducts;
        this.userBudget = userBudget;
        this.userVector = new ArrayRealVector(U.getColumnDimension());
    }

    public void handleSwipe(Product product, boolean isRightSwipe) {
        int productIndex = candidateProducts.indexOf(product);
        if (productIndex == -1) return;

        RealVector productVector = U.getRowVector(productIndex);

        if (isRightSwipe) {
            updateUserVectorForLike(productVector);
        } else {
            userVector = userVector.subtract(productVector.mapMultiply(beta));
        }
        updateRecommendations();
    }

    private void updateUserVectorForLike(RealVector productVector) {
        if (numLikes == 0) {
            userVector = productVector;
        } else {
            userVector = userVector.mapMultiply(numLikes)
                    .add(productVector)
                    .mapDivide(numLikes + 1);
        }
        numLikes++;
    }

    private void updateRecommendations() {
        candidateProducts = candidateProducts.stream()
                .sorted((a, b) -> Double.compare(
                        calculateScore(b), // Sort descending
                        calculateScore(a)
                ))
                .collect(Collectors.toList());
    }

    private double calculateScore(Product product) {
        int productIndex = candidateProducts.indexOf(product);
        RealVector productVector = U.getRowVector(productIndex);

        // SVD score
        double svdScore = userVector.dotProduct(productVector);

        // Price proximity score
        double priceDiff = Math.abs(product.getPrice() - userBudget);
        double priceScore = 1 - (priceDiff / maxPriceDeviation);
        priceScore = Math.max(0, Math.min(1, priceScore));

        return alpha * svdScore + (1 - alpha) * priceScore;
    }

    // Getter for sorted candidate products
    public List<Product> getRecommendedProducts() {
        return candidateProducts;
    }
}
