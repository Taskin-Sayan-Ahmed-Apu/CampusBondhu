package com.campusbondhu.ui.academic;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.model.RoutineItem;
import com.campusbondhu.data.repository.AcademicRepository;
import com.campusbondhu.databinding.ActivityManageRoutineBinding;

public class ManageRoutineActivity extends AppCompatActivity {
    private ActivityManageRoutineBinding binding;
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityManageRoutineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnAdd.setOnClickListener(v -> {
            String code = binding.etCode.getText().toString().trim();
            String room = binding.etRoom.getText().toString().trim();
            String time = binding.etTime.getText().toString().trim();

            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(time)) {
                Toast.makeText(this, "Course Code and Time required", Toast.LENGTH_SHORT).show();
                return;
            }

            RoutineItem item = new RoutineItem();
            item.setCourseCode(code);
            item.setRoom(room);
            item.setDay(binding.spinnerDay.getSelectedItem().toString());
            item.setTime(time);

            binding.progressBar.setVisibility(View.VISIBLE);
            new AcademicRepository().addRoutine(item).observe(this, ok -> {
                binding.progressBar.setVisibility(View.GONE);
                if (Boolean.TRUE.equals(ok)) {
                    Toast.makeText(this, "Routine Added", Toast.LENGTH_SHORT).show();
                    binding.etCode.setText("");
                    binding.etRoom.setText("");
                    binding.etTime.setText("");
                } else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
