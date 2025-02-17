package com.example.tinker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout homeLayout;
    private Button buttonLaptops, buttonPhones, buttonTablets;
    private ImageButton buttonBack;
    private TextView productName, productPrice;
    private ImageView productImage;
    private CardView cardView;
    private GestureDetector gestureDetector;
    private List<ProductSerializable> laptops_list;
    private List<ProductSerializable> phones_list;
    private List<ProductSerializable> tablets_list;
    private String category;
    private ProductSerializable product;
    private MatchingAlgorithm laptops_engine;
    private MatchingAlgorithm phones_engine;
    private MatchingAlgorithm tablets_engine;

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
        buttonBack = findViewById(R.id.buttonBack);
        productName = findViewById(R.id.productName);
        productImage = findViewById(R.id.productImage);
        productPrice = findViewById(R.id.productPrice);

        // Initialize lists and Firestore
        laptops_list = new ArrayList<>();
        phones_list = new ArrayList<>();
        tablets_list = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        loadData().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                phones_engine = new SVDMatchingAlgorithm(phones_list);
                laptops_engine = new SVDMatchingAlgorithm(laptops_list);
                tablets_engine = new SVDMatchingAlgorithm(tablets_list);
                Log.d("Success", "Data loaded and recommendation engines initialized successfully.");
            } else {
                Log.e("Error", "Failed to load data", task.getException());
            }
        });
        setupGestureDetector();
        setupButtonListeners();
    }

    private Task<Void> loadData() {
        Task<List<ProductSerializable>> laptopsTask = loadProducts("laptops", laptops_list);
        Task<List<ProductSerializable>> phonesTask = loadProducts("phones", phones_list);
        Task<List<ProductSerializable>> tabletsTask = loadProducts("tablets", tablets_list);

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

    private Task<List<ProductSerializable>> loadProducts(String collectionName, List<ProductSerializable> targetList) {
        return db.collection(collectionName)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProductSerializable product = new ProductSerializable(document);
                            targetList.add(product);
                        }
                        Log.d("Firestore", collectionName + " loaded successfully");
                    } else {
                        Log.e("Firestore", "Failed to load " + collectionName, task.getException());
                    }
                    return targetList;
                });
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
            animateButtonTransition(buttonLaptops);
            homeLayout.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            category = "laptops";
            loadProduct(laptops_engine.getRecommendedProduct());
        });

        buttonPhones.setOnClickListener(v -> {
            animateButtonTransition(buttonPhones);
            homeLayout.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
            category = "phones";
            loadProduct(phones_engine.getRecommendedProduct());
        });

        buttonTablets.setOnClickListener(v -> {
            animateButtonTransition(buttonTablets);
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

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void loadProduct(ProductSerializable product) {
        this.product = product;
        productPrice.setText("$" + product.getPrice());
        if (product.getName().length() < 75) {
            productName.setText(product.getName().strip());
        }
        else {
            productName.setText(product.getName().strip().substring(0, Math.min(product.getName().strip().length(), 75)) + " ...");
        }

        // Load product image
        Glide.with(this)
                .load(product.getImageURL())
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
            switch (category) {
                case "laptops":
                    laptops_engine.handleSwipe(product, true);
                    loadProduct(laptops_engine.getRecommendedProduct());
                    break;
                case "tablets":
                    tablets_engine.handleSwipe(product, true);
                    loadProduct(tablets_engine.getRecommendedProduct());
                    break;
                case "phones":
                    phones_engine.handleSwipe(product, true);
                    loadProduct(phones_engine.getRecommendedProduct());
                    break;
            }
            Log.d("Product Loaded", product.getName());
        });
    }

    private void onSwipeLeft() {
        animateSwipe(-1000f, () -> {
            switch (category) {
                case "laptops":
                    laptops_engine.handleSwipe(product, false);
                    loadProduct(laptops_engine.getRecommendedProduct());
                    break;
                case "tablets":
                    tablets_engine.handleSwipe(product, false);
                    loadProduct(tablets_engine.getRecommendedProduct());
                    break;
                case "phones":
                    phones_engine.handleSwipe(product, false);
                    loadProduct(phones_engine.getRecommendedProduct());
                    break;
            }
            Log.d("Product Loaded", product.getName());
        });
    }

    private void animateButtonTransition(Button button) {
        button.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() ->
                        button.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .start())
                .start();
    }
}
