package com.example.grocery;

import androidx.annotation.NonNull;

import com.example.grocery.activities.OrderDetailsSellerActivity;
import com.example.grocery.activities.OrderDetailsUserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import java.util.Random;
import android.app.NotificationChannel;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Get data from notification
        String notificationType = message.getData().get("notificationType");

        if (notificationType != null) {
            String buyerUid = message.getData().get("buyerUid");
            String sellerUid = message.getData().get("sellerUid");
            String orderId = message.getData().get("orderId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationDescription = message.getData().get("notificationDescription");

            if (notificationType.equals("NewOrder") && firebaseUser != null && firebaseAuth.getUid().equals(sellerUid)) {
                // User is signed in and is the seller to whom the notification is sent
                showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationDescription, notificationType);
            } else if (notificationType.equals("OrderStatusChanged") && firebaseUser != null && firebaseAuth.getUid().equals(buyerUid)) {
                // User is signed in and is the buyer to whom the notification is sent
                showNotification(orderId, sellerUid, buyerUid, notificationTitle, notificationDescription, notificationType);
            }
        }
    }

    // Method to show notification
    private void showNotification(String orderId, String sellerUid, String buyerUid, String notificationTitle, String notificationDescription, String notificationType) {
        // Notification manager to show notifications
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Generate a unique notification ID based on orderId for specific notifications
        int notificationID = Integer.parseInt(orderId); // Using orderId as notification ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel(notificationManager);
        }

        // Handle notification click and start appropriate order activity
        Intent intent = null;

        if (notificationType.equals("NewOrder")) {
            // Open OrderDetailsSellerActivity
            intent = new Intent(this, OrderDetailsSellerActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderBy", buyerUid);
             // If the user is already in the activity, the new activity will not be launched
        } else if (notificationType.equals("OrderStatusChanged")) {
            // Open OrderDetailsUserActivity
            intent = new Intent(this, OrderDetailsUserActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderTo", sellerUid);
             // If the user is already in the activity, the new activity will not be launched
        }

        // Add flags for the intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationID, intent, PendingIntent.FLAG_IMMUTABLE);

        // Large icon for notification
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        // Sound of notification
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon) // Your small notification icon
                .setLargeIcon(largeIcon) // Set the large icon
                .setContentTitle(notificationTitle) // Title of the notification
                .setContentText(notificationDescription) // Description of the notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationDescription)) // Show full text
                .setSound(notificationSoundUri) // Set the notification sound
                .setContentIntent(pendingIntent) // Set the intent that will fire when the user taps the notification
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set priority
                .setAutoCancel(true); // Dismiss the notification when tapped

        // Show the notification
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Grocery Notifications";
        String channelDescription = "Channel for Grocery app notifications";

        NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
