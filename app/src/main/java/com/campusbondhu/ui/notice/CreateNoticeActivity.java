package com.campusbondhu.ui.notice;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.model.Notice;
import com.campusbondhu.data.repository.NoticeRepository;
import com.campusbondhu.databinding.ActivityCreateNoticeBinding;
import com.campusbondhu.utils.SessionManager;

public class CreateNoticeActivity extends AppCompatActivity {
    private ActivityCreateNoticeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNoticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnPost.setOnClickListener(v -> {
            String title = binding.etTitle.getText().toString().trim();
            String body = binding.etBody.getText().toString().trim();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(body)) {
                Toast.makeText(this, "Title and body required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Notice n = new Notice();
            n.setTitle(title);
            n.setBody(body);
            n.setCategory(binding.spinnerCategory.getSelectedItem().toString());
            n.setImportant(binding.switchImportant.isChecked());
            
            SessionManager session = new SessionManager(this);
            n.setAuthorName(session.getName());

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnPost.setEnabled(false);

            new NoticeRepository().createNotice(n).observe(this, ok -> {
                binding.progressBar.setVisibility(View.GONE);
                if (Boolean.TRUE.equals(ok)) {
                    Toast.makeText(this, "Notice posted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    binding.btnPost.setEnabled(true);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
