package com.example.grocery.activities;

import static android.webkit.WebView.findAddress;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.grocery.Constants;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderedItem;
import com.example.grocery.models.ModelOrderedItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsSellerActivity extends AppCompatActivity {
    private ImageButton backBtn, editBtn, mapBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, emailTv, phoneTv, totalItemsTv, amountTv, addressTv;
    private RecyclerView itemsRv;
    String orderId, orderBy;
    //to open destination in map
    String sourceLatitude, sourceLongitude,destinationLatitude,destinationLongitude;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize UI views
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        mapBtn = findViewById(R.id.mapBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRv);
        //get data from intent
        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();

        // Set up back button functionality
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity
            }
        });

        // Set up map button functionality
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();

            }
        });
        // Set up edit button functionality
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOrderStatusDialog();
            }
        });
    }

    private void editOrderStatusDialog() {
        // Options to display in dialog
        final String[] options = {" In Progress", " Completed", " Cancelled"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status:")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle item clicks
                        String selectedOption = options[which];
                        editOrderStatus(selectedOption);
                    }
                })
                .show();
    }


    private void editOrderStatus(final String selectedOption) {
        // Set up data to put in Firebase DB
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", ""+ selectedOption); // Update the order status

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String message = "Order is now " + selectedOption;
                        // Status updated
                        Toast.makeText(OrderDetailsSellerActivity.this, message, Toast.LENGTH_SHORT).show();
//                         Send notification to the buyer
                        prepareNotificationMessage(orderId, message);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed updating status, show reason
                        Toast.makeText(OrderDetailsSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadOrderedItems() {
        orderedItemArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
               .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ModelOrderedItem orderedItem = dataSnapshot.getValue(ModelOrderedItem.class);
                            orderedItemArrayList.add(orderedItem);
                        }
                        // Update RecyclerView with the ordered items
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsSellerActivity.this, orderedItemArrayList);
                        itemsRv.setAdapter(adapterOrderedItem);

                        //set total number of items/product in order
                        totalItemsTv.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        //load details info of this order,based on order id
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get order info
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


                        // Convert timestamp to formatted date, e.g., dd/MM/yyyy
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormatted = DateFormat.format("dd/MM/yyyy", calendar).toString();


                        //order status
                        if (orderStatus.equals("In Progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));

                        }
                        else if (orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));

                        }
                        else if (orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));

                        }
                        //set data
                        orderIdTv.setText(orderId);
                        dateTv.setText(dateFormatted);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("$" + orderCost + "[Including delivery fee $" + deliveryFee + "" + discount + " ]");
                        addressTv.setText(orderTo);

                        findAddress(latitude,longitude);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude); // Convert latitude to double
        double lon = Double.parseDouble(longitude); // Convert longitude to double

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            // Get address from latitude and longitude
            addresses = geocoder.getFromLocation(lat, lon, 1); // maxResults = 1 to get a single address
            if (addresses != null && !addresses.isEmpty()) {
                // Complete address
                String address = addresses.get(0).getAddressLine(0);
                addressTv.setText(address);
            } else {
                // Address not found
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Handle any exceptions
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "http://maps.google.com/maps?saddr=" + sourceLatitude+","+sourceLongitude+"&daddr=" + destinationLatitude+","+destinationLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadBuyerInfo() {
        // Firebase Database Reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        destinationLatitude = "" + snapshot.child("latitude").getValue();
                        destinationLongitude = "" + snapshot.child("longitude").getValue();
                        String email = ""+ snapshot.child("email").getValue();
                        String phone = ""+ snapshot.child("phone").getValue();

                        //set info
                        emailTv.setText(email);
                        phoneTv.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMyInfo() {
        // Firebase Database Reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sourceLatitude = "" + snapshot.child("latitude").getValue();
                        sourceLongitude = "" + snapshot.child("longitude").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void prepareNotificationMessage(String orderId,String message) {
        // When user seller change an order status InProgress/Completed/Cancelled, send notification to the buyer
        // Prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC; // Must be same as subscribed by user
        String NOTIFICATION_TITLE = "Your Order " + orderId;
        String NOTIFICATION_MESSAGE = ""+message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

        // Prepare JSON (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            // What to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", orderBy);
            notificationBodyJo.put("sellerUid", firebaseAuth.getUid()); // Assuming shopUid is defined and contains the seller's UID
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            // Where to send
            notificationJo.put("to", NOTIFICATION_TOPIC); // To all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo); // Payload

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {
        // Send Volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                "https://fcm.googleapis.com/fcm/send",
                notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
               //notification sent

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //notification failed to send

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