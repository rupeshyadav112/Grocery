package com.example.grocery.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grocery.R;
import com.example.grocery.activities.AddPromotionCodeActivity;
import com.example.grocery.models.ModelPromotion;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterPromotionShop extends RecyclerView.Adapter<AdapterPromotionShop.HolderPromotionShop> {

    private Context context;
    private ArrayList<ModelPromotion> promotionArrayList;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    public AdapterPromotionShop(Context context, ArrayList<ModelPromotion> promotionArrayList) {
        this.context = context;
        this.promotionArrayList = promotionArrayList;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderPromotionShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate row_promotion_shop.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_promotion_shop, parent, false);
        return new HolderPromotionShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPromotionShop holder, int position) {
        // Get data from the model at the current position
        ModelPromotion modelPromotion = promotionArrayList.get(position);
        String id = modelPromotion.getId();
        String timestamp = modelPromotion.getTimestamp();
        String description = modelPromotion.getDescription();
        String promoCode = modelPromotion.getPromoCode();
        String promoPrice = modelPromotion.getPromoPrice();
        String expireDate = modelPromotion.getExpireDate();
        String minimumOrderPrice = modelPromotion.getMinimumOrderPrice();

// Set data to the view holder
        holder.descriptionTv.setText(description);
        holder.promoPriceTv.setText(promoPrice);
        holder.minimumOrderPriceTv.setText(minimumOrderPrice);
        holder.promoCodeTv.setText("Code: " + promoCode);
        holder.expireDateTv.setText("Expire Date: " + expireDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDeleteDialog(modelPromotion,holder);
            }
        });


    }

    private void editDeleteDialog(final ModelPromotion modelPromotion, HolderPromotionShop holder) {
        // options to display in dialog
        String[] options = {"Edit", "Delete"};

        // dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Option")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // handle clicks
                        if (i == 0) {
                            // Edit clicked
                            editPromoCode(modelPromotion);
                        } else if (i == 1) {
                            // Delete clicked
                            deletePromoCode(modelPromotion);
                        }
                    }
                })
                .show();  // Show the dialog
    }

    private void deletePromoCode(ModelPromotion modelPromotion) {
        progressDialog.setMessage("Deleting Promotion Code...");
        progressDialog.show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(modelPromotion.getId())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void editPromoCode(ModelPromotion modelPromotion) {
        Intent intent = new Intent(context, AddPromotionCodeActivity.class);
        intent.putExtra("promoId", modelPromotion.getId());
        context.startActivity(intent);
    }



    @Override
    public int getItemCount() {
        return promotionArrayList.size();
    }

    // ViewHolder class to hold UI views for row_promotion_shop
    class HolderPromotionShop extends RecyclerView.ViewHolder {

        // UI Views of row_promotion_shop
        private ImageView iconIv;
        private TextView promoCodeTv, promoPriceTv, minimumOrderPriceTv, expireDateTv, descriptionTv;

        public HolderPromotionShop(@NonNull View itemView) {
            super(itemView);

            // Initialize UI Views from row_promotion_shop.xml
            iconIv = itemView.findViewById(R.id.iconIv);
            promoCodeTv = itemView.findViewById(R.id.promoCodeTv);
            promoPriceTv = itemView.findViewById(R.id.promoPriceTv);
            minimumOrderPriceTv = itemView.findViewById(R.id.minimumOrderPriceTv);
            expireDateTv = itemView.findViewById(R.id.expireDateTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
        }
    }
}
