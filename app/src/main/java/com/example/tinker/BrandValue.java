package com.example.tinker;
import java.util.HashMap;
import java.util.Map;

public class BrandValue {

    private static final Map<String, Double> staticScores = new HashMap<>();
    static {
        staticScores.put("Samsung", 0.9);
        staticScores.put("Apple", 1.0);
        staticScores.put("Sony", 0.8);
        staticScores.put("Xiaomi", 0.6);
        // Add more brands...
    }


    private static final Map<String, Integer> brandRightSwipes = new HashMap<>();
    private static final Map<String, Integer> brandTotalSwipes = new HashMap<>();
    private static final double alpha = 0.7; // Weight for static vs. dynamic

    // Update brand score based on user swipe
    public static void updateBrandScore(String brand, boolean isRightSwipe) {
        brandTotalSwipes.merge(brand, 1, Integer::sum);


        if (isRightSwipe) {
            brandRightSwipes.merge(brand, 1, Integer::sum);
        }
    }

    // Get combined score (static + dynamic)
    public static double getScore(String brand) {
        staticScores.merge(brand, 0.3, (existing, newValue) -> existing);
        double staticScore = staticScores.get(brand);


        brandTotalSwipes.merge(brand, 1, Integer::sum);
        brandRightSwipes.merge(brand, 0, Integer::sum);
        int total = brandTotalSwipes.get(brand);
        int right = brandRightSwipes.get(brand);

        double dynamicScore = total == 0 ? 0 : (double) right / total;
        return alpha * staticScore + (1 - alpha) * dynamicScore;
    }
}