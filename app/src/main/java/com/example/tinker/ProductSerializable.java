package com.example.tinker;

import com.google.firebase.firestore.DocumentSnapshot;
import java.io.Serializable;

public class ProductSerializable implements Serializable {
    private final double price;
    private final double[] attributes;   // 7-dimensional Vector
    private final String name, image_url, product_url;

    public String getName() { return name; }
    public String getImageURL() { return image_url; }
    public String getProductURL() { return product_url; }
    public double getPrice() { return price; }
    public double[] getAttributes() { return attributes; }

    public ProductSerializable(DocumentSnapshot documentSnapshot) {
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