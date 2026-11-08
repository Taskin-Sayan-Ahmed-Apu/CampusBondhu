package com.campusbondhu.ui.studyhub;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.model.StudyResource;
import com.campusbondhu.data.repository.StudyHubRepository;
import com.campusbondhu.databinding.ActivityUploadResourceBinding;
import com.campusbondhu.utils.SessionManager;

public class UploadResourceActivity extends AppCompatActivity {
    private ActivityUploadResourceBinding binding;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityUploadResourceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v -> {
            String title = binding.etTitle.getText().toString().trim();
            String course = binding.etCourse.getText().toString().trim();
            String url = binding.etUrl.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(course) || TextUtils.isEmpty(url)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!url.startsWith("http")) {
                Toast.makeText(this, "Please provide a valid URL (http...)", Toast.LENGTH_SHORT).show();
                return;
            }

            SessionManager session = new SessionManager(this);
            StudyResource res = new StudyResource();
            res.setTitle(title);
            res.setCourseCode(course);
            res.setType(binding.spinnerType.getSelectedItem().toString());
            res.setFileUrl(url);
            res.setUploadedBy(session.getName());
            res.setTimestamp(System.currentTimeMillis());

            binding.progressBar.setVisibility(View.VISIBLE);
            new StudyHubRepository().addResource(res).observe(this, ok -> {
                binding.progressBar.setVisibility(View.GONE);
                if (Boolean.TRUE.equals(ok)) {
                    Toast.makeText(this, "Resource Added", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
