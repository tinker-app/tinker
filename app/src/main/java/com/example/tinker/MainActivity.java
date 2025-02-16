package com.example.tinker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Home layout views
    private LinearLayout homeLayout;
    private Button buttonLaptops, buttonPhones, buttonTablets;

    // Product swipe layout views
    private TextView productName;
    private TextView productDetails;
    private TextView productPrice;
    private ImageView productImage;
    private CardView cardView;
    private GestureDetector gestureDetector;

    // Product data
    private List<Product> productList;
    private int currentProductIndex = 0;
    private String currentCategory = "";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initializeViews();

        // Initialize product list (replace with API/database data later)
        initializeProductList();

        // Initialize swipe gesture detector
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());
        cardView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        // Set click listeners for category buttons
        buttonLaptops.setOnClickListener(v -> {
            currentCategory = "laptops";
            switchToProductView();
        });
        buttonPhones.setOnClickListener(v -> {
            currentCategory = "phones";
            switchToProductView();
        });
        buttonTablets.setOnClickListener(v -> {
            currentCategory = "tablets";
            switchToProductView();
        });

        // Load the first product
        loadProduct(currentProductIndex);
    }

    private void initializeViews() {
        homeLayout = findViewById(R.id.homeLayout);
        buttonLaptops = findViewById(R.id.buttonLaptops);
        buttonPhones = findViewById(R.id.buttonPhones);
        buttonTablets = findViewById(R.id.buttonTablets);
        productName = findViewById(R.id.productName);
        productDetails = findViewById(R.id.productDetails);
        productPrice = findViewById(R.id.productPrice);
        productImage = findViewById(R.id.productImage);
        cardView = findViewById(R.id.cardView);
    }

    private void initializeProductList() {
        productList = new ArrayList<>();

        // Sample data using the new Product class structure
        productList.add(new Product(
                "laptop1",
                "ThinkPad X1 Carbon",
                "Laptop",
                "Lenovo",
                1299.99,
                2024,
                "laptop1.png",
                "https://example.com/products/thinkpad-x1"
        ));

        productList.add(new Product(
                "phone1",
                "Galaxy S24 Ultra",
                "Phone",
                "Samsung",
                1199.99,
                2024,
                "https://example.com/galaxy-s24.jpg",
                "https://example.com/products/galaxy-s24"
        ));

        productList.add(new Product(
                "tablet1",
                "iPad Pro 12.9",
                "Tablet",
                "Apple",
                1099.99,
                2024,
                "https://example.com/ipad-pro.jpg",
                "https://example.com/products/ipad-pro"
        ));
    }

    private void switchToProductView() {
        homeLayout.setVisibility(View.GONE);
        cardView.setVisibility(View.VISIBLE);
        // Filter products by category and reset index
        currentProductIndex = 0;
        loadProduct(currentProductIndex);
    }

    private void loadProduct(int index) {
        List<Product> filteredProducts = getFilteredProducts();

        if (index >= 0 && index < filteredProducts.size()) {
            Product product = filteredProducts.get(index);
            productName.setText(product.getName());

            // Combine product details into a single string
            String details = String.format("%s\nBrand: %s\nCategory: %s\nRelease Year: %d",
                    product.getName(),
                    product.getBrand(),
                    product.getCategory(),
                    product.getReleaseYear());
            productDetails.setText(details);

            // Format price with two decimal places
            productPrice.setText(String.format("$%.2f", product.getPrice()));

            // Load image using Glide
            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.laptop1)
                    .into(productImage);
        } else {
            Toast.makeText(this, "No more products available", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Product> getFilteredProducts() {
        if (currentCategory.isEmpty()) {
            return productList;
        }

        List<Product> filtered = new ArrayList<>();
        for (Product product : productList) {
            if (product.getCategory().equals(currentCategory)) {
                filtered.add(product);
            }
        }
        return filtered;
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private void onSwipeRight() {
        List<Product> filteredProducts = getFilteredProducts();
        if (!filteredProducts.isEmpty()) {
            animateSwipe(1000f);
            currentProductIndex = (currentProductIndex - 1 + filteredProducts.size()) % filteredProducts.size();
            loadProduct(currentProductIndex);
        }
    }

    private void onSwipeLeft() {
        List<Product> filteredProducts = getFilteredProducts();
        if (!filteredProducts.isEmpty()) {
            animateSwipe(-1000f);
            currentProductIndex = (currentProductIndex + 1) % filteredProducts.size();
            loadProduct(currentProductIndex);
        }
    }

    private void animateSwipe(float translationX) {
        cardView.animate()
                .translationX(translationX)
                .rotation(translationX > 0 ? 15 : -15)
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    cardView.setTranslationX(0);
                    cardView.setRotation(0);
                    cardView.setAlpha(1f);
                })
                .start();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentProductIndex", currentProductIndex);
        outState.putString("currentCategory", currentCategory);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentProductIndex = savedInstanceState.getInt("currentProductIndex");
        currentCategory = savedInstanceState.getString("currentCategory");
        loadProduct(currentProductIndex);
    }
}