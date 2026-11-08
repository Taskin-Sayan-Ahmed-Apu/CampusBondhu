package com.campusbondhu.ui.dashboard;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.repository.GuardianRepository;
import com.campusbondhu.databinding.ActivityLinkStudentBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LinkStudentActivity extends AppCompatActivity {
    private ActivityLinkStudentBinding binding;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityLinkStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnLink.setOnClickListener(v -> {
            String roll = binding.etRoll.getText().toString().trim();
            if (TextUtils.isEmpty(roll)) {
                Toast.makeText(this, "Enter Student ID", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            String guardianId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            new GuardianRepository().linkStudent(guardianId, roll).observe(this, result -> {
                binding.progressBar.setVisibility(View.GONE);
                if ("OK".equals(result)) {
                    Toast.makeText(this, "Linked Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if ("NOT_FOUND".equals(result)) {
                    Toast.makeText(this, "No student found with this ID", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Error linking student", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
