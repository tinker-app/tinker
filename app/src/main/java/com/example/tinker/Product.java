package com.example.tinker;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;

public class Product {
    private final String name, image_url, product_url;
    private final double[] attributes;   // 7-dimensional Vector

    public String getName() { return name; }

    public String getImageUrl() { return image_url; }

    public String getProductUrl() { return product_url; }

    public double[] getAttributes() { return attributes; }

    public Product(DocumentSnapshot documentSnapshot) {
        this.name = documentSnapshot.getString("name");
        this.product_url = documentSnapshot.getString("product_url");
        this.image_url = documentSnapshot.getString("image_url");
        Log.d("Product", "Product: " + name);

        // Populate Attributes Vector
        attributes = new double[7];
        attributes[0] = documentSnapshot.getDouble("brand");
        attributes[1] = documentSnapshot.getDouble("price");
        attributes[2] = documentSnapshot.getDouble("rating");
        attributes[3] = documentSnapshot.getDouble("ram");
        attributes[4] = documentSnapshot.getDouble("storage");
        attributes[5] = documentSnapshot.getDouble("size");
        attributes[6] = documentSnapshot.getDouble("weight");
    }
}