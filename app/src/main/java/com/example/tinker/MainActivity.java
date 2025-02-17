package com.example.tinker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout homeLayout;
    private Button buttonLaptops, buttonPhones, buttonTablets;
    private TextView productName;
    private ImageView productImage;
    private CardView cardView;
    private GestureDetector gestureDetector;
    private List<Product> laptopsList = new ArrayList<>();
    private List<Product> phonesList = new ArrayList<>();
    private List<Product> tabletsList = new ArrayList<>();
    private String category;
    private Product product;
    RecommendationEngine laptops_engine;
    RecommendationEngine phones_engine;
    RecommendationEngine tablets_engine;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeLayout = findViewById(R.id.homeLayout);
        cardView = findViewById(R.id.cardView);
        buttonLaptops = findViewById(R.id.buttonLaptops);
        buttonTablets = findViewById(R.id.buttonTablets);
        buttonPhones = findViewById(R.id.buttonPhones);

        productName = findViewById(R.id.productName);
        productImage = findViewById(R.id.productImage);


        db = FirebaseFirestore.getInstance();

        db.collection("laptops")
                .get()
                .addOnCompleteListener(response -> {
                    for (QueryDocumentSnapshot documentReference: response.getResult()) {
                        laptopsList.add(new Product(documentReference.getReference()));
                        Log.d("Laptop", documentReference.getString("name"));
                    }
                }).addOnFailureListener( response -> Log.d("Firestore", "Could not load laptops"));

        db.collection("phones")
                .get()
                .addOnCompleteListener(response -> {
                    for (QueryDocumentSnapshot documentReference: response.getResult()) {
                        phonesList.add(new Product(documentReference.getReference()));
                        Log.d("Phone", documentReference.getString("name"));
                    }
                }).addOnFailureListener( response -> Log.d("Firestore", "Could not load phones"));

        db.collection("tablets")
                .get()
                .addOnCompleteListener(response -> {
                    for (QueryDocumentSnapshot documentReference: response.getResult()) {
                        tabletsList.add(new Product(documentReference.getReference()));
                        Log.d("Tablet", documentReference.getString("name"));
                    }
                }).addOnFailureListener( response -> Log.d("Firestore", "Could not load tablets"));

        SVD svd = new SVD(laptopsList, phonesList, tabletsList);
        laptops_engine = new RecommendationEngine(svd.getLaptopLatentFactors(), laptopsList);
        phones_engine = new RecommendationEngine(svd.getPhoneLatentFactors(), phonesList);
        tablets_engine = new RecommendationEngine(svd.getTabletLatentFactors(), tabletsList);

        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        cardView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        buttonLaptops.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            category = "laptops";
            loadProduct(laptops_engine.getRecommendedProducts().get(0));
        });
        buttonPhones.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            category = "phones";
            loadProduct(phones_engine.getRecommendedProducts().get(0));
        });
        buttonTablets.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            category = "tablets";
            loadProduct(tablets_engine.getRecommendedProducts().get(0));
        });
    }
    @SuppressLint("DefaultLocale")
    private void loadProduct(Product product) {
            this.product = product;
            productName.setText(product.getName());

            // Load product image
            Glide.with(this)
                    .load(product.getImageUrl())
                    .into(productImage);

            // Make product name a clickable link
            productName.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(product.getProductUrl()));
                startActivity(browserIntent);
            });
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
        if (category == "laptops") {
            laptops_engine.handleSwipe(product, true);
        }
        if (category == "tablets") {
            tablets_engine.handleSwipe(product, true);
        }
        if (category == "phones") {
            phones_engine.handleSwipe(product, true);
        }

    }

    private void onSwipeLeft() {
        if (category == "laptops") {
            laptops_engine.handleSwipe(product, false);
        }
        if (category == "tablets") {
            tablets_engine.handleSwipe(product, false);
        }
        if (category == "phones") {
            phones_engine.handleSwipe(product, false);
        }
    }
}
