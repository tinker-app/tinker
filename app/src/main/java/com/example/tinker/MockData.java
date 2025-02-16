package com.example.tinker;

import java.util.ArrayList;
import java.util.List;

public class MockData {
    public static List<Product> getMockProducts(String category) {
        List<Product> products = new ArrayList<>();
        switch (category) {
            case "mobile":
                products.add(new Product("p1", "Samsung Galaxy S23", "mobile", "Samsung", 799.99, 2023, "img_url", "amazon_link"));
                products.add(new Product("p2", "iPhone 14", "mobile", "Apple", 999.99, 2022, "img_url", "amazon_link"));
                break;
            case "laptop":
                products.add(new Product("p3", "MacBook Pro 16", "laptop", "Apple", 2499.99, 2023, "img_url", "amazon_link"));
                break;
            // Add more...
        }
        return products;
    }
}
