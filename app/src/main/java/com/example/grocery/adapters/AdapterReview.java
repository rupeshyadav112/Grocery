package com.example.grocery.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.models.ModelReview;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview> {
    private Context context;
    private ArrayList<ModelReview> reviewArrayList;

    public AdapterReview(Context context, ArrayList<ModelReview> reviewArrayList) {
        this.context = context;
        this.reviewArrayList = reviewArrayList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row layout for each review item
        View view = LayoutInflater.from(context).inflate(R.layout.row_review, parent, false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        // Get data for the current review
        ModelReview modelReview = reviewArrayList.get(position);

        // Bind data to the views
        String uid = modelReview.getUid();
        String review = modelReview.getReview();
        String ratings = modelReview.getRatings();
        String timestamp = modelReview.getTimestamp();

        // We also need info (profile image, name) of the user who wrote the review
        loadUserDetail(modelReview, holder);

        // Convert timestamp to proper format dd/MM/yyyy
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString();

        // Set data to the views
        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.reviewTv.setText(review);
        holder.dateTv.setText(dateFormat);

    }

    private void loadUserDetail(ModelReview modelReview, HolderReview holder) {
        // UID of the user who wrote the review
        String uid = modelReview.getUid();

        // Fetch user info from Firebase Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get user info, use the same key names as in Firebase
                String name = "" + dataSnapshot.child("name").getValue();
                String profileImage = "" + dataSnapshot.child("profileImage").getValue();

                // Set the user's name and profile image to the view holder
                holder.nameTv.setText(name);
                try {
                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(holder.profileIv);
                } catch (Exception e) {
                    holder.profileIv.setImageResource(R.drawable.ic_store_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors here (if needed)
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewArrayList.size();
    }

    // View holder class that holds/inits views of recyclerview item
    class HolderReview extends RecyclerView.ViewHolder {
        // UI views of layout row_review
        private ImageView profileIv;
        private TextView nameTv, dateTv, reviewTv;
        private RatingBar ratingBar;

        public HolderReview(@NonNull View itemView) {
            super(itemView);
            // Initialize UI views
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            reviewTv = itemView.findViewById(R.id.reviewTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
