package com.example.tinker;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SGDMatchingAlgorithm implements MatchingAlgorithm{
    private double learning_rate = 0.3;
    private RealVector estimate_vector;
    private final List<ProductSerializable> products;
    private ArrayList<RealVector> product_vectors;


    public SGDMatchingAlgorithm(List<ProductSerializable> products) {
        this.products = products;
        Random rand = new Random();
        double[] randomValues = rand.doubles(6, 0, 1).toArray(); // Generates 6 random values in [0,1]
        this.estimate_vector = new ArrayRealVector(randomValues).unitVector(); // Normalize the vector
        product_vectors = new ArrayList<>();

        for (ProductSerializable product: products)
            product_vectors.add(new ArrayRealVector(product.getAttributes()));

    }

    @Override
    public void handleSwipe(ProductSerializable product, boolean isRightSwipe) {
        RealVector product_vector = new ArrayRealVector(product.getAttributes());

        int sign =  isRightSwipe ? 1 : -1;

        int index = products.indexOf(product);
        products.remove(index);
        product_vectors.remove(index);

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
            double dot = product_vectors.get(index).dotProduct(estimate_vector) / (estimate_vector.getNorm() * product_vectors.get(index).getNorm());
            if (dot < min) {
                min = dot;
                min_index = index;
            }
        } return products.get(min_index);
    }
}
