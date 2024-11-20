package com.example.grocery.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.grocery.Constants;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterCartItem;
import com.example.grocery.adapters.AdapterProductUser;
import com.example.grocery.adapters.AdapterReview;
import com.example.grocery.models.ModelCartItem;
import com.example.grocery.models.ModelProduct;
import com.example.grocery.models.ModelReview;
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

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {
    // Declare UI views
    private ImageView shopIv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv, addressTv, filteredProductsTv,cartCountTv;
    private ImageButton callBtn, mapBtn, cartBtn, backBtn, filterProductBtn,reviewsBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;

    private String shopUid;
    private String myLatitude, myLongitude,myPhone ;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    public String deliveryFee;
    private FirebaseAuth firebaseAuth;
    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;
    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        // Initialize UI views
        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);

        callBtn = findViewById(R.id.callBtn);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        cartCountTv = findViewById(R.id.cartCountTv);
        backBtn = findViewById(R.id.backBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);

        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);
        ratingBar = findViewById(R.id.ratingBar);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Get the shop UID from the intent
        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();
        //declare it to class level and initialize it in onCreate()
        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //each shop have its own products and orders so if user add items to cart and go back and open cart in different shop then cart should be different
        //so delete cart data whenever user open this activities
        deleteCartData();
        cartCount();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when user type each letter
                try {
                    adapterProductUser.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to the previous activity
                onBackPressed();
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show cart dialog
                showCartDialog();

            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        filterProductBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
            builder.setTitle("Filter Products:")
                    .setItems(Constants.productCategories1, (dialog, which) -> {
                        String selected = Constants.productCategories1[which];
                        filteredProductsTv.setText(selected);
                        if (selected.equals("ALL")) {
                            loadShopProducts();
                        } else {
                            adapterProductUser.getFilter().filter(selected);
                        }
                    })
                    .show();
        });


        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass shop uid to show its review
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });
    }
    private float ratingSum = 0;

    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear list before adding data
                        ratingSum = 0;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Get data as model
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue()); // eg 4.3
                            ratingSum = ratingSum+rating;
                        }
                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating =  ratingSum/numberOfReviews ;
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle possible errors here (if needed)
                    }
                });
    }
    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();//delete all records from cart
    }
    public void cartCount() {
        // Keep it public so we can access it in adapter
        // Get cart count from the database using easyDB
        int count = easyDB.getAllData().getCount();
        // Check if the count is less than or equal to 0 (empty cart)
        if (count <= 0) {
            // No item in cart, hide the cart count TextView
            cartCountTv.setVisibility(View.GONE);
        } else {
            // There are items in the cart, show the cart count TextView and set the count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText("" + count); // Convert count to String to set in TextView
        }
    }


    public double allTotalPrice = 0.00;
    //need to view these views in adapter so making public
    public TextView sTotalTv,dFeeTv,allTotalPriceTv,promoDescriptionTv,discountTv;
    public EditText promoCodeEt;
    public Button applyBtn;

    private void showCartDialog() {
        //init list
        cartItemList = new ArrayList<>();
        // Inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);

        // Initialize views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        promoCodeEt = view.findViewById(R.id.promoCodeEt);
        promoDescriptionTv = view.findViewById(R.id.promoDescriptionTv);
        discountTv = view.findViewById(R.id.discountTv);
        FloatingActionButton validateBtn = view.findViewById(R.id.validateBtn);
        applyBtn = view.findViewById(R.id.applyBtn);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        // Whenever the cart dialog shows, check if a promo code is applied or not
        if (isPromoCodeApplied) {
            // Promo code is applied
            promoDescriptionTv.setVisibility(View.VISIBLE);
            applyBtn.setVisibility(View.VISIBLE);
            applyBtn.setText("Applied");
            promoCodeEt.setText(promoCode); // Set the promo code in the EditText
            promoDescriptionTv.setText(promoDescription); // Set the description of the promo code
        } else {
            // Promo code is not applied
            promoDescriptionTv.setVisibility(View.GONE);
            applyBtn.setVisibility(View.GONE);
            applyBtn.setText("Apply"); // Reset button text to "Apply"
        }

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        // Get all records from the database
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);         // Corrected the assignment
            String pId = res.getString(2);        // Corrected the assignment
            String name = res.getString(3);       // Corrected the assignment
            String price = res.getString(4);      // Corrected the assignment
            String cost = res.getString(5);       // Corrected the assignment
            String quantity = res.getString(6);   // Corrected the assignment

            // Remove '$' sign if present before parsing to double
            cost = cost.replace("$", "");  // Removes the dollar sign

            allTotalPrice = allTotalPrice + Double.parseDouble(cost); // Now it will work

            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity
            );
            cartItemList.add(modelCartItem);
        }

        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        //set to recyclerView
        cartItemsRv.setAdapter(adapterCartItem);
        if (isPromoCodeApplied) {
            priceWithDiscount();
        } else {
            priceWithoutDiscount();
        }

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });

        //place Order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                    //user did not enter address in profile
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile before placing order...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if (myPhone.equals("") || myPhone.equals("null")) {
                    //user did not enter phone in profile
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your phone number in your profile before placing order...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if (cartItemList.size() == 0) {
                    Toast.makeText(ShopDetailsActivity.this, "No item in cart...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                submitOrder();
            }
        });

        //start validating promo code when validate button is pressed
        validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Flow:
                // 1) Get Code from EditText
                String promotionCode = promoCodeEt.getText().toString().trim();
                // 2) Check if the code is empty
                if (TextUtils.isEmpty(promotionCode)) {
                    // Show a message if the promo code is not entered
                    Toast.makeText(ShopDetailsActivity.this, "Please enter promo code...", Toast.LENGTH_SHORT).show();
                } else {
                    // 3) Check if the code is valid (available in seller's promotions DB)
                    checkCodeAvailability(promotionCode);
                }
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPromoCodeApplied = true;
                applyBtn.setText("Applied");
                priceWithDiscount();
            }
        });
    }

    private void priceWithDiscount(){
        discountTv.setText("$"+ promoPrice);
        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$"+ String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("$", "")) - Double.parseDouble(promoPrice)));
    }

    private void priceWithoutDiscount() {
        discountTv.setText("$0");
        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$" + String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("$", ""))));
    }

    public boolean isPromoCodeApplied = false;
    public String promoId, promoTimestamp, promoCode, promoDescription, promoExpDate, promoMinimumOrderPrice, promoPrice;

    private void checkCodeAvailability(String promotionCode) {
        // Initialize progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Checking Promo Code...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // Reset promo application status
        isPromoCodeApplied = false;
        applyBtn.setText("Apply");
        priceWithoutDiscount();

        // Check promo code availability in Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Promotions")
                .orderByChild("promoCode")
                .equalTo(promotionCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Check if promo code exists
                        if (snapshot.exists()) {
                            // Promo code exists, dismiss progress dialog
                            progressDialog.dismiss();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                promoId = "" + ds.child("id").getValue();
                                promoTimestamp = "" + ds.child("timestamp").getValue();
                                promoCode = "" + ds.child("promoCode").getValue();
                                promoDescription = "" + ds.child("description").getValue();
                                promoExpDate = "" + ds.child("expireDate").getValue();
                                promoMinimumOrderPrice = "" + ds.child("minimumOrderPrice").getValue();
                                promoPrice = "" + ds.child("promoPrice").getValue();

                                // Check if promo code is expired
                                checkCodeExpireDate();
                            }
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(ShopDetailsActivity.this, "Invalid promo code", Toast.LENGTH_SHORT).show();
                            applyBtn.setVisibility(View.GONE);
                            promoDescriptionTv.setVisibility(View.GONE);
                            promoDescriptionTv.setText("");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkCodeExpireDate() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month starts from 0, so add 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // Concatenate date
        String todayDate = day + "/" + month + "/" + year;

        /* --- Check for expiry --- */
        try {
            SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = sdformat.parse(todayDate);
            Date expireDate = sdformat.parse(promoExpDate); // compare dates

            if (expireDate.compareTo(currentDate) > 0) {
                // Date 1 occurs after Date 2 (i.e., not expired)
                checkMinimumOrderPrice();
            } else if (expireDate.compareTo(currentDate) < 0) {
                // Date 1 occurs before Date 2 (i.e., expired)
                Toast.makeText(this, "The promotion code is expired on " + promoExpDate, Toast.LENGTH_SHORT).show();
                applyBtn.setVisibility(View.GONE);
                promoDescriptionTv.setVisibility(View.GONE);
                promoDescriptionTv.setText("");
            }
            else if (expireDate.compareTo(currentDate) == 0) {
                checkMinimumOrderPrice();
            }

        } catch (Exception e) {
            // If anything goes wrong causing exception while comparing dates
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        }
    }

    private void checkMinimumOrderPrice() {
        // Each promo code has a minimum order price requirement. If the order price is less than the required minimum, don't allow the promo code to be applied
        if (Double.parseDouble(String.format("%.2f", allTotalPrice)) < Double.parseDouble(promoMinimumOrderPrice)) {
            // Current order price is less than the minimum order price required by the promo code, so don't allow to apply
            Toast.makeText(this, "This code is valid for orders with a minimum amount of: $" + promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        } else {
            // Current order price is equal to or greater than the minimum order price required by the promo code, allow to apply the code
            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            promoDescriptionTv.setText(promoDescription);
        }
    }




    private void submitOrder() {
        //show progress
        progressDialog.setMessage("Placing Order...");
        progressDialog.show();
        //for order id and order time
        final String timestamp = ""+System.currentTimeMillis();
        String cost = allTotalPriceTv.getText().toString().trim().replace("$", ""); //remove $ if contains
        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", ""+timestamp);
        hashMap.put("orderTime", ""+timestamp);
        hashMap.put("orderStatus", "In Progress"); //In Progress/Completed/Cancelled
        hashMap.put("orderCost", ""+cost);
        hashMap.put("orderBy", ""+firebaseAuth.getUid());
        hashMap.put("orderTo", ""+shopUid);
        hashMap.put("latitude", ""+myLatitude);
        hashMap.put("longitude", ""+myLongitude);
        hashMap.put("deliveryFee", ""+deliveryFee);

        if (isPromoCodeApplied){
            //promo applied
            hashMap.put("discount", ""+promoPrice);
        }else{
            //promo not applied,include price 0
            hashMap.put("discount", "0");
        }


        //add to db
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        // Add order info to database
                        // Loop through cart items and add each item to the order in the database
                        for (int i = 0; i < cartItemList.size(); i++) {
                            // Get each item from cartItemList
                            String pId = cartItemList.get(i).getId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String name = cartItemList.get(i).getName();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            // Create a HashMap to store the details of each order item
                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            // Add the item to the database under the order timestamp and item ID
                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        // Dismiss progress dialog after placing the order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();
                        prepareNotificationMessage(timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Dismiss progress dialog and show failure message
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "http://maps.google.com/maps?saddr=" + myLatitude+","+myLongitude+"&daddr=" + shopLatitude+","+shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadShopProducts() {
        // Initialize list
        productsList = new ArrayList<>();

        // Get reference to Firebase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Clear list before adding items
                        productsList.clear();

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }

                        // Set up the adapter
                        adapterProductUser = new AdapterProductUser( ShopDetailsActivity.this, productsList);
                        // Set adapter to RecyclerView
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error (optional)
                    }
                });
    }
    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get shop data
                String name = "" + dataSnapshot.child("name").getValue();
                shopName = "" + dataSnapshot.child("shopName").getValue();
                shopEmail = "" + dataSnapshot.child("email").getValue();
                shopPhone = "" + dataSnapshot.child("phone").getValue();
                shopAddress = "" + dataSnapshot.child("address").getValue();
                shopLatitude = "" + dataSnapshot.child("latitude").getValue();
                shopLongitude = "" + dataSnapshot.child("longitude").getValue();
                deliveryFee = "" + dataSnapshot.child("deliveryFee").getValue();
                String profileImage = "" + dataSnapshot.child("profileImage").getValue();
                String shopOpen = "" + dataSnapshot.child("shopOpen").getValue();

                // Set data to TextViews
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: $"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);

                // Set shop status (Open/Closed)
                if (shopOpen.equals("true")) {
                    openCloseTv.setText("Open");
                } else {
                    openCloseTv.setText("Closed");
                }

                // Load profile image using Picasso
                try {
                    Picasso.get().load(profileImage).into(shopIv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible database error
                Log.e("DatabaseError", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            myPhone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String city = "" + ds.child("city").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any errors
                    }
                });
    }
    private void prepareNotificationMessage(String orderId) {
        // When user places an order, send notification to the seller
        // Prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC; // Must be same as subscribed by user
        String NOTIFICATION_TITLE = "New Order " + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have a new order.";
        String NOTIFICATION_TYPE = "NewOrder";

        // Prepare JSON (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            // What to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid()); // Current user UID (buyer)
            notificationBodyJo.put("sellerUid", shopUid); // Assuming shopUid is defined and contains the seller's UID
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            // Where to send
            notificationJo.put("to", NOTIFICATION_TOPIC); // To all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo); // Payload

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo,orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, final String orderId) {
        // Send Volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                "https://fcm.googleapis.com/fcm/send",
                notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after placing order open order details page
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderId", ""+orderId);
                intent.putExtra("orderTo", ""+shopUid);
                startActivity(intent);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderId", ""+orderId);
                intent.putExtra("orderTo", ""+shopUid);
                startActivity(intent);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);
                return headers;
            }
        };
        // Enqueue the Volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }


}
