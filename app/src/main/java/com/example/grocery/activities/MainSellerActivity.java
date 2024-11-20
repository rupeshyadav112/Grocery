package com.example.grocery.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.grocery.adapters.AdapterOrderShop;
import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.Constants;
import com.example.grocery.models.ModelOrderShop;
import com.example.grocery.models.ModelProduct;
import com.example.grocery.R;
import com.squareup.picasso.Picasso;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {

    // Declare the views
    private TextView nameTv, shopNameTv, emailTv,tabProductsTv,tabOrdersTv,filteredProductsTv,filteredOrdersTv;
    private EditText searchProductEt;
    private ImageButton logoutBtn, editProfileBtn, addProductBtn,filterProductBtn,filterOrderBtn,moreBtn;
    private ImageView profileIv;
    private RelativeLayout productsRl,ordersRl;
    private RecyclerView productsRv,ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });// Set the layout

        // Initialize the views
        nameTv = findViewById(R.id.nameTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        profileIv = findViewById(R.id.profileIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        emailTv = findViewById(R.id.emailTv);
        tabProductsTv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        productsRv = findViewById(R.id.productsRv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        ordersRv = findViewById(R.id.ordersRv);
        moreBtn = findViewById(R.id.moreBtn);




        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrders();
        showProductsUI();
        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when user type each letter
                try {
                    adapterProductSeller.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // Handle logout button click
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make offline
                //sign out
                //go to login activity
                makeMeOffline();
            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit profile activity
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));

            }
        });
        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open add product activity
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));

            }
        });
        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load products
                showProductsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders
                showOrdersUI();

            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected items
                                String selected = Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")){
                                    //load all products
                                    loadAllProducts();
                                }
                                else {
                                    //load filtered products
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                        .show();

            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Options to display in the dialog
                String[] options = {"All", "In Progress", "Completed", "Cancelled"};

                // Create an AlertDialog to display the options
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle item clicks based on which option was selected
                                if (which == 0) {
                                    // "All" option selected - load all orders
                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter("");
                                } else {
                                    // Other option selected - load filtered orders
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Showing " + optionClicked+ " Orders");//showing completed orders
                                    adapterOrderShop.getFilter().filter(optionClicked);

                                }
                            }
                        })
                        .show();
            }
        });

        //popup menu
        PopupMenu popupMenu = new PopupMenu(MainSellerActivity.this,moreBtn);
        //add menu item to our menu
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("Review");
        popupMenu.getMenu().add("Promotion Codes");
        //handle menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle() =="Settings"){
                    startActivity(new Intent(MainSellerActivity.this, SettingsActivity.class));

                }
                else if (item.getTitle() =="Review"){
                    Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
                    intent.putExtra("shopUid", firebaseAuth.getUid());
                    startActivity(intent);

                }
                else if (item.getTitle() =="Promotion Codes"){
                    //start promotion list screen
                    startActivity(new Intent(MainSellerActivity.this, PromotionCodesActivity.class));

                }
                return true;
            }
        });

        //show more options:Settings,Review,AddPromotionCode
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show popup menu
                popupMenu.show();
            }
        });

    }


    private void loadAllOrders() {
        // Initialize the order list
        orderShopArrayList = new ArrayList<>();
        // Get reference to Firebase database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Retrieve all orders related to the logged-in seller (current user's ID)
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear list before adding new data
                        orderShopArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Get the order model and add it to the list
                            ModelOrderShop orderShop = ds.getValue(ModelOrderShop.class);
                            orderShopArrayList.add(orderShop);
                        }

                        // Set up the adapter with the order list and attach it to the RecyclerView
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this, orderShopArrayList);
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error in case of failure
                        Toast.makeText(MainSellerActivity.this, "Failed to load orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();
        // Get the user's products from Firebase Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear the previous product list before loading new products
                        productList.clear();

                        // Get the products from the snapshot
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            // Retrieve the category of the product
                            String productCategory = "" + dataSnapshot.child("productCategory").getValue();

                            // Check if the product category matches the selected category
                            if (productCategory.equals(selected)) {
                                // Retrieve the product data using ModelProduct
                                ModelProduct modelProduct = dataSnapshot.getValue(ModelProduct.class);
                                if (modelProduct != null) {
                                    // Add the product to the list if it matches the category
                                    productList.add(modelProduct);
                                }
                            }
                        }

                        // Setup the adapter and bind it to the RecyclerView
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("loadFilteredProducts", "Error loading products: " + error.getMessage());
                    }
                });
    }


    private void loadAllProducts() {
        productList = new ArrayList<>();
        // Get the user's products from Firebase Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear the previous product list before loading new products
                        productList.clear();

                        // Get the products from the snapshot
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            // Retrieve the category of the product

                            // Retrieve the product data using ModelProduct
                            ModelProduct modelProduct = dataSnapshot.getValue(ModelProduct.class);
                            if (modelProduct != null) {
                                // Add the product to the list
                                productList.add(modelProduct);
                            }
                        }

                        // Setup the adapter and bind it to the RecyclerView
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("loadAllProducts", "Error loading products: " + error.getMessage());
                    }
                });
    }


    private void showProductsUI() {
        //show products ui and hide orders ui
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show orders ui and hide products ui
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);


    }



    private void makeMeOffline() {
        progressDialog.setMessage("Logging out...");
        progressDialog.show();

        // Create a HashMap to update user status
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        // update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //updated successfully
                        firebaseAuth.signOut();
                        checkUser(); // Check user status after sign out
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update status
                        progressDialog.dismiss(); // Dismiss the progress dialog
                        Toast.makeText(MainSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            // User is not logged in, navigate to LoginActivity
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        } else {
            // User is logged in, load user info
            loadMyInfo();
        }
    }

    private void loadMyInfo()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // get data from db
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String shopName = "" + ds.child("shopName").getValue();
                            String email = "" + ds.child("email").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();

                            // Set data to UI
                            nameTv.setText(name+"  ("+accountType+")");
                            shopNameTv.setText(shopName);
                            emailTv.setText(email);
                            try {
                                // Load image using Picasso
                                Picasso.get()
                                        .load(profileImage)
                                        .placeholder(R.drawable.ic_store_gray)
                                        .into(profileIv);
                            } catch (Exception e) {
                                // Set default image if loading fails
                                profileIv.setImageResource(R.drawable.ic_store_gray);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    

}





