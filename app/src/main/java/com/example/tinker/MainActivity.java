package com.example.tinker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
    private ImageButton buttonBack;
    private TextView productName;
    private ImageView productImage;
    private CardView cardView;
    private GestureDetector gestureDetector;
    private List<Product> laptopsList;
    private List<Product> phonesList;
    private List<Product> tabletsList;
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
        buttonBack = findViewById(R.id.buttonBack);
        productName = findViewById(R.id.productName);
        productImage = findViewById(R.id.productImage);

        laptopsList = new ArrayList<>();
        phonesList = new ArrayList<>();
        tabletsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        loadProductsFromFirestore();

        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        cardView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        buttonLaptops.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            category = "laptops";
            loadProduct(laptops_engine.getRecommendedProduct());
        });
        buttonPhones.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            category = "phones";
            loadProduct(phones_engine.getRecommendedProduct());
        });
        buttonTablets.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            category = "tablets";
            loadProduct(tablets_engine.getRecommendedProduct());
        });

        buttonBack.setOnClickListener(v -> {
            cardView.setVisibility(View.GONE); // Hide product screen
            homeLayout.setVisibility(View.VISIBLE); // Show category selection
        });
    }

    private void loadProductsFromFirestore() {
        AtomicInteger completedTasks = new AtomicInteger(0);

        db.collection("laptops")
                .get()
                .addOnCompleteListener(response -> {
                    if (response.isSuccessful()) {
                        for (QueryDocumentSnapshot document : response.getResult()) {
                            laptopsList.add(new Product(document.getReference()));
                        }
                        Log.d("Firestore", "Laptops loaded successfully");
                    } else {
                        Log.d("Firestore", "Failed to load laptops");
                    }
                    checkAllDataLoaded(completedTasks);
                });

        db.collection("phones")
                .get()
                .addOnCompleteListener(response -> {
                    if (response.isSuccessful()) {
                        for (QueryDocumentSnapshot document : response.getResult()) {
                            phonesList.add(new Product(document.getReference()));
                        }
                        Log.d("Firestore", "Phones loaded successfully");
                    } else {
                        Log.d("Firestore", "Failed to load phones");
                    }
                    checkAllDataLoaded(completedTasks);
                });

        db.collection("tablets")
                .get()
                .addOnCompleteListener(response -> {
                    if (response.isSuccessful()) {
                        for (QueryDocumentSnapshot document : response.getResult()) {
                            tabletsList.add(new Product(document.getReference()));
                        }
                        Log.d("Firestore", "Tablets loaded successfully");
                    } else {
                        Log.d("Firestore", "Failed to load tablets");
                    }
                    checkAllDataLoaded(completedTasks);
                });
    }

    private void checkAllDataLoaded(AtomicInteger completedTasks) {
        if (completedTasks.incrementAndGet() == 3) {
            Log.d("Firestore", "All products loaded. Running SVD...");
            new Handler(Looper.getMainLooper()).post(this::initializeRecommendationEngines);
        }
    }

    private void initializeRecommendationEngines() {
        SVD svd = new SVD(laptopsList, phonesList, tabletsList);
        laptops_engine = new RecommendationEngine(svd.getLaptopLatentFactors(), laptopsList);
        phones_engine = new RecommendationEngine(svd.getPhoneLatentFactors(), phonesList);
        tablets_engine = new RecommendationEngine(svd.getTabletLatentFactors(), tabletsList);
        Log.d("Success", "Recommendation engines initialized successfully.");
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void loadProduct(Product product) {
        this.product = product;
        String product_name_raw = product.getName().strip();
        if (product.getName().length() < 75) {
            productName.setText(product_name_raw);
        }
        else {
            productName.setText(product_name_raw.substring(0, Math.min(product_name_raw.length(), 75)));
        }


        // Load product image
        Glide.with(this)
                .load(product.getImageUrl())
                .into(productImage);

        // Make product name a clickable link
//        productName.setOnClickListener(v -> {
//            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(product.getProductUrl()));
//            startActivity(browserIntent);
//        });
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
    private void animateSwipe(float translationX, Runnable onAnimationEnd) {
        cardView.animate()
                .translationX(translationX)
                .rotation(translationX > 0 ? 15 : -15)
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    cardView.setTranslationX(0);
                    cardView.setRotation(0);
                    cardView.setAlpha(1f);
                    onAnimationEnd.run();

                })
                .start();
    }
    private void onSwipeRight() {
        animateSwipe(1000f, () -> {
            if (category.equals("laptops")) {
                laptops_engine.handleSwipe(product, true);
                loadProduct(laptops_engine.getRecommendedProduct());
            } else if (category.equals("tablets")) {
                tablets_engine.handleSwipe(product, true);
                loadProduct(tablets_engine.getRecommendedProduct());
            } else if (category.equals("phones")) {
                phones_engine.handleSwipe(product, true);
                loadProduct(phones_engine.getRecommendedProduct());
            }
            Log.d("Product Loaded", product.getName());
        });
    }

    private void onSwipeLeft() {
        animateSwipe(-1000f, () -> {
            if (category.equals("laptops")) {
                laptops_engine.handleSwipe(product, false);
                loadProduct(laptops_engine.getRecommendedProduct());
            } else if (category.equals("tablets")) {
                tablets_engine.handleSwipe(product, false);
                loadProduct(tablets_engine.getRecommendedProduct());
            } else if (category.equals("phones")) {
                phones_engine.handleSwipe(product, false);
                loadProduct(phones_engine.getRecommendedProduct());
            }
            Log.d("Product Loaded", product.getName());
        });
    }


    private void enableButtons(boolean enabled) {
        buttonLaptops.setEnabled(enabled);
        buttonPhones.setEnabled(enabled);
        buttonTablets.setEnabled(enabled);
    }

}
