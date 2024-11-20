package com.example.grocery.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddPromotionCodeActivity extends AppCompatActivity {
    // Declare views
    private ImageButton backBtn;
    private EditText promoCodeEt, promoDescriptionEt, promoPriceEt, minimumOrderPriceEt;
    private TextView expireDateTv,titleTv;
    private Button addBtn;

    //firebase auth
     FirebaseAuth firebaseAuth;
    //progress dialog
    ProgressDialog progressDialog;
    private String promoId;
    private boolean isUpdating = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion_code);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize views
        backBtn = findViewById(R.id.backBtn);
        promoCodeEt = findViewById(R.id.promoCodeEt);
        promoDescriptionEt = findViewById(R.id.promoDescriptionEt);
        promoPriceEt = findViewById(R.id.promoPriceEt);
        minimumOrderPriceEt = findViewById(R.id.minimumOrderPriceEt);
        expireDateTv = findViewById(R.id.expireDateTv);
        addBtn = findViewById(R.id.addBtn);
        titleTv = findViewById(R.id.titleTv);

        // Initialize firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Get promo id from intent
        Intent intent = getIntent();
        if (intent.getStringExtra("promoId") != null) {
            // Came here from adapter to update record
            promoId = intent.getStringExtra("promoId");
            titleTv.setText("Update Promotion Code");
            addBtn.setText("Update");
            isUpdating = true;
            loadPromoInfo(); // Load promotion code info to set in our views, so we can also update a single value
        } else {
            // Came here from promo codes list activity to add new promo code
            titleTv.setText("Add Promotion Code");
            addBtn.setText("Add");
            isUpdating = false;
        }


        // Set up listeners or any other logic for the views
        backBtn.setOnClickListener(v -> onBackPressed());

        //handle click,pick date
        expireDateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickDialog();
            }
        });

        addBtn.setOnClickListener(v -> {

           inputData();
        });


    }


    private void loadPromoInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String id = "" + snapshot.child("id").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String promoCode = "" + snapshot.child("promoCode").getValue();
                        String promoPrice = "" + snapshot.child("promoPrice").getValue();
                        String minimumOrderPrice = "" + snapshot.child("minimumOrderPrice").getValue();
                        String expireDate = "" + snapshot.child("expireDate").getValue();

                        promoCodeEt.setText(promoCode);
                        promoDescriptionEt.setText(description);
                        promoPriceEt.setText(promoPrice);
                        minimumOrderPriceEt.setText(minimumOrderPrice);
                        expireDateTv.setText(expireDate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }


    private void datePickDialog() {
        // Get current date to set on calendar
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        // Date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        DecimalFormat mFormat = new DecimalFormat("00");
                        String pDay = mFormat.format(dayOfMonth);
                        String pMonth = mFormat.format(monthOfYear + 1); // Month is 0-indexed
                        String pYear = "" + year;
                        String pDate = pDay + "/" + pMonth + "/" + pYear; // e.g. 27/06/2020
                        expireDateTv.setText(pDate);
                    }
                }, mYear, mMonth, mDay
        );

        // Show dialog
        datePickerDialog.show();

        // Disable past dates selection on calendar
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }

    private String description, promoCode, promoPrice, minimumOrderPrice, expireDate;

    private void inputData() {
        // Input data
        promoCode = promoCodeEt.getText().toString().trim();
        description = promoDescriptionEt.getText().toString().trim();
        promoPrice = promoPriceEt.getText().toString().trim();
        minimumOrderPrice = minimumOrderPriceEt.getText().toString().trim();
        expireDate = expireDateTv.getText().toString().trim();
        // Validate form data
        if (promoCode.isEmpty()) {
            Toast.makeText(this, "Please enter discount code", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }
        if (promoPrice.isEmpty()) {
            Toast.makeText(this, "Please enter a promotion price", Toast.LENGTH_SHORT).show();
            return;
        }
        if (minimumOrderPrice.isEmpty()) {
            Toast.makeText(this, "Please enter a minimum order price", Toast.LENGTH_SHORT).show();
            return;
        }
        if (expireDate.isEmpty()) {
            Toast.makeText(this, "Please select an expiry date", Toast.LENGTH_SHORT).show();
            return;
        }


        // all fields entered, add/update data to db
        if (isUpdating) {
            updateDataToDb();
        } else {
            addDataToDb();
        }
    }

    private void updateDataToDb() {
        // Show progress dialog
        progressDialog.setMessage("Updating promotion code...");
        progressDialog.show();
        String timestamp = "" + System.currentTimeMillis();
        // Setup data to add to the database
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("promoCode", promoCode);
        dataMap.put("description", description);
        dataMap.put("promoPrice", promoPrice);
        dataMap.put("minimumOrderPrice", minimumOrderPrice);
        dataMap.put("expireDate", expireDate);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Add the data to the database using the timestamp as the unique key
        ref.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .updateChildren(dataMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                    //update
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "updated successfully", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    //failed to update
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "Failed to update promotion code: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }


    private void addDataToDb() {
        // Show progress dialog
        progressDialog.setMessage("Adding promotion code...");
        progressDialog.show();
        String timestamp = ""+System.currentTimeMillis();
        // Setup data to add to the database
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", timestamp);
        dataMap.put("promoCode", promoCode);
        dataMap.put("description", description);
        dataMap.put("promoPrice", promoPrice);
        dataMap.put("minimumOrderPrice", minimumOrderPrice);
        dataMap.put("expireDate", expireDate);
        dataMap.put("timestamp", timestamp);

        // Assuming you are using Firebase Realtime Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Add the data to the database using the timestamp as the unique key
        ref.child(firebaseAuth.getUid()).child("Promotions").child(timestamp)
                .setValue(dataMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Dismiss progress dialog
                        progressDialog.dismiss();
                        // Notify user of success
                        Toast.makeText(AddPromotionCodeActivity.this, "Promotion code added successfully", Toast.LENGTH_SHORT).show();
                        // Clear form fields or finish activity as needed
                        clearForm();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Dismiss progress dialog
                        progressDialog.dismiss();
                        // Notify user of failure
                        Toast.makeText(AddPromotionCodeActivity.this, "Failed to add promotion code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Clear form method to reset the fields after adding the promo code
    private void clearForm() {
        promoCodeEt.setText("");
        promoDescriptionEt.setText("");
        promoPriceEt.setText("");
        minimumOrderPriceEt.setText("");
        expireDateTv.setText("");
    }
    }



