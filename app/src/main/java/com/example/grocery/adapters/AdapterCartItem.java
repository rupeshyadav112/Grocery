package com.example.grocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModelCartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    // Constructor
    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row_cartitem.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem, parent, false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {
        // Get data
        ModelCartItem modelCartItem = cartItems.get(position);

        // Set data
        holder.itemTitleTv.setText(modelCartItem.getName());
        holder.itemPriceTv.setText("Total: $" + modelCartItem.getCost());
        holder.itemPriceEachTv.setText("Price: $" + modelCartItem.getPrice());
        holder.itemQuantityTv.setText("[" + modelCartItem.getQuantity() + "]"); // eg [3]

        // Handle remove item click
        holder.itemRemoveTv.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition(); // Get the current position
            if (currentPosition != RecyclerView.NO_POSITION) {
                // Proceed with your deletion logic
                EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                        .doneTableColumn();

                // Check if the item can be deleted
                if (easyDB.deleteRow("Item_Id", modelCartItem.getId())) {
                    Toast.makeText(context, "Removed from cart...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error: Could not remove item.", Toast.LENGTH_SHORT).show();
                    return; // Exit early if the deletion failed
                }

                // Refresh list
                cartItems.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                notifyItemRangeChanged(currentPosition, cartItems.size());

                // Adjust the subtotal after product remove
                double subTotalWithoutDiscount = ((ShopDetailsActivity) context).allTotalPrice;
                double subTotalAfterProductRemove = subTotalWithoutDiscount - getPriceWithoutDollar(modelCartItem.getCost());
                ((ShopDetailsActivity) context).allTotalPrice = subTotalAfterProductRemove;

                // Update the subtotal text view
                ((ShopDetailsActivity) context).sTotalTv.setText("$" + String.format("%.2f", ((ShopDetailsActivity) context).allTotalPrice));

                // Once subtotal is updated, check minimum order price of promo code
                double promoPrice = getPromoPrice();
                double deliveryFee = getDeliveryFee();
                double promoMinimumOrderPrice = getPromoMinimumOrderPrice();

                // Check if promo code applied
                if (((ShopDetailsActivity) context).isPromoCodeApplied) {
                    // If applied, check if the new subtotal meets the minimum order price
                    if (subTotalAfterProductRemove < promoMinimumOrderPrice) {
                        // Current order price is less than the minimum required price
                        Toast.makeText(context, "This code is valid for orders with a minimum amount: $" + promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
                        ((ShopDetailsActivity) context).applyBtn.setVisibility(View.GONE);
                        ((ShopDetailsActivity) context).promoDescriptionTv.setVisibility(View.GONE);
                        ((ShopDetailsActivity) context).discountTv.setText("$0");
                        ((ShopDetailsActivity) context).promoDescriptionTv.setText("");
                        ((ShopDetailsActivity) context).promoCodeEt.setText("");
                        ((ShopDetailsActivity) context).isPromoCodeApplied = false;
                        // Show new net total after adding delivery fee
                        ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", subTotalAfterProductRemove + deliveryFee));
                    } else {
                        // If promo code is applied and conditions are met
                        ((ShopDetailsActivity) context).applyBtn.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity) context).promoDescriptionTv.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity) context).promoDescriptionTv.setText(((ShopDetailsActivity) context).promoDescription);
                        // Show new total price after adding delivery fee and subtracting promo price
                        ((ShopDetailsActivity) context).isPromoCodeApplied = true;
                        double newTotalPrice = subTotalAfterProductRemove + deliveryFee - promoPrice;
                        ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", newTotalPrice));
                    }
                } else {
                    // If promo code is not applied
                    ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", subTotalAfterProductRemove + deliveryFee));
                }

                // After removing item from cart, update cart count
                ((ShopDetailsActivity) context).cartCount();
            } else {
                Toast.makeText(context, "Error: Item position not valid.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();  // Return list size
    }

    // View holder class
    public class HolderCartItem extends RecyclerView.ViewHolder {
        // UI views from row_cartitem.xml
        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv, itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            // Init views
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);
        }
    }

    // Utility method to safely parse the price value and handle errors
    private double getPriceWithoutDollar(String cost) {
        if (cost != null && !cost.trim().isEmpty()) {
            try {
                return Double.parseDouble(cost.replace("$", "").trim());
            } catch (NumberFormatException e) {
                // Handle invalid number format
                return 0;
            }
        }
        return 0;
    }

    private double getPromoPrice() {
        if (((ShopDetailsActivity) context).promoPrice != null && !((ShopDetailsActivity) context).promoPrice.trim().isEmpty()) {
            try {
                return Double.parseDouble(((ShopDetailsActivity) context).promoPrice.replace("$", "").trim());
            } catch (NumberFormatException e) {
                // Handle invalid number format
                return 0;
            }
        }
        return 0;
    }

    private double getDeliveryFee() {
        if (((ShopDetailsActivity) context).deliveryFee != null && !((ShopDetailsActivity) context).deliveryFee.trim().isEmpty()) {
            try {
                return Double.parseDouble(((ShopDetailsActivity) context).deliveryFee.replace("$", "").trim());
            } catch (NumberFormatException e) {
                // Handle invalid number format
                return 0;
            }
        }
        return 0;
    }

    private double getPromoMinimumOrderPrice() {
        if (((ShopDetailsActivity) context).promoMinimumOrderPrice != null && !((ShopDetailsActivity) context).promoMinimumOrderPrice.trim().isEmpty()) {
            try {
                return Double.parseDouble(((ShopDetailsActivity) context).promoMinimumOrderPrice.trim());
            } catch (NumberFormatException e) {
                // Handle invalid number format
                return 0;
            }
        }
        return 0;
    }
}
