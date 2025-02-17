package com.example.tinker;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;

public class Product {
    private String name, image_url, product_url;
    private double price;
    private final double[] attributes;   // 7-dimensional Vector

    public String getName() { return name; }

    public String getImageUrl() { return image_url; }

    public String getProductUrl() { return product_url; }
    public double getPrice() { return price; }

    public double[] getAttributes() { return attributes; }

    public Product(DocumentSnapshot documentSnapshot) {
        this.name = documentSnapshot.getString("name");
        this.product_url = documentSnapshot.getString("product_url");
        this.image_url = documentSnapshot.getString("image_url");
        this.price = documentSnapshot.getDouble("actual_price");

        // Populate Attributes Vector
        attributes = new double[6];
        attributes[0] = documentSnapshot.getDouble("brand");
        attributes[1] = documentSnapshot.getDouble("price");
        attributes[2] = documentSnapshot.getDouble("rating");
        attributes[3] = documentSnapshot.getDouble("ram");
        attributes[4] = documentSnapshot.getDouble("storage");
        attributes[5] = documentSnapshot.getDouble("size");
    }
}