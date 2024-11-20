package com.example.grocery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.grocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen setup
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);  // Hide the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  // Fullscreen mode
        setContentView(R.layout.activity_splash);  // Setting the layout

        // Firebase instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Delay for splash screen, here it's 1 second
        new Handler().postDelayed(this::checkCurrentUser, 1000);
    }

    private void checkCurrentUser() {
        // Get the current logged-in user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            // If no user is logged in, navigate to LoginActivity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();  // Finish the splash screen
        } else {
            checkUserType(user.getUid());
        }
    }

    private void checkUserType(String userId) {
        // Firebase reference to check the user type (Seller or Buyer)
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String accountType = "" + dataSnapshot.child("accountType").getValue();

                // Check the account type and navigate accordingly
                if ("Seller".equals(accountType)) {
                    startActivity(new Intent(SplashActivity.this, MainSellerActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
                }
                finish();  // Finish the splash screen after navigation
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors, like database fetch failure
                // You can optionally show an error message or retry fetching the data
            }
        });
    }
}
