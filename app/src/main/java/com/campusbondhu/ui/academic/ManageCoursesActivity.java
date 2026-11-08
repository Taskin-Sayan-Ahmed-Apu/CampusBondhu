package com.campusbondhu.ui.academic;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.model.Course;
import com.campusbondhu.data.repository.AcademicRepository;
import com.campusbondhu.databinding.ActivityManageCoursesBinding;

public class ManageCoursesActivity extends AppCompatActivity {
    private ActivityManageCoursesBinding binding;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityManageCoursesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnAdd.setOnClickListener(v -> {
            String code = binding.etCode.getText().toString().trim();
            String name = binding.etName.getText().toString().trim();
            // ✅ Spinner থেকে ক্রেডিট নেওয়া হচ্ছে
            String creditStr = binding.spinnerCredit.getSelectedItem().toString();

            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(creditStr)) {
                Toast.makeText(this, "Code and Credit required", Toast.LENGTH_SHORT).show();
                return;
            }

            Course c = new Course();
            c.setCode(code);
            c.setName(name);
            c.setCredit(Double.parseDouble(creditStr));

            binding.progressBar.setVisibility(View.VISIBLE);
            new AcademicRepository().addCourse(c).observe(this, ok -> {
                binding.progressBar.setVisibility(View.GONE);
                if (Boolean.TRUE.equals(ok)) {
                    Toast.makeText(this, "Course Added", Toast.LENGTH_SHORT).show();
                    binding.etCode.setText("");
                    binding.etName.setText("");
                } else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
