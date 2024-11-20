package com.example.grocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.MainUserActivity;
import com.example.grocery.activities.OrderDetailsUserActivity;
import com.example.grocery.models.ModelOrderUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser> {

    private Context context;
    private ArrayList<ModelOrderUser> orderUserList;

    // Constructor
    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user, parent, false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position) {
        // Get data at the current position
        ModelOrderUser modelOrderUser = orderUserList.get(position);

        // Set data
        String orderId = modelOrderUser.getOrderId();
        String orderTime = modelOrderUser.getOrderTime();
        String orderBy = modelOrderUser.getOrderBy();
        String orderTo = modelOrderUser.getOrderTo();
        String orderCost = modelOrderUser.getOrderCost();
        String orderStatus = modelOrderUser.getOrderStatus();

        //get shop info
        loadShopInfo(modelOrderUser, holder);

        // Bind data to views
        holder.amountTv.setText("Amount: $" + orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("OrderId:" + orderId);
        //change order status text color
        if (orderStatus.equals("In Progress")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        } else if (orderStatus.equals("Completed")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        } else if (orderStatus.equals("Cancelled")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String date = android.text.format.DateFormat.format("dd/MM/yyyy", calendar).toString();
        holder.dateTv.setText(date);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details, we need to keys there, orderId, orderTo
                Intent intent = new Intent(context, OrderDetailsUserActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderTo", orderTo);
                context.startActivity(intent);
            }
        });
    }


    private void loadShopInfo(ModelOrderUser modelOrderUser, HolderOrderUser holder) {
        // Reference to the Users node in Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get the shop name from the database
                        String shopName = "" + dataSnapshot.child("shopName").getValue();
                        holder.shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle potential errors
                        Toast.makeText(context, "Failed to load shop info: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    // ViewHolder class
    class HolderOrderUser extends RecyclerView.ViewHolder {

        // Views of layout
        private TextView orderIdTv, dateTv, shopNameTv, amountTv, statusTv;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            // Initialize views of layout
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}


