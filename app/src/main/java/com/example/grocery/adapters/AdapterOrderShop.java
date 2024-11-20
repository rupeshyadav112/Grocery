package com.example.grocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.FilterOrderShop;
import com.example.grocery.R;
import com.example.grocery.activities.OrderDetailsSellerActivity;
import com.example.grocery.models.ModelOrderShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable {

    private Context context;
    public ArrayList<ModelOrderShop> orderShopArrayList,filterList;;
    private FilterOrderShop filter;

    // Constructor
    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> orderShopArrayList) {
        this.context = context;
        this.orderShopArrayList = orderShopArrayList;
        this.filterList = orderShopArrayList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout row_order_seller.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller, parent, false);
        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {
        //get data at position
        ModelOrderShop modelOrderShop = orderShopArrayList.get(position);
        final String orderId = modelOrderShop.getOrderId();
        final String orderBy = modelOrderShop.getOrderBy();
        String orderCost = modelOrderShop.getOrderCost();
        String orderStatus = modelOrderShop.getOrderStatus();
        String orderTime = modelOrderShop.getOrderTime();
        String orderTo = modelOrderShop.getOrderTo();


        //load user/buyer info
        loadUserInfo(modelOrderShop, holder);


        // Set data
        holder.orderIdTv.setText("Order ID" +orderId);
        holder.amountTv.setText("Amount: $"+orderCost);
        holder.statusTv.setText(orderStatus);

        // Set order status color
        if (orderStatus.equals("In Progress")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        } else if (orderStatus.equals("Completed")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        } else if (orderStatus.equals("Cancelled")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
        //convert time to proper format e.g. dd/MM/yyyy
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formattedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();
        holder.orderDateTv.setText(formattedDate);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open order details
                Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderBy", orderBy);
                context.startActivity(intent);
            }
        });
    }

    private void loadUserInfo(ModelOrderShop modelOrderShop, final HolderOrderShop holder) {
        //to load email of the  user/buyer
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderShop.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = "" + snapshot.child("email").getValue();
                        holder.emailTv.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return orderShopArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterOrderShop(this, filterList);
        }
        return filter;
    }

    // ViewHolder class
    class HolderOrderShop extends RecyclerView.ViewHolder {

        private TextView orderIdTv, orderDateTv, emailTv, amountTv, statusTv;


        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
