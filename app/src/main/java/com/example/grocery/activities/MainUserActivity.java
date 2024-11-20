package com.example.grocery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderUser;
import com.example.grocery.adapters.AdapterShop;
import com.example.grocery.models.ModelOrderUser;
import com.example.grocery.models.ModelShop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTv, emailTv, phoneTv, tabShopsTv, tabOrdersTv;
    private ImageButton logoutBtn, editProfileBtn, settingsBtn;
    private RelativeLayout shopsRl, ordersRl;
    private ImageView profileIv;
    private RecyclerView shopsRv, ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UI Views
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        tabShopsTv = findViewById(R.id.tabShopsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        profileIv = findViewById(R.id.profileIv);
        shopsRl = findViewById(R.id.shopsRl);
        ordersRl = findViewById(R.id.ordersRl);
        ordersRv = findViewById(R.id.ordersRv);
        settingsBtn = findViewById(R.id.settingsBtn);
        shopsRv = findViewById(R.id.shopsRv);


// Set LayoutManager to the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        ordersRv.setLayoutManager(layoutManager);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // Show shop UI at the start
        showShopsUI();

        logoutBtn.setOnClickListener(view -> makeMeOffline());
        editProfileBtn.setOnClickListener(v -> startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class)));

        tabShopsTv.setOnClickListener(view -> showShopsUI());
        tabOrdersTv.setOnClickListener(view -> showOrdersUI());

        // Settings Button
        settingsBtn.setOnClickListener(v -> startActivity(new Intent(MainUserActivity.this, SettingsActivity.class)));
    }

    private void showShopsUI() {
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void makeMeOffline() {
        progressDialog.setMessage("Logging out...");
        progressDialog.show();

        // Update user status to offline
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    firebaseAuth.signOut();
                    checkUser(); // Check user status after sign out
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            //get user data
                            String fullName = "" + ds.child("fullName").getValue();
                            String email = "" + ds.child("email").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();

                            // Set data to UI
                            nameTv.setText(fullName);
                            emailTv.setText(email);
                            phoneTv.setText(phone);

                            try {
                                // Load image using Picasso
                                Picasso.get()
                                        .load(profileImage)
                                        .placeholder(R.drawable.ic_person_gray)
                                        .into(profileIv);
                            } catch (Exception e) {
                                // Set default image if loading fails
                                profileIv.setImageResource(R.drawable.ic_person_gray);
                            }
                            loadShops(city);
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors here
                    }
                });
    }

    private void loadOrders() {
        ordersList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = ds.getRef().getKey();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);
                                            // Add to list
                                            ordersList.add(modelOrderUser);
                                        }
                                        // Setup adapter
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, ordersList);
                                        // Set to RecyclerView
                                        ordersRv.setAdapter(adapterOrderUser);
                                    }
                                }


                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadShops(final String myCity) {
        // Initialize the shops list
        shopsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Clear the list before adding new items
                        shopsList.clear();

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelShop modelShop = ds.getValue(ModelShop.class);
                            String shopCity = "" + ds.child("city").getValue();

                            // Show only shops from the user's city
                            if (shopCity.equals(myCity)) {
                                shopsList.add(modelShop);
                            }
                            // If you want to display all shops, add this line instead of the if statement
                            // shopsList.add(modelShop);
                        }

                        // Set up the adapter
                        adapterShop = new AdapterShop(MainUserActivity.this, shopsList);
                        // Set adapter to RecyclerView
                        shopsRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainUserActivity.this, "Failed to load shops: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });
    }

}
