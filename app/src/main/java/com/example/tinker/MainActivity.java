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
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize home layout views
        homeLayout = findViewById(R.id.homeLayout);
        buttonLaptops = findViewById(R.id.buttonLaptops);
        buttonPhones = findViewById(R.id.buttonPhones);
        buttonTablets = findViewById(R.id.buttonTablets);

        // Initialize product layout views
        productName = findViewById(R.id.productName);
        productDetails = findViewById(R.id.productDetails);
        productPrice = findViewById(R.id.productPrice);
        productImage = findViewById(R.id.productImage);
        cardView = findViewById(R.id.cardView);

        // Set sample product details for now
        String name = "Product XYZ";
        String details = "This is a high-end product with exceptional performance.";
        String price = "$499.99";

        productName.setText(name);
        productDetails.setText(details);
        productPrice.setText(price);
        setProductImage(R.drawable.lenovo);

        // Initialize swipe gesture detector for product card
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());
        cardView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        // Set click listeners for category buttons on the home screen
        buttonLaptops.setOnClickListener(v -> {
            // Optionally, store the selected category (e.g., "laptops")
            switchToProductView();
        });

        buttonPhones.setOnClickListener(v -> {
            // Optionally, store the selected category (e.g., "phones")
            switchToProductView();
        });

        buttonTablets.setOnClickListener(v -> {
            // Optionally, store the selected category (e.g., "tablets")
            switchToProductView();
        });
    }

    // Method to switch from home view to product view
    private void switchToProductView() {
        homeLayout.setVisibility(View.GONE);
        cardView.setVisibility(View.VISIBLE);
    }

    // Setter method to set the image in the ImageView
    private void setProductImage(int drawableId) {
        productImage.setImageResource(drawableId);
    }

    // Swipe Gesture Listener to detect left or right swipe
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

            // Check if the swipe is mostly horizontal
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

    // Handle swipe right
    private void onSwipeRight() {
        animateSwipe(1000f); // Swipe right animation
        loadNextProduct();
    }

    // Handle swipe left
    private void onSwipeLeft() {
        animateSwipe(-1000f); // Swipe left animation
        loadNextProduct();
    }

    // Animate the card swipe and then load next product
    private void animateSwipe(float translationX) {
        cardView.animate()
                .translationX(translationX)
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    cardView.setTranslationX(0);
                    cardView.setAlpha(1f);
                    loadNextProduct();
                })
                .start();
    }

    // Simulated method to load the next product (replace with dynamic loading later)
    private void loadNextProduct() {
        String name = "Product ABC";
        String details = "This is another high-quality product with better features.";
        String price = "$699.99";

        productName.setText(name);
        productDetails.setText(details);
        productPrice.setText(price);

        // Change image to template or another image as needed
        setProductImage(R.drawable.template);
    }
}
