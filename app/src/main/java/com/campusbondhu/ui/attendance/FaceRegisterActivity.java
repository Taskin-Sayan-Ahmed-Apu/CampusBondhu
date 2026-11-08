package com.campusbondhu.ui.attendance;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.databinding.ActivityFaceRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FaceRegisterActivity extends AppCompatActivity {
    private ActivityFaceRegisterBinding binding;
    private Bitmap capturedBitmap;

    // ✅ তোমার Cloudinary তথ্য এখানে বসানো হয়েছে
    private static final String CLOUD_NAME = "xd53hdus";
    private static final String UPLOAD_PRESET = "campusbondhu_faces";

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getExtras() != null) {
                    Bitmap bmp = (Bitmap) result.getData().getExtras().get("data");
                    if (bmp != null) {
                        capturedBitmap = bmp;
                        binding.ivPreview.setImageBitmap(bmp);
                        binding.btnSave.setEnabled(true);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaceRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCapture.setOnClickListener(v -> {
            Intent take = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(take);
        });
        binding.btnSave.setOnClickListener(v -> verifyThenUpload());
    }

    private void verifyThenUpload() {
        if (capturedBitmap == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);
        binding.tvStatus.setText("Detecting face...");

        InputImage image = InputImage.fromBitmap(capturedBitmap, 0);
        FaceDetector detector = FaceDetection.getClient();
        detector.process(image)
                .addOnSuccessListener(faces -> {
                    detector.close();
                    if (faces.isEmpty()) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSave.setEnabled(true);
                        binding.tvStatus.setText("❌ No face found. Retake.");
                        return;
                    }
                    binding.tvStatus.setText("Uploading to Cloudinary...");
                    uploadToCloudinary();
                })
                .addOnFailureListener(e -> {
                    detector.close();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                    binding.tvStatus.setText("❌ Detection failed");
                });
    }

    private void uploadToCloudinary() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        byte[] data = baos.toByteArray();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("file", "face_" + uid + ".jpg",
                        RequestBody.create(MediaType.parse("image/jpeg"), data))
                .build();

        String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> onError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        String secureUrl = obj.getString("secure_url");
                        runOnUiThread(() -> saveUrlToFirestore(secureUrl));
                    } catch (Exception e) {
                        runOnUiThread(() -> onError("Parse error"));
                    }
                } else {
                    runOnUiThread(() -> onError("Upload failed: " + response.code()));
                }
            }
        });
    }

    private void saveUrlToFirestore(String imageUrl) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> upd = new HashMap<>();
        upd.put("faceImageUrl", imageUrl);

        FirebaseFirestore.getInstance().collection("users").document(uid).update(upd)
                .addOnSuccessListener(a -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvStatus.setText("✅ Face registered!");
                    Toast.makeText(this, "Face registered", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> onError(e.getMessage()));
    }

    private void onError(String msg) {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSave.setEnabled(true);
        binding.tvStatus.setText("❌ " + msg);
    }
}