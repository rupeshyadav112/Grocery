package com.example.grocery.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderUser;
import com.example.grocery.adapters.AdapterOrderedItem;
import com.example.grocery.models.ModelOrderUser;
import com.example.grocery.models.ModelOrderedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUserActivity extends AppCompatActivity {
    private String orderTo, orderId;

    // UI views
    private ImageButton backBtn, writeReviewBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, shopNameTv, totalItemsTv, amountTv, addressTv;
    private RecyclerView itemsRv;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

   private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
        // Initialize views
        backBtn = findViewById(R.id.backBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRV);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);



        // Retrieve order information from the intent
        final Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo"); // orderTo contains the UID of the shop
        orderId = intent.getStringExtra("orderId");
        // Initialize FirebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();


        // Load shop info, order details, and ordered items
        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();

        // Set back button click listener
        backBtn.setOnClickListener((v) -> onBackPressed());

        //handle writeReviewBtn click, start write review activity
        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(OrderDetailsUserActivity.this, WriteReviewActivity.class);
                intent1.putExtra("shopUid", orderTo);//to write review we must have uid of shop
                startActivity(intent1);
            }
        });
    }

    private void loadOrderedItems() {
        // Initialize orderedItemArrayList
        orderedItemArrayList = new ArrayList<>();
        // Get ordered items from the database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Clear the list
                        orderedItemArrayList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ModelOrderedItem modelOrderedItem = snapshot.getValue(ModelOrderedItem.class);
                            orderedItemArrayList.add(modelOrderedItem);
                        }

                        // Update the RecyclerView adapter
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsUserActivity.this, orderedItemArrayList);
                        itemsRv.setAdapter(adapterOrderedItem);
                        //set item count
                        totalItemsTv.setText("" + dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }

                });
    }

    private void loadOrderDetails() {
        // Load order details
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get data from dataSnapshot
                        String orderBy = "" + dataSnapshot.child("orderBy").getValue();
                        String orderCost = "" + dataSnapshot.child("orderCost").getValue();
                        String orderId = "" + dataSnapshot.child("orderId").getValue();
                        String orderStatus = "" + dataSnapshot.child("orderStatus").getValue();
                        String orderTime = "" + dataSnapshot.child("orderTime").getValue();
                        String orderTo = "" + dataSnapshot.child("orderTo").getValue();
                        String deliveryFee = "" + dataSnapshot.child("deliveryFee").getValue();
                        String latitude = "" + dataSnapshot.child("latitude").getValue();
                        String longitude = "" + dataSnapshot.child("longitude").getValue();
                        String discount = "" + dataSnapshot.child("discount").getValue();

                        if (discount.equals("null") || discount.equals("0")) {
                            // Value is either null or "0"
                            discount = "& Discount $0";
                        } else {
                            discount = "& Discount $" + discount;
                        }


                        // Convert timestamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formattedDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

                        // Change order status text color based on status
                        if (orderStatus.equals("In Progress")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        } else if (orderStatus.equals("Completed")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        } else if (orderStatus.equals("Cancelled")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        // Set data to TextViews
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);

                        amountTv.setText("$" + orderCost + "[Including D.Fee $" + deliveryFee + "" + discount + " ]");
                        dateTv.setText(formattedDate);

                        // Call method to find and display address based on latitude and longitude
                        findAddress(latitude, longitude);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopInfo() {
        // Reference to Firebase database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Assume that the shop ID is stored in a variable called `shopId`
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        // Parse latitude and longitude to double
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        // Initialize Geocoder for retrieving address information
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            // Get the address from the latitude and longitude
            addresses = geocoder.getFromLocation(lat, lon, 1);

            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0); // Complete address
                addressTv.setText(address);
            } else {
                // Handle case where no address is found
                addressTv.setText("Address not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addressTv.setText("Unable to get address");
        }
    }

}
