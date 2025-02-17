package com.example.tinker;

public interface MatchingAlgorithm {
    void handleSwipe(ProductSerializable product, boolean isRightSwipe);
    ProductSerializable getRecommendedProduct();
}
