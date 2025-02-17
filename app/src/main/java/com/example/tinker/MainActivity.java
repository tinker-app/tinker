package com.example.tinker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout homeLayout;
    private Button buttonLaptops, buttonPhones, buttonTablets;
    private TextView productName;
    private ImageView productImage;
    private CardView cardView;
    private GestureDetector gestureDetector;
    private List<Product> laptopsList;
    private List<Product> phonesList;
    private List<Product> tabletsList;
    private String category;
    private Product product;
    private RecommendationEngine laptops_engine;
    private RecommendationEngine phones_engine;
    private RecommendationEngine tablets_engine;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        homeLayout = findViewById(R.id.homeLayout);
        cardView = findViewById(R.id.cardView);
        buttonLaptops = findViewById(R.id.buttonLaptops);
        buttonTablets = findViewById(R.id.buttonTablets);
        buttonPhones = findViewById(R.id.buttonPhones);
        productName = findViewById(R.id.productName);
        productImage = findViewById(R.id.productImage);

        // Initialize lists and Firestore
        laptopsList = new ArrayList<>();
        phonesList = new ArrayList<>();
        tabletsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // Disable buttons until data is loaded
        setButtonsEnabled(false);

        // Load data asynchronously
        loadData().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Initialize recommendation engines after data is loaded
                SVD svd = new SVD(laptopsList, phonesList, tabletsList);
                laptops_engine = new RecommendationEngine(svd.getLaptopLatentFactors(), laptopsList);
                phones_engine = new RecommendationEngine(svd.getPhoneLatentFactors(), phonesList);
                tablets_engine = new RecommendationEngine(svd.getTabletLatentFactors(), tabletsList);

                // Enable buttons after everything is initialized
                setButtonsEnabled(true);
                Log.d("Success", "Data loaded and recommendation engines initialized successfully.");
            } else {
                Log.e("Error", "Failed to load data", task.getException());
                // Handle error - maybe show a message to user
            }
        });

        // Setup gesture detector and button click listeners
        setupGestureDetector();
        setupButtonListeners();
    }

    private Task<Void> loadData() {
        // Create tasks for loading each collection
        Task<List<Product>> laptopsTask = loadProducts("laptops", laptopsList);
        Task<List<Product>> phonesTask = loadProducts("phones", phonesList);
        Task<List<Product>> tabletsTask = loadProducts("tablets", tabletsList);

        // Combine all tasks
        return Tasks.whenAll(laptopsTask, phonesTask, tabletsTask)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return Tasks.forResult(null);
                    } else {
                        return Tasks.forException(task.getException());
                    }
                });
    }

    private Task<List<Product>> loadProducts(String collectionName, List<Product> targetList) {
        return db.collection(collectionName)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = new Product(document);
                            targetList.add(product);
                        }
                        Log.d("Firestore", collectionName + " loaded successfully");
                    } else {
                        Log.e("Firestore", "Failed to load " + collectionName, task.getException());
                    }
                    return targetList;
                });
    }

    private void setButtonsEnabled(boolean enabled) {
        buttonLaptops.setEnabled(enabled);
        buttonPhones.setEnabled(enabled);
        buttonTablets.setEnabled(enabled);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());
        cardView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void setupButtonListeners() {
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
    }

    @SuppressLint("DefaultLocale")
    private void loadProduct(Product product) {
        this.product = product;
        productName.setText(product.getName());

        // Load product image
        Glide.with(this)
                .load(product.getImageUrl())
                .into(productImage);
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
    }

    private void onSwipeLeft() {
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
    }
}