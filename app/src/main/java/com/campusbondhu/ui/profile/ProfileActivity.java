package com.campusbondhu.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.campusbondhu.data.model.User;
import com.campusbondhu.data.repository.UserRepository;
import com.campusbondhu.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private final UserRepository repo = new UserRepository();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnEdit.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        
        repo.getCurrentUser().observe(this, this::bind);
    }

    private void bind(User u) {
        if (u == null) return;
        binding.tvName.setText(nz(u.getName()));
        binding.tvEmail.setText(nz(u.getEmail()));
        binding.tvRole.setText("Role: " + (u.getRole() != null ? u.getRole().toUpperCase() : "—"));
        binding.tvStudentId.setText("ID: " + nz(u.getStudentId()));
        binding.tvDepartment.setText("Dept: " + nz(u.getDepartment()));
        binding.tvPhone.setText("Phone: " + nz(u.getPhone()));
        
        // ✅ Glide এ circleCrop এবং centerCrop ব্যবহার করা হলো
        if (u.getProfileImageUrl() != null && !u.getProfileImageUrl().isEmpty()) {
            Glide.with(this).load(u.getProfileImageUrl())
                .circleCrop()
                .into(binding.ivAvatar);
        }
    }

    private String nz(String s) { return s != null && !s.isEmpty() ? s : "—"; }
}
