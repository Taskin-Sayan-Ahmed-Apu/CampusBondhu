package com.campusbondhu.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.campusbondhu.data.repository.UserRepository;
import com.campusbondhu.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
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

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private final UserRepository repo = new UserRepository();
    private Bitmap selectedImageBitmap = null;

    private static final String CLOUD_NAME = "xd53hdus";
    private static final String UPLOAD_PRESET = "campusbondhu_faces";

    private final ActivityResultLauncher<Intent> galleryLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                android.net.Uri imageUri = result.getData().getData();
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    binding.ivProfilePic.setImageBitmap(selectedImageBitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        });

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnAddPic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        repo.getCurrentUser().observe(this, u -> {
            if (u == null) return;
            binding.etName.setText(u.getName());
            binding.etStudentId.setText(u.getStudentId());
            binding.etDepartment.setText(u.getDepartment());
            binding.etPhone.setText(u.getPhone());
            
            if (u.getProfileImageUrl() != null && !u.getProfileImageUrl().isEmpty()) {
                Glide.with(this).load(u.getProfileImageUrl()).circleCrop().into(binding.ivProfilePic);
            }
        });
        
        binding.btnSave.setOnClickListener(v -> {
            if (selectedImageBitmap != null) uploadImageToCloudinary();
            else saveData(null);
        });
    }

    private void uploadImageToCloudinary() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("file", "profile_" + uid + ".jpg", RequestBody.create(MediaType.parse("image/jpeg"), data))
                .build();

        String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
        Request request = new Request.Builder().url(url).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE); binding.btnSave.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String secureUrl = new JSONObject(response.body().string()).getString("secure_url");
                        runOnUiThread(() -> saveData(secureUrl));
                    } catch (Exception e) {
                        runOnUiThread(() -> { binding.progressBar.setVisibility(View.GONE); binding.btnSave.setEnabled(true); });
                    }
                } else {
                    runOnUiThread(() -> { binding.progressBar.setVisibility(View.GONE); binding.btnSave.setEnabled(true); });
                }
            }
        });
    }

    private void saveData(String imageUrl) {
        String name = binding.etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) { binding.etName.setError("Required"); return; }
        
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> u = new HashMap<>();
        u.put("name", name);
        u.put("studentId", binding.etStudentId.getText().toString().trim());
        u.put("department", binding.etDepartment.getText().toString().trim()); // ✅ ডিপার্টমেন্ট এখানে সেভ হচ্ছে
        u.put("phone", binding.etPhone.getText().toString().trim());
        if (imageUrl != null) u.put("profileImageUrl", imageUrl);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);
        
        repo.updateProfile(uid, u).observe(this, ok -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSave.setEnabled(true);
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
