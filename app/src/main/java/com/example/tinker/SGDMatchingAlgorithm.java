package com.example.tinker;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import java.util.ArrayList;
import java.util.List;

public class SGDMatchingAlgorithm implements MatchingAlgorithm{
    private double learning_rate = 0.2;
    private RealVector estimate_vector;
    private final List<ProductSerializable> products;
    private ArrayList<RealVector> product_vectors;

    public SGDMatchingAlgorithm(List<ProductSerializable> products) {
        this.products = products;
        estimate_vector = new ArrayRealVector(6);
        product_vectors = new ArrayList<>();

        for (ProductSerializable product: products)
            product_vectors.add(new ArrayRealVector(product.getAttributes()));

    }

    @Override
    public void handleSwipe(ProductSerializable product, boolean isRightSwipe) {
        RealVector product_vector = new ArrayRealVector(product.getAttributes());

        int sign =  isRightSwipe ? 1 : -1;
        if (isRightSwipe) {
            int index = products.indexOf(product);
            products.remove(index);
            products.remove(index);
        }

        estimate_vector = estimate_vector
                                .add(product_vector.subtract(estimate_vector)
                                .mapMultiply(sign)
                                .mapMultiply(learning_rate));

        estimate_vector = estimate_vector.mapDivide(estimate_vector.getNorm());
    }

    @Override
    public ProductSerializable getRecommendedProduct() {
        int min_index = 0;
        double min = 0;
        for (int index = 0; index < product_vectors.size(); index++) {
            double dot = product_vectors.get(index).dotProduct(estimate_vector);
            if (dot < min) {
                min = dot;
                min_index = index;
            }
        } return products.get(min_index);
    }
}
