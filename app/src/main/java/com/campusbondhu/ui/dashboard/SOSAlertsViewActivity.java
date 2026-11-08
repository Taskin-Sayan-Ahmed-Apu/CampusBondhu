package com.campusbondhu.ui.dashboard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.databinding.ActivitySosAlertsViewBinding;

public class SOSAlertsViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        ActivitySosAlertsViewBinding binding = ActivitySosAlertsViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());
        // Reusing a simple layout for SOS alerts list for now
    }
}
