package com.campusbondhu.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.databinding.ActivityLoginBinding;
import com.campusbondhu.ui.dashboard.*;
import com.campusbondhu.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private String selectedRole = "student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        // ChipGroup লিসেনার যা নিখুঁতভাবে সিলেক্টেড রোল ধরবে
        binding.chipGroupRoles.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.chipStudent.getId()) selectedRole = "student";
            else if (checkedId == binding.chipTeacher.getId()) selectedRole = "teacher";
            else if (checkedId == binding.chipGuardian.getId()) selectedRole = "guardian";
            else if (checkedId == binding.chipAdmin.getId()) selectedRole = "admin";
        });

        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        binding.tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) { binding.etEmail.setError("Email required"); return; }
        if (TextUtils.isEmpty(password)) { binding.etPassword.setError("Password required"); return; }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                verifyRoleAndRoute(auth.getCurrentUser().getUid());
            } else {
                Toast.makeText(this, "Login failed: " +
                                (task.getException() != null ? task.getException().getMessage() : "Error"),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verifyRoleAndRoute(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_LONG).show();
                        auth.signOut(); return;
                    }
                    String actualRole = doc.getString("role");
                    String name = doc.getString("name");

                    // চেক করা হচ্ছে অ্যামেইল যে রোলের, লগইনে ইউজার সেই রোলই সিলেক্ট করেছে কি না
                    if (actualRole == null || !actualRole.equals(selectedRole)) {
                        Toast.makeText(this, "Role mismatch! You registered as a " + actualRole.toUpperCase() +
                                ". Please select the correct role.", Toast.LENGTH_LONG).show();
                        auth.signOut(); return;
                    }
                    new SessionManager(this).saveSession(uid, actualRole, name);
                    routeToDashboard(actualRole);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Verification failed", Toast.LENGTH_LONG).show();
                    auth.signOut();
                });
    }

    private void routeToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "teacher": intent = new Intent(this, TeacherDashboardActivity.class); break;
            case "guardian": intent = new Intent(this, GuardianDashboardActivity.class); break;
            case "admin": intent = new Intent(this, AdminDashboardActivity.class); break;
            default: intent = new Intent(this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
    }
}