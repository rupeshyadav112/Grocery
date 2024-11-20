package com.example.grocery.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterReview;
import com.example.grocery.models.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {
    // UI views
    private ImageButton backBtn;
    private ImageView profileIv;
    private RatingBar ratingBar;
    private TextView ratingsTv, shopNameTv;
    private RecyclerView reviewsRv;

    private ArrayList<ModelReview> reviewArrayList;
    private AdapterReview adapterReview;

    private String shopUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI views
        backBtn = findViewById(R.id.backBtn);
        profileIv = findViewById(R.id.profileIv);
        ratingBar = findViewById(R.id.ratingBar);
        ratingsTv = findViewById(R.id.ratingsTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        reviewsRv = findViewById(R.id.reviewsRv);
        // Get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");
        // Initialize Firebase auth
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        loadShopDetails();
        loadReviews();

        backBtn.setOnClickListener(v -> onBackPressed());
    }

    private float ratingSum = 0;

    private void loadReviews() {
        // Initialize list
        reviewArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear list before adding data
                        reviewArrayList.clear();
                        ratingSum = 0;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Get data as model
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue()); // eg 4.3
                            ratingSum = ratingSum+rating;
                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);
                        }

                        // Setup adapter
                        adapterReview = new AdapterReview(ShopReviewsActivity.this, reviewArrayList);
                        reviewsRv.setAdapter(adapterReview);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating =  ratingSum/numberOfReviews ;

                        ratingsTv.setText(String.format("%.2f", avgRating) + " [" + numberOfReviews + "]");
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle possible errors here (if needed)
                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get user info, use the same key names as in Firebase
                        String shopName = "" + dataSnapshot.child("shopName").getValue();
                        String profileImage = "" + dataSnapshot.child("profileImage").getValue();

                        // Set the user's name and profile image to the view holder
                        shopNameTv.setText(shopName);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                        } catch (Exception e) {
                            profileIv.setImageResource(R.drawable.ic_store_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors here (if needed)
                    }
                });
    }
}
