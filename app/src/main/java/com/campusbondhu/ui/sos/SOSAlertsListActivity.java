package com.campusbondhu.ui.sos;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.EmergencyAlert;
import com.campusbondhu.data.repository.SOSRepository;
import com.campusbondhu.databinding.ActivitySosAlertsListBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SOSAlertsListActivity extends AppCompatActivity {
    private ActivitySosAlertsListBinding binding;
    private final List<EmergencyAlert> data = new ArrayList<>();
    private AlertAdapter adapter;
    private SOSRepository repo = new SOSRepository();
    private int previousCount = 0;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivitySosAlertsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlertAdapter();
        binding.rvAlerts.setAdapter(adapter);

        repo.getAllActiveAlerts().observe(this, list -> {
            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();
            binding.tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);

            // যদি নতুন SOS আসে, তবে নোটিফিকেশন + ভাইব্রেশন
            if (data.size() > previousCount) {
                showLocalNotification(data.get(0));
            }
            previousCount = data.size();
        });
    }

    private void showLocalNotification(EmergencyAlert alert) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "sos_channel";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "SOS Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 SOS ALERT!")
            .setContentText(alert.getStudentName() + " is in danger!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setAutoCancel(true);
            
        nm.notify(1, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repo.removeListeners();
    }

    private class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(
                com.campusbondhu.R.layout.item_sos_alert, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            EmergencyAlert alert = data.get(pos);
            h.tvName.setText(alert.getStudentName() + " is in danger!");
            h.tvTime.setText(new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                .format(new Date(alert.getTimestamp())));
            h.tvLoc.setText("Lat: " + alert.getLatitude() + ", Lng: " + alert.getLongitude());

            h.btnResolve.setOnClickListener(v -> {
                repo.resolveAlert(alert.getAlertId()).observe(SOSAlertsListActivity.this, ok -> {
                    if (Boolean.TRUE.equals(ok)) {
                        android.widget.Toast.makeText(SOSAlertsListActivity.this, "Alert Resolved", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvTime, tvLoc;
            com.google.android.material.button.MaterialButton btnResolve;
            VH(View v) {
                super(v);
                tvName = v.findViewById(com.campusbondhu.R.id.tvSosName);
                tvTime = v.findViewById(com.campusbondhu.R.id.tvSosTime);
                tvLoc = v.findViewById(com.campusbondhu.R.id.tvSosLocation);
                btnResolve = v.findViewById(com.campusbondhu.R.id.btnResolve);
            }
        }
    }
}
