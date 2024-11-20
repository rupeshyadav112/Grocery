package com.example.grocery.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.grocery.Constants;
import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt,descriptionEt;
    private TextView categoryTv,priceEt,quantityEt,discountedPriceEt,discountedNoteEt;
    private SwitchCompat discountSwitch;
    private Button updateProductBtn;

    private String productId;

    // Permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    private static final int STORAGE_REQUEST_CODE = 300;


    private String[] cameraPermission;
    private String[] mediaPermission;

    // Image picker URI
    private Uri imageUri;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        // Initialize UI components
        backBtn = findViewById(R.id.backBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        categoryTv = findViewById(R.id.categoryTv);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountSwitch = findViewById(R.id.discountSwitch);
        updateProductBtn = findViewById(R.id.updateProductBtn);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountedNoteEt = findViewById(R.id.discountedNoteEt);


        // Get the product ID from the intent
        productId = getIntent().getStringExtra("productId");

        // Hide discounted price fields initially
        discountedPriceEt.setVisibility(View.GONE);
        discountedNoteEt.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        loadProductDetails();//to set on view

        // Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Initialize permissions
        cameraPermission = new String[]{android.Manifest.permission.CAMERA};
        mediaPermission = new String[]{android.Manifest.permission.READ_MEDIA_IMAGES};


        // Handle discount switch
        discountSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                discountedPriceEt.setVisibility(View.VISIBLE);
                discountedNoteEt.setVisibility(View.VISIBLE);
            } else {
                discountedPriceEt.setVisibility(View.GONE);
                discountedNoteEt.setVisibility(View.GONE);
            }
        });

        // Back button listener
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productIconIv.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //show dialog  to pick image
                showImagePickDialog();
            }
        });
        categoryTv.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //pick category
                categoryDialog();
            }
        });

        // Update product button click listener
        updateProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //flow:
                //1)Input data
                //2)Validate data
                //3)Update data to db
                inputData();
            }
        });
    }

    private void loadProductDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get data
                        String productId = "" + dataSnapshot.child("productId").getValue();
                        String productTitle = "" + dataSnapshot.child("productTitle").getValue();
                        String productDescription = "" + dataSnapshot.child("productDescription").getValue();
                        String productCategory = "" + dataSnapshot.child("productCategory").getValue();
                        String productQuantity = "" + dataSnapshot.child("productQuantity").getValue();
                        String productIcon = "" + dataSnapshot.child("productIcon").getValue();
                        String originalPrice = "" + dataSnapshot.child("originalPrice").getValue();
                        String discountPrice = "" + dataSnapshot.child("discountPrice").getValue();
                        String discountNote = "" + dataSnapshot.child("discountNote").getValue();
                        String discountAvailable = "" + dataSnapshot.child("discountAvailable").getValue();
                        String timestamp = "" + dataSnapshot.child("timestamp").getValue();
                        String uid = "" + dataSnapshot.child("uid").getValue();


                        //set data to view
                        if(discountAvailable.equals("true")){
                            discountSwitch.setChecked(true);

                            discountedPriceEt.setVisibility(View.VISIBLE);
                            discountedNoteEt.setVisibility(View.VISIBLE);
                        }
                        else{
                            discountSwitch.setChecked(false);

                            discountedPriceEt.setVisibility(View.GONE);
                            discountedNoteEt.setVisibility(View.GONE);

                        }
                        titleEt.setText(productTitle);
                        descriptionEt.setText(productDescription);
                        discountedNoteEt.setText(discountNote);
                        categoryTv.setText(productCategory);
                        quantityEt.setText(productQuantity);
                        priceEt.setText(originalPrice);
                        discountedPriceEt.setText(discountPrice);

                        try {
                            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_white).into(productIconIv);
                        } catch (Exception e) {
                            productIconIv.setImageResource(R.drawable.ic_add_shopping_white);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData() {
        //1)Input data
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();

        // Validate data
        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(this, "Please enter product title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(this, "Please enter product price", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(this, "Please select product category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (discountAvailable) {
            discountPrice = discountedPriceEt.getText().toString().trim();
            discountNote = discountedNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Please enter discount price", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            discountPrice = "0";
            discountNote = "";
        }

        // Proceed to update the product in Firebase Database
        updateProduct();
    }
    private void updateProduct() {
        // Show progress
        progressDialog.setMessage("Updating product...");
        progressDialog.show();

        if (imageUri == null) {
            // Update without image

            // Set up data in HashMap to update
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productTitle", ""+ productTitle);
            hashMap.put("productDescription", ""+productDescription);
            hashMap.put("productCategory", ""+productCategory);
            hashMap.put("discountNote", ""+discountNote);
            hashMap.put("productQuantity", ""+productQuantity);
            hashMap.put("originalPrice", ""+originalPrice);
            hashMap.put("discountPrice", ""+discountPrice);
            hashMap.put("discountAvailable", ""+discountAvailable);

            // Update to database
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Update success
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Update Successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Failed to update.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else{
            //update with image
            // First upload image
            String filePathAndName = "product_images/" + ""+ productId;
            // Upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded, get URL of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();
                            if (uriTask.isSuccessful()) {
                                // Setup data in HashMap to update
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productTitle", ""+productTitle);
                                hashMap.put("productDescription", ""+productDescription);
                                hashMap.put("productCategory", ""+productCategory);
                                hashMap.put("productIcon", ""+downloadImageUri); // Save the image URL
                                hashMap.put("productQuantity", ""+productQuantity);
                                hashMap.put("originalPrice", ""+originalPrice);
                                hashMap.put("discountPrice", ""+discountPrice);
                                hashMap.put("discountNote", ""+discountNote);
                                hashMap.put("discountAvailable", ""+discountAvailable);

                                // Update to database
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Update success
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Update Successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle failure
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Failed to update.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure in uploading image
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void categoryDialog() {
        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        //get picked category
                        String category = Constants.productCategories[which];

                        //set picked category
                        categoryTv.setText(category);
                    }
                });
        builder .show();
    }

    // Method to show image selection dialog
    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (checkCameraPermission()) {
                    pickFromCamera();
                } else {
                    requestCameraPermission();
                }
            } else {
                pickFromGallery();
            }
        });
        builder.create().show();
    }
    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "New Pick");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }


    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }


    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        // Camera permission granted
                        pickFromCamera();
                    } else {
                        // Camera permission denied
                        Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        // Storage permission granted
                        pickFromGallery();
                    } else {
                        // Storage permission denied
                        Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //handle image pick result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                imageUri = data.getData();
                productIconIv.setImageURI(imageUri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image picked from camera
                productIconIv.setImageURI(imageUri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}

