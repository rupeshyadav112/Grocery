package com.example.grocery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class WriteReviewActivity extends AppCompatActivity {
    //ui views
// Declare the views
    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv;
    private RatingBar ratingBar;
    private EditText reviewEt;
    private FloatingActionButton submitBtn;


    private String shopUid;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize UI views
        backBtn = findViewById(R.id.backBtn);
        profileIv = findViewById(R.id.profileIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewEt = findViewById(R.id.reviewEt);
        submitBtn = findViewById(R.id.submitBtn);

        // Get the shop uid from the intent extras
        shopUid = getIntent().getStringExtra("shopUid");
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        //load shop info: shop name,shop Image
        loadShopInfo();
        loadMyReview();


        // go back previous activity
       backBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onBackPressed();
           }
       });
       //input data
        submitBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               inputData();
           }
       });


    }

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get shop info
                String shopName = "" + dataSnapshot.child("shopName").getValue();
                String shopImage = "" + dataSnapshot.child("profileImage").getValue();

                // Set shop info to UI
                shopNameTv.setText(shopName);

                try {
                    // Load image using Picasso
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                } catch (Exception e) {
                    // If there's an error, set default image
                    profileIv.setImageResource(R.drawable.ic_store_gray);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // My review is available in this shop
                            // Get review details
                            String uid = "" + dataSnapshot.child("uid").getValue();
                            String ratings = "" + dataSnapshot.child("ratings").getValue();
                            String review = "" + dataSnapshot.child("review").getValue();
                            String timestamp = "" + dataSnapshot.child("timestamp").getValue();

                            // Set review details to our UI
                            float myRating = Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);  // Set rating on RatingBar
                            reviewEt.setText(review);  // Set review in EditText
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                        Toast.makeText(WriteReviewActivity.this, "Failed to load my review: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void inputData() {
        String ratings = "" + ratingBar.getRating();
        String review = reviewEt.getText().toString().trim();
        // Get current timestamp for the review timestamp in the database
        String timestamp = "" + System.currentTimeMillis();

        // setup data in hashmap
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + firebaseAuth.getUid());  // User ID
        hashMap.put("ratings", "" + ratings);  // Rating value (e.g., 4.6)
        hashMap.put("review", "" + review);  // Review text (e.g., Good service)
        hashMap.put("timestamp", "" + timestamp);

        // Firebase database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Success message
                        Toast.makeText(WriteReviewActivity.this, "Review published successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error message
                        Toast.makeText(WriteReviewActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}