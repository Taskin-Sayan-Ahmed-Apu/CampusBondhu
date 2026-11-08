package com.campusbondhu.ui.attendance;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.repository.AttendanceRepository;
import com.campusbondhu.databinding.ActivityStartAttendanceBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class StartAttendanceActivity extends AppCompatActivity {
    private ActivityStartAttendanceBinding binding;
    private final AttendanceRepository repo = new AttendanceRepository();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timer;
    private String activeSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartAttendanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnStart.setOnClickListener(v -> startSession());
        binding.btnStop.setOnClickListener(v -> stopSession());
    }

    private void startSession() {
        String code = binding.etCourseCode.getText().toString().trim();
        String name = binding.etCourseName.getText().toString().trim();
        String durStr = binding.etDuration.getText().toString().trim();

        if (TextUtils.isEmpty(code)) { binding.etCourseCode.setError("Required"); return; }

        int duration;
        try { duration = Integer.parseInt(durStr); } catch (Exception e) { duration = 15; }
        if (duration < 1 || duration > 180) duration = 15;

        // ✅ এখানে duration-কে final করা হলো, যাতে ল্যাম্বডার ভেতরে কাজ করে
        final int finalDuration = duration;

        binding.btnStart.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        repo.createSession(code, name, finalDuration).observe(this, sessionId -> {
            binding.progressBar.setVisibility(View.GONE);
            if (sessionId == null) {
                binding.btnStart.setEnabled(true);
                Toast.makeText(this, "Failed to start session", Toast.LENGTH_LONG).show();
                return;
            }
            activeSessionId = sessionId;
            binding.layoutPreStart.setVisibility(View.GONE);
            binding.layoutActive.setVisibility(View.VISIBLE);
            binding.tvCourseInfo.setText(code + (name.isEmpty() ? "" : " — " + name));
            try {
                binding.ivQr.setImageBitmap(generateQr(sessionId, 700));
            } catch (Exception e) {
                Toast.makeText(this, "QR gen error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            long expiresAt = System.currentTimeMillis() + finalDuration * 60_000L;
            startTimer(expiresAt);
        });
    }

    private void stopSession() {
        if (activeSessionId == null) { finish(); return; }
        if (timer != null) handler.removeCallbacks(timer);
        repo.stopSession(activeSessionId).observe(this, ok -> {
            Toast.makeText(this, ok != null && ok ? "Session ended" : "Failed", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void startTimer(long expiresAt) {
        timer = new Runnable() {
            @Override
            public void run() {
                long remaining = expiresAt - System.currentTimeMillis();
                if (remaining <= 0) {
                    binding.tvTimer.setText("Session expired");
                    return;
                }
                long s = remaining / 1000;
                binding.tvTimer.setText(String.format("Expires in %02d:%02d", s / 60, s % 60));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timer);
    }

    private Bitmap generateQr(String content, int size) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix m = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                bmp.setPixel(x, y, m.get(x, y) ? Color.BLACK : Color.WHITE);
        return bmp;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) handler.removeCallbacks(timer);
    }
}