package com.campusbondhu.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String selectedRole = "student"; // ডিফল্ট

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // নিখুঁত রোল সিলেকশন লজিক
        binding.chipGroupRoles.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.chipStudent.getId()) selectedRole = "student";
            else if (checkedId == binding.chipTeacher.getId()) selectedRole = "teacher";
            else if (checkedId == binding.chipGuardian.getId()) selectedRole = "guardian";
            else if (checkedId == binding.chipAdmin.getId()) selectedRole = "admin";
        });

        binding.btnRegister.setOnClickListener(v -> register());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String studentId = binding.etStudentId.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { binding.etName.setError("Required"); return; }
        if (TextUtils.isEmpty(email)) { binding.etEmail.setError("Required"); return; }
        if (password.length() < 6) { binding.etPassword.setError("Min 6 chars"); return; }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
                Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            String uid = auth.getCurrentUser().getUid();
            Map<String, Object> profile = new HashMap<>();
            profile.put("uid", uid);
            profile.put("name", name);
            profile.put("email", email);
            profile.put("role", selectedRole);
            profile.put("studentId", studentId);
            profile.put("createdAt", System.currentTimeMillis());
            profile.put("active", true);

            db.collection("users").document(uid).set(profile)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnRegister.setEnabled(true);
                        Toast.makeText(this, "Save failed", Toast.LENGTH_LONG).show();
                    });
        });
    }
}