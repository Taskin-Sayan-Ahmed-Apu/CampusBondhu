package com.campusbondhu.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.databinding.ActivitySplashBinding;
import com.campusbondhu.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ পালস অ্যানিমেশন চালু করা হলো
        binding.splashLogo.startAnimation(
            AnimationUtils.loadAnimation(this, com.campusbondhu.R.anim.pulse)
        );

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(com.campusbondhu.R.anim.zoom_in, com.campusbondhu.R.anim.zoom_out);
            finish();
        }, 2000);
    }
}
