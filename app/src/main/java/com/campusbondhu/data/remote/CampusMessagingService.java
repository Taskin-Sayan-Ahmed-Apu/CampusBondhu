package com.campusbondhu.data.remote;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class CampusMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        // Save to in-app notification history if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("title", remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "CampusBondhu");
            notif.put("body", remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "");
            notif.put("timestamp", System.currentTimeMillis());
            notif.put("read", false);
            notif.put("targetUser", FirebaseAuth.getInstance().getCurrentUser().getUid());

            FirebaseFirestore.getInstance().collection("notifications")
                .add(notif);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM", "New token: " + token);
        // Typically save this token to Firestore for targeted messaging
    }
}
