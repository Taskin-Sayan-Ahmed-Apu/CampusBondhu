package com.campusbondhu.data.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.campusbondhu.utils.NotificationHelper;

public class SystemStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
                if (isAirplaneModeOn) {
                    NotificationHelper.showNotification(context, "✈️ Airplane Mode On", "Your campus alerts will not work.", 999);
                } else {
                    NotificationHelper.showNotification(context, "📶 Network Restored", "Airplane mode is off.", 998);
                }
            } else if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
                NotificationHelper.showNotification(context, "🪫 Battery Low", "Please charge your phone.", 997);
            }
        }
    }
}
