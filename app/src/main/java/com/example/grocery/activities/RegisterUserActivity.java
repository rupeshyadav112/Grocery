package com.example.grocery.activities;

import static android.webkit.WebView.findAddress;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import android.Manifest;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.grocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {

    private ImageButton backBtn, gpsBtn;
    private ImageView profileIv;
    private EditText nameEt, phoneEt, countryEt, stateEt, cityEt, addressEt, emailEt, passwordEt, cPasswordEt;
    private Button registerBtn;
    private TextView registerSellerTv;

    // Permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int MEDIA_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    // Permission arrays
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] mediaPermission;

    // Image picker URI
    private Uri imageUri;
    private double latitude = 0.0, longitude = 0.0;
    private LocationManager locationManager;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        initializeUI();


        // Initialize permission arrays
        locationPermission = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{android.Manifest.permission.CAMERA};
        mediaPermission = new String[]{android.Manifest.permission.READ_MEDIA_IMAGES};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(view -> onBackPressed());

        gpsBtn.setOnClickListener(view -> {
            if (checkLocationPermission()) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    new AlertDialog.Builder(RegisterUserActivity.this)
                            .setTitle("Enable GPS")
                            .setMessage("GPS is not enabled. Please enable it to detect your location.")
                            .setPositiveButton("Settings", (dialogInterface, i) -> {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                            .show();
                } else {
                    detectLocation(); // Request location detection when everything is enabled
                }
            } else {
                requestLocationPermission();
            }
        });

        profileIv.setOnClickListener(view -> showImagePickDialog());

        registerBtn.setOnClickListener(view -> inputData());

        registerSellerTv.setOnClickListener(view -> startActivity(new Intent(RegisterUserActivity.this, RegisterSellerActivity.class)));
    }

    private void detectLocation() {
        Toast.makeText(this, "Please Wait...", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    private void showEnableGPSDialog() {
        new AlertDialog.Builder(RegisterUserActivity.this)
                .setTitle("Enable GPS")
                .setMessage("GPS is not enabled. Please enable it to detect your location.")
                .setPositiveButton("Settings", (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void initializeUI() {
        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        cPasswordEt = findViewById(R.id.cPasswordEt);
        registerBtn = findViewById(R.id.registerBtn);
        registerSellerTv = findViewById(R.id.registerSellerTv);
    }

    private void inputData() {
        String fullName = nameEt.getText().toString().trim();
        String phoneNumber = phoneEt.getText().toString().trim();
        String country = countryEt.getText().toString().trim();
        String state = stateEt.getText().toString().trim();
        String city = cityEt.getText().toString().trim();
        String address = addressEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();
        String confirmPassword = cPasswordEt.getText().toString().trim();

        // Define account type, assuming it's set based on a toggle or some logic
        String accountType = "user"; // Set this based on user selection (e.g., "seller" or "buyer")

        // Check for empty fields
        if (TextUtils.isEmpty(fullName)) {
            showToast("Enter Name...");
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            showToast("Enter Phone Number...");
            return;
        }
        if (!isValidPhoneNumber(phoneNumber)) {
            showToast("Phone number must be 10 digits...");
            return;
        }
        if (TextUtils.isEmpty(country)) {
            showToast("Enter Country...");
            return;
        }
        if (TextUtils.isEmpty(state)) {
            showToast("Enter State...");
            return;
        }
        if (TextUtils.isEmpty(city)) {
            showToast("Enter City...");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            showToast("Enter Address...");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            showToast("Enter Email...");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showToast("Enter Password...");
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            showToast("Confirm Your Password...");
            return;
        }

        // Check GPS location
        if (latitude == 0.0 || longitude == 0.0) {
            showToast("Please click GPS button to detect location...");
            return;
        }

        // Email format validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid Email");
            return;
        }

        // Password length validation
        if (password.length() < 6) {
            showToast("Password must be at least 6 characters...");
            return;
        }

        // Password confirmation validation
        if (!password.equals(confirmPassword)) {
            showToast("Passwords don't match...");
            return;
        }

        // If all validations pass, create the account
        createAccount(fullName, phoneNumber, country, state, city, address, email, password, accountType);
    }


    // Method to validate phone number
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 10 && TextUtils.isDigitsOnly(phoneNumber);
    }


    private void createAccount(String fullName, String phoneNumber, String country, String state, String city, String address, String email, String password, String accountType) {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> saveFirebaseData(fullName, phoneNumber, country, state, city, address, email, accountType))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showToast(e.getMessage());
                });
    }

    private void saveFirebaseData(String fullName, String phoneNumber, String country, String state, String city, String address, String email, String accountType) {
        progressDialog.setMessage("Saving Account Info...");
        String timestamp = "" + System.currentTimeMillis();
        if (imageUri != null) {
            uploadProfileImage(timestamp, fullName, phoneNumber, country, state, city, address, email, accountType);
        } else {
            saveDataToDatabase(fullName, phoneNumber, country, state, city, address, email, "", accountType);
        }
    }

    private void uploadProfileImage(String timestamp, String fullName, String phoneNumber, String country, String state, String city, String address, String email, String accountType) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Profile Images");
        StorageReference filePath = storageReference.child(timestamp);
        filePath.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> saveDataToDatabase(fullName, phoneNumber, country, state, city, address, email, uri.toString(), accountType)))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showToast(e.getMessage());
                });
    }

    private void saveDataToDatabase(String fullName, String phoneNumber, String country, String state, String city, String address, String email, String profileImageUrl, String accountType) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        String uid = firebaseAuth.getCurrentUser().getUid();

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("fullName", fullName);
        userData.put("phone", phoneNumber);
        userData.put("country", country);
        userData.put("state", state);
        userData.put("city", city);
        userData.put("address", address);
        userData.put("email", email);
        userData.put("profileImage", profileImageUrl);
        userData.put("latitude", latitude);
        userData.put("longitude", longitude);
        userData.put("accountType", accountType); // Add accountType to user data

        databaseReference.child(uid).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    showToast("Account Created...");
                    startActivity(new Intent(RegisterUserActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showToast(e.getMessage());
                });
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

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call super method
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectLocation();
            } else {
                showToast("Location permission is required.");
            }
        }

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromCamera();
            } else {
                showToast("Camera permission is required.");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                imageUri = data.getData();
                profileIv.setImageURI(imageUri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                profileIv.setImageURI(imageUri);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        findAddress();

        // Stop location updates to save battery
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }


    private void findAddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0); // Get the first address
                // Update the address EditText with the retrieved address
                StringBuilder addressString = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressString.append(address.getAddressLine(i)).append("\n");
                }
                addressEt.setText(addressString.toString().trim());
                countryEt.setText(address.getCountryName());
                stateEt.setText(address.getAdminArea());
                cityEt.setText(address.getLocality());// Set the address to the EditText
            } else {
                showToast("Unable to find address.");
            }
        } catch (Exception e) {
            showToast("Geocoder service is not available.");
        }
    }




    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // GPS/location disabled
        new AlertDialog.Builder(this)
                .setTitle("Location Disabled")
                .setMessage("Please turn on location services to allow this app to detect your location.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    // Open location settings
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        showToast("Location Provider Enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle status change if necessary
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }


}
