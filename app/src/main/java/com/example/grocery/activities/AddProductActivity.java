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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.util.HashMap;


public class AddProductActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt, descriptionEt,discountedPriceEt,discountedNoteEt,quantityEt,priceEt;
    private TextView categoryTv;
    private SwitchCompat discountSwitch;
    private Button addProductBtn;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
//init ui views
        backBtn = findViewById(R.id.backBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        categoryTv = findViewById(R.id.categoryTv);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountSwitch = findViewById(R.id.discountSwitch);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountedNoteEt = findViewById(R.id.discountedNoteEt);
        addProductBtn = findViewById(R.id.addProductBtn);

        //unchecked, hide discountPriceEt, discountNoteEt
        discountedPriceEt.setVisibility(View.GONE);
        discountedNoteEt.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        cameraPermission = new String[]{android.Manifest.permission.CAMERA};
        mediaPermission = new String[]{android.Manifest.permission.READ_MEDIA_IMAGES};
        //if discountSwitch is enabled: show discountPriceEt,discountNoteEt | if discountSwitch is disabled: hide discountPriceEt,discountNoteEt
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //checked, show discountPriceEt,discountNoteEt
                    discountedPriceEt.setVisibility(View.VISIBLE);
                    discountedNoteEt.setVisibility(View.VISIBLE);
                } else {
                    //unchecked, hide discountPriceEt, discountNoteEt
                    discountedPriceEt.setVisibility(View.GONE);
                    discountedNoteEt.setVisibility(View.GONE);
                }
            }
        });

        //pick image from gallery or camera
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
        addProductBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //flow:
                //1.input data
                //2.validate data
                //3.Add data to db
               inputData();

            }
        });


    }
private String productTitle,productDescription,productCategory,productQuantity,originalPrice,discountPrice,discountNote;
    private boolean discountAvailable = false;
    private void inputData() {
        //1.input data
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();
        //2.validate data
        if(productTitle.isEmpty()){
            Toast.makeText(this, "Please enter product title", Toast.LENGTH_SHORT).show();
            return;
        }
        if(originalPrice.isEmpty()){
            Toast.makeText(this, "Please enter product price", Toast.LENGTH_SHORT).show();
            return;
        }
        if(productCategory.isEmpty()) {
            Toast.makeText(this, "Please select product category", Toast.LENGTH_SHORT).show();
            return;
        }
            if (discountAvailable){
                //product is with discount
                discountPrice = discountedPriceEt.getText().toString().trim();
                discountNote = discountedNoteEt.getText().toString().trim();
                if(discountPrice.isEmpty()){
                    Toast.makeText(this, "Please enter discount price", Toast.LENGTH_SHORT).show();
                    return;
            }
    }
            else{
                //product is without discount
                discountPrice = "0";
                discountNote = "";
            }
            //3.Add data to db
            addProduct();
            }

    private void addProduct() {
        progressDialog.setMessage("Adding Product...");
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());

        if (imageUri == null) {
            // Upload without image
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId", "" + timestamp);
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + productDescription);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("productIcon", ""); // No image, so an empty string
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountAvailable", "" + discountAvailable);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("uid", "" + firebaseAuth.getUid());

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.e("AddProductActivity", "Failed to add product: " + e.getMessage());
                            Toast.makeText(AddProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Upload with image
            Log.d("AddProductActivity", "Uploading image: " + imageUri);
            StorageReference ref = FirebaseStorage.getInstance().getReference("product_images/" + timestamp);
            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded successfully
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadImageUrl) {
                                    // URL of image received, now add product data to Firebase database
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("productId", "" + timestamp);
                                    hashMap.put("productTitle", "" + productTitle);
                                    hashMap.put("productDescription", "" + productDescription);
                                    hashMap.put("productCategory", "" + productCategory);
                                    hashMap.put("productQuantity", "" + productQuantity);
                                    hashMap.put("productIcon", "" + downloadImageUrl.toString()); // Image URL
                                    hashMap.put("originalPrice", "" + originalPrice);
                                    hashMap.put("discountAvailable", "" + discountAvailable);
                                    hashMap.put("discountPrice", "" + discountPrice);
                                    hashMap.put("discountNote", "" + discountNote);
                                    hashMap.put("uid", "" + firebaseAuth.getUid());

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                    ref.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(AddProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                                                    clearData();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Log.e("AddProductActivity", "Failed to add product: " + e.getMessage());
                                                    Toast.makeText(AddProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Log.e("AddProductActivity", "Failed to get image URL: " + e.getMessage());
                                    Toast.makeText(AddProductActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.e("Upload Error", e.getMessage()); // Log the error
                            Toast.makeText(AddProductActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    private void clearData() {
        //clear data after uploading product
        titleEt.setText("");
        descriptionEt.setText("");
        categoryTv.setText("");
        quantityEt.setText("");
        priceEt.setText("");
        discountedPriceEt.setText("");
        discountedNoteEt.setText("");
        imageUri = null;
        productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);

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
        builder.show();
    }

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
//complete code

