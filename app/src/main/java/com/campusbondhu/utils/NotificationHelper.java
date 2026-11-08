package com.campusbondhu.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static void showNotification(Context context, String title, String message, int notifId) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "campus_alerts_high";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Campus High Priority Alerts", NotificationManager.IMPORTANCE_HIGH);
            // মাল্টিপল ভাইব্রেশন প্যাটার্ন (১ সেকেন্ড ভাইব, ০.৫ সেকেন্ড বিশ্রাম)
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000, 500, 1000});
            nm.createNotificationChannel(channel);
        }

        // অ্যালার্ম সাউন্ড
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (soundUri == null) {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 1000, 500, 1000, 500, 1000})
                .setAutoCancel(true);

        nm.notify(notifId, builder.build());

        // ম্যানুয়াল ভাইব্রেশন (যদি ডিভাইস সাইলেন্টে থাকে)
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(new long[]{0, 1000, 500, 1000, 500, 1000}, -1));
            } else {
                v.vibrate(new long[]{0, 1000, 500, 1000, 500, 1000}, -1);
            }
        }
    }
}
