package com.example.tinker;
public class Product {
    private String id, name, category , imageUrl, productUrl;
    private double price;
    private double storage; // In GB
    private double ram;     // In GB
    private double rating;  // 0-5
    private double size;    // In inches
    private double weight;
    double brand;

    public Product(String id, String name, String category, double brand,
                   double price, double storage, double ram, double rating,
                   double size, double weight, String imageUrl, String productUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.storage = storage;
        this.ram = ram;
        this.rating = rating;
        this.size = size;
        this.weight = weight;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getBrand() { return brand; }
    public void setBrand(double brand) { this.brand = brand; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getStorage() { return storage; }
    public void setStorage(double storage) { this.storage = storage; }
    public double getRam() { return ram; }
    public void setRam(double ram) { this.ram = ram; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public double getSize() { return size; }
    public void setSize(double size) { this.size = size; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
}