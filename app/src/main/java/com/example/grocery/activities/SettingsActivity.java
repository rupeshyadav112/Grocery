package com.example.grocery.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.grocery.Constants;
import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private SwitchCompat fcmSwitch;
    private TextView notificationStatusTv;

    private static final String enabledMessage = "Notifications are enabled";
    private static final String disabledMessage = "Notifications are disabled";

    private boolean isChecked = false;

    private FirebaseAuth firebaseAuth;

    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        backBtn = findViewById(R.id.backBtn);
        fcmSwitch = findViewById(R.id.fcmSwitch);
        notificationStatusTv = findViewById(R.id.notificationStatusTv);

        firebaseAuth = FirebaseAuth.getInstance();
        sp = getSharedPreferences("SETTINGS_SP", MODE_PRIVATE);
        isChecked = sp.getBoolean("FCM_ENABLED", false);
        fcmSwitch.setChecked(isChecked);
        if (isChecked) {
            notificationStatusTv.setText(enabledMessage);
        } else {
            notificationStatusTv.setText(disabledMessage);
        }


        backBtn.setOnClickListener(v -> onBackPressed());

        // Set listener for the SwitchCompat (enable/disable notifications)
        fcmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Notifications enabled
                subscribeToTopic();
                notificationStatusTv.setText(enabledMessage);
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Notifications disabled
                unsubscribeToTopic();
                notificationStatusTv.setText(disabledMessage);
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Set the initial state of the switch and notification status
        boolean areNotificationsEnabled = getNotificationStatus(); // Example method to retrieve status
        fcmSwitch.setChecked(areNotificationsEnabled);
        notificationStatusTv.setText(areNotificationsEnabled ? enabledMessage : disabledMessage);
    }

    private boolean getNotificationStatus() {
        // Placeholder logic; replace with actual logic to check notification status
        return false; // Default: Notifications are disabled
    }

    // Subscribe to the FCM topic
    private void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Successfully subscribed to the topic
                        //save setting ins shared preferences
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED", true);
                        spEditor.apply();
                        Toast.makeText(SettingsActivity.this, ""+enabledMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(enabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to subscribe to the topic
                        Toast.makeText(SettingsActivity.this, "" +disabledMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Unsubscribe from the FCM topic
    private void unsubscribeToTopic() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Unsubscribed from the topic
                        //save setting ins shared preferences
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED", false);
                        spEditor.apply();
                        Toast.makeText(SettingsActivity.this, "Unsubscribed from notifications", Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(disabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to unsubscribe from the topic
                        Toast.makeText(SettingsActivity.this, "Unsubscription failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
