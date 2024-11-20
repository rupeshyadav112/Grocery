package com.example.grocery.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterPromotionShop;
import com.example.grocery.models.ModelPromotion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PromotionCodesActivity extends AppCompatActivity {
    private ImageButton backBtn, addPromoBtn, filterBtn;
    private TextView filteredTv;
    private RecyclerView promoRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelPromotion> promotionArrayList;
    private AdapterPromotionShop adapterPromotionShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_codes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        backBtn = findViewById(R.id.backBtn);
        addPromoBtn = findViewById(R.id.addPromoBtn);
        filterBtn = findViewById(R.id.filterBtn);
        filteredTv = findViewById(R.id.filteredTv);
        promoRv = findViewById(R.id.promoRv);

        // Initialize firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
        // Load all promo codes initially
        loadAllPromoCodes();

        // Set click listeners for buttons
        backBtn.setOnClickListener(v -> onBackPressed());
        addPromoBtn.setOnClickListener(v -> {
            // Handle add promo button click
            startActivity(new Intent(PromotionCodesActivity.this, AddPromotionCodeActivity.class));
        });


        // Handle filtering with filter button
        filterBtn.setOnClickListener(v -> {
           filterDialog();
        });
    }

    private void filterDialog() {
        // options to display in dialog
        String[] options = {"ALL", "Expired", "Not Expired"};

        // dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Promotion Codes")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // handle item clicks
                        if (i == 0) {
                            // ALL clicked
                            filteredTv.setText("All Promotion Codes");
                            loadAllPromoCodes();  // Load all promotion codes
                        } else if (i == 1) {
                            // Expired clicked
                            filteredTv.setText("Expired Promotion Codes");
                            loadExpiredPromoCodes();  // Load expired promotion codes
                        } else if (i == 2) {
                            // Not Expired clicked
                            filteredTv.setText("Not Expired Promotion Codes");
                            loadNotExpiredPromoCodes();  // Load not expired promotion codes
                        }
                    }
                })
                .show();  // Show the dialog
    }


    private void loadAllPromoCodes() {
        // Initialize the list
        promotionArrayList = new ArrayList<>();

        // Database reference for Users > current user > Promotions > codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear the list before adding new data
                        promotionArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Get the promotion data from the snapshot
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            // Add the data to the list
                            promotionArrayList.add(modelPromotion);
                        }

                        // Set up the adapter with the updated list and attach it to the RecyclerView
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error (optional)
                    }
                });
    }


    private void loadExpiredPromoCodes() {
        // Get current date
        DecimalFormat format = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
// Get year, month, and day
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // months are 0-indexed in Calendar, so add 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);
// Format the date as "dd/MM/yyyy"
        final String todayDate = format.format(day) + "/" + format.format(month) + "/" + year; // e.g. 29/06/2020

        promotionArrayList = new ArrayList<>();
        // Database reference for Users > current user > Promotions > codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear the list before adding new data
                        promotionArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Get the promotion data from the snapshot
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            String expDate = modelPromotion.getExpireDate();

                            try {
                                // Check for expired promotion
                                SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = sdformat.parse(todayDate);  // Today's date
                                Date expireDate = sdformat.parse(expDate);     // Promotion's expiration date

                                // Compare the dates
                                if (expireDate.compareTo(currentDate) > 0) {

                                } else if (expireDate.compareTo(currentDate) < 0) {
                                    promotionArrayList.add(modelPromotion);

                                } else if (expireDate.compareTo(currentDate) == 0) {
                                }
                            } catch (Exception e) {
                                e.printStackTrace();  // Handle parsing exception
                            }

                        }

                        // Set up the adapter with the updated list and attach it to the RecyclerView
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error (optional)
                    }
                });
    }

    private void loadNotExpiredPromoCodes() {
        // Get current date
        DecimalFormat format = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // months are 0-indexed in Calendar, so add 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        final String todayDate = format.format(day) + "/" + format.format(month) + "/" + year; // e.g. 29/06/2020

        promotionArrayList = new ArrayList<>();
        // Database reference for Users > current user > Promotions > codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear the list before adding new data
                        promotionArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Get the promotion data from the snapshot
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            String expDate = modelPromotion.getExpireDate();

                            try {
                                // Check for expired promotion
                                SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = sdformat.parse(todayDate);  // Today's date
                                Date expireDate = sdformat.parse(expDate);     // Promotion's expiration date

                                // Compare the dates
                                if (expireDate.compareTo(currentDate) > 0) {
                                    promotionArrayList.add(modelPromotion);
                                } else if (expireDate.compareTo(currentDate) < 0) {

                                } else if (expireDate.compareTo(currentDate) == 0) {
                                    promotionArrayList.add(modelPromotion);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        // Set up the adapter with the updated list and attach it to the RecyclerView
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this, promotionArrayList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error (optional)
                    }
                });
    }
}
