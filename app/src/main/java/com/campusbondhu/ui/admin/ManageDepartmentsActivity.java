package com.campusbondhu.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.databinding.ActivityManageDepartmentsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ManageDepartmentsActivity extends AppCompatActivity {
    private ActivityManageDepartmentsBinding binding;
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityManageDepartmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnAdd.setOnClickListener(v -> {
            String name = binding.etDeptName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            binding.progressBar.setVisibility(View.VISIBLE);
            Map<String, Object> dept = new HashMap<>();
            dept.put("name", name);
            
            FirebaseFirestore.getInstance().collection("departments").add(dept)
                .addOnSuccessListener(ref -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Department Added", Toast.LENGTH_SHORT).show();
                    binding.etDeptName.setText("");
                }).addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        });
    }
}
