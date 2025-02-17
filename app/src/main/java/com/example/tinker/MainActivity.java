package com.example.tinker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference productsRef;
    private LinearLayout homeLayout;
    private Button buttonLaptops, buttonPhones, buttonTablets;
    private TextView productName, productPrice;
    private ImageView productImage;
    private CardView cardView;
    private GestureDetector gestureDetector;
    private List<Product> productList = new ArrayList<>();
    private int currentProductIndex = 0;
    private String currentCategory = "";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        db = FirebaseFirestore.getInstance();
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        cardView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        buttonLaptops.setOnClickListener(v -> {
            currentCategory = "laptops";
            fetchProductsByCategory();
        });
        buttonPhones.setOnClickListener(v -> {
            currentCategory = "phones";
            fetchProductsByCategory();
        });
        buttonTablets.setOnClickListener(v -> {
            currentCategory = "tablets";
            fetchProductsByCategory();
        });
    }

    private void initializeViews() {
        homeLayout = findViewById(R.id.homeLayout);
        buttonLaptops = findViewById(R.id.buttonLaptops);
        buttonPhones = findViewById(R.id.buttonPhones);
        buttonTablets = findViewById(R.id.buttonTablets);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productImage = findViewById(R.id.productImage);
        cardView = findViewById(R.id.cardView);
    }

    private void fetchProductsByCategory() {
        homeLayout.setVisibility(View.GONE);
        cardView.setVisibility(View.VISIBLE);
        productsRef = db.collection("products");

        productsRef.whereEqualTo("category", currentCategory).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                productList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Product product = document.toObject(Product.class);
                    productList.add(product);
                }
                currentProductIndex = 0;
                loadProduct(currentProductIndex);
            } else {
                Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void loadProduct(int index) {
        if (!productList.isEmpty() && index >= 0 && index < productList.size()) {
            Product product = productList.get(index);
            productName.setText(product.getName());
            productPrice.setText(String.format("$%.2f", product.getPrice()));

            // Load product image
            Glide.with(this)
                    .load(product.getImageUrl())
                    .into(productImage);

            // Make product name a clickable link
            productName.setOnClickListener(v -> {
                if (product.getProductUrl() != null && !product.getProductUrl().isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(product.getProductUrl()));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(this, "No profile available", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No products available", Toast.LENGTH_SHORT).show();
        }
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    onSwipeRight();
                } else {
                    onSwipeLeft();
                }
                return true;
            }
            return false;
        }
    }

    private void onSwipeRight() {
        if (!productList.isEmpty()) {
            currentProductIndex = (currentProductIndex - 1 + productList.size()) % productList.size();
            loadProduct(currentProductIndex);
        }
    }

    private void onSwipeLeft() {
        if (!productList.isEmpty()) {
            currentProductIndex = (currentProductIndex + 1) % productList.size();
            loadProduct(currentProductIndex);
        }
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
        fetchProductsByCategory();
    }
}
