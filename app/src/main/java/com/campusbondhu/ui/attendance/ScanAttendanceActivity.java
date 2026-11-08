package com.campusbondhu.ui.attendance;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.model.User;
import com.campusbondhu.data.repository.AttendanceRepository;
import com.campusbondhu.data.repository.UserRepository;
import com.campusbondhu.databinding.ActivityScanAttendanceBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ScanAttendanceActivity extends AppCompatActivity {
    private ActivityScanAttendanceBinding binding;
    private final AttendanceRepository attRepo = new AttendanceRepository();
    private final UserRepository userRepo = new UserRepository();
    private User currentUser;

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
        registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) onScanned(result.getContents());
        });

    private final ActivityResultLauncher<Intent> cameraLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null
                    && result.getData().getExtras() != null) {
                Bitmap bmp = (Bitmap) result.getData().getExtras().get("data");
                if (bmp != null) runFaceDetection(bmp);
            } else {
                binding.tvStatus.setText("Face verification cancelled");
                binding.btnScan.setEnabled(true);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanAttendanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnScan.setOnClickListener(v -> launchScanner());
        binding.tvRegisterFace.setOnClickListener(v ->
            startActivity(new Intent(this, FaceRegisterActivity.class)));

        userRepo.getCurrentUser().observe(this, user -> {
            currentUser = user;
            if (user != null && (user.getFaceImageUrl() == null || user.getFaceImageUrl().isEmpty())) {
                binding.tvStatus.setText("⚠ Please register your face first.");
                binding.tvRegisterFace.setVisibility(View.VISIBLE);
                binding.btnScan.setEnabled(false);
            } else {
                binding.tvStatus.setText("Tap Scan to verify attendance");
                binding.tvRegisterFace.setVisibility(View.GONE);
                binding.btnScan.setEnabled(true);
            }
        });
    }

    private void launchScanner() {
        ScanOptions opts = new ScanOptions();
        opts.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        opts.setPrompt("Scan attendance QR from teacher");
        opts.setBeepEnabled(false);
        opts.setOrientationLocked(false);
        scanLauncher.launch(opts);
    }

    private void onScanned(String sessionId) {
        binding.tvStatus.setText("Verifying session...");
        binding.btnScan.setEnabled(false);

        attRepo.getSession(sessionId).observe(this, session -> {
            if (session == null) {
                binding.tvStatus.setText("❌ Invalid session");
                binding.btnScan.setEnabled(true);
                return;
            }
            if (!session.isActive()) {
                binding.tvStatus.setText("❌ Session closed");
                binding.btnScan.setEnabled(true);
                return;
            }
            if (System.currentTimeMillis() > session.getExpiresAt()) {
                binding.tvStatus.setText("❌ Session expired");
                binding.btnScan.setEnabled(true);
                return;
            }
            // Now capture face
            binding.tvStatus.setText("Verify your face");
            Intent take = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(take);
            // store session id for after face detection
            binding.getRoot().setTag(sessionId);
        });
    }

    private void runFaceDetection(Bitmap bmp) {
        binding.tvStatus.setText("Detecting face...");
        InputImage image = InputImage.fromBitmap(bmp, 0);
        FaceDetector detector = FaceDetection.getClient();
        detector.process(image)
            .addOnSuccessListener(faces -> {
                detector.close();
                if (faces.isEmpty()) {
                    binding.tvStatus.setText("❌ No face detected. Try again.");
                    binding.btnScan.setEnabled(true);
                    return;
                }
                String sessionId = (String) binding.getRoot().getTag();
                if (sessionId != null) submitAttendance(sessionId);
            })
            .addOnFailureListener(e -> {
                detector.close();
                binding.tvStatus.setText("❌ Face detection failed: " + e.getMessage());
                binding.btnScan.setEnabled(true);
            });
    }

    private void submitAttendance(String sessionId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String name = currentUser != null ? currentUser.getName() : "";
        attRepo.submitAttendance(sessionId, uid, name).observe(this, status -> {
            binding.btnScan.setEnabled(true);
            switch (status) {
                case "OK": binding.tvStatus.setText("✅ Attendance recorded!"); break;
                case "DUPLICATE": binding.tvStatus.setText("⚠ Already marked for this session"); break;
                case "EXPIRED": binding.tvStatus.setText("❌ Session expired"); break;
                case "CLOSED": binding.tvStatus.setText("❌ Session closed"); break;
                default: binding.tvStatus.setText("❌ Error submitting. Try again.");
            }
            Toast.makeText(this, binding.tvStatus.getText(), Toast.LENGTH_LONG).show();
        });
    }
}
