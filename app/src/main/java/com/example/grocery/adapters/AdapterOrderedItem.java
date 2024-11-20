package com.example.grocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.models.ModelCartItem;
import com.example.grocery.models.ModelOrderedItem;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem>{
    private Context context;
    private ArrayList<ModelOrderedItem> orderedItemArrayList;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderedItem> orderedItemArrayList) {
        this.context = context;
        this.orderedItemArrayList = orderedItemArrayList;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordereditem,parent,false);
        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {

        // Get data at the specified position
        ModelOrderedItem modelOrderedItem = orderedItemArrayList.get(position);
        String getpId = modelOrderedItem.getpId();
        String name = modelOrderedItem.getName();
        String cost = modelOrderedItem.getCost();
        String price = modelOrderedItem.getPrice();
        String quantity = modelOrderedItem.getQuantity();

        // Set data to the holder views
        holder.itemTitleTv.setText(name);
        holder.itemPriceEachTv.setText("$" + price);
        holder.itemPriceTv.setText("$" + cost);
        holder.itemQuantityTv.setText("[" + quantity + "]");

    }

    @Override
    public int getItemCount() {
        return orderedItemArrayList.size();//return list size
    }

    //view holder class
    class HolderOrderedItem extends RecyclerView.ViewHolder{
        //views of row_ordered_item
         private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv;


        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);
            //init view
            itemTitleTv= itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv= itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv= itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv= itemView.findViewById(R.id.itemQuantityTv);
        }


    }
}
