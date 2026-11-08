package com.campusbondhu.ui.sos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.campusbondhu.data.model.EmergencyAlert;
import com.campusbondhu.data.remote.SOSForegroundService;
import com.campusbondhu.data.repository.SOSRepository;
import com.campusbondhu.databinding.ActivitySosBinding;
import com.campusbondhu.utils.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

public class SOSActivity extends AppCompatActivity {
    private ActivitySosBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private SOSRepository repo = new SOSRepository();
    private double lat = 0.0, lng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSOS.setOnClickListener(v -> {
            binding.btnSOS.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
            
            // ✅ ফোরগ্রাউন্ড সার্ভিস চালু করা হলো (নোটিফিকেশন বারে থাকবে)
            Intent serviceIntent = new Intent(this, SOSForegroundService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
            
            getLocationAndAlert();
        });
    }

    private void getLocationAndAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
            }
            sendAlert();
        }).addOnFailureListener(e -> sendAlert());
    }

    private void sendAlert() {
        SessionManager session = new SessionManager(this);
        EmergencyAlert alert = new EmergencyAlert();
        alert.setStudentId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        alert.setStudentName(session.getName());
        alert.setLatitude(lat);
        alert.setLongitude(lng);

        repo.triggerSOS(alert).observe(this, ok -> {
            binding.progressBar.setVisibility(View.GONE);
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(this, "🚨 SOS Alert Sent! Service is running.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                binding.btnSOS.setEnabled(true);
                Toast.makeText(this, "Failed to send alert", Toast.LENGTH_SHORT).show();
                // ফেইল করলে সার্ভিস বন্ধ করে দাও
                stopService(new Intent(this, SOSForegroundService.class));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            getLocationAndAlert();
        }
    }
}
