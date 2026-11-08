package com.campusbondhu.ui.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.model.AttendanceRecord;
import com.campusbondhu.data.model.EmergencyAlert;
import com.campusbondhu.data.model.User;
import com.campusbondhu.data.repository.GuardianRepository;
import com.campusbondhu.databinding.ActivityGuardianViewStudentBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GuardianViewStudentActivity extends AppCompatActivity {
    private ActivityGuardianViewStudentBinding binding;
    private GuardianRepository repo = new GuardianRepository();
    private User linkedStudent;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityGuardianViewStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        String guardianId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        repo.getLinkedStudent(guardianId).observe(this, student -> {
            if (student == null) {
                Toast.makeText(this, "No linked student found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            linkedStudent = student;
            binding.tvStudentName.setText(student.getName() + " (" + student.getStudentId() + ")");
            loadAttendance(student.getUid());
            loadSOSAlerts(student.getUid());
        });
    }

    private void loadAttendance(String studentId) {
        repo.getStudentAttendance(studentId).observe(this, records -> {
            if (records == null || records.isEmpty()) {
                binding.tvAttendanceCount.setText("No attendance records found");
                return;
            }
            binding.tvAttendanceCount.setText("Recent Attendance: " + records.size() + " records");
            
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            for (AttendanceRecord rec : records) {
                sb.append("• ").append(rec.getCourseCode())
                  .append(" on ").append(sdf.format(new Date(rec.getTimestamp())))
                  .append("\n");
            }
            binding.tvAttendanceList.setText(sb.toString());
        });
    }

    private void loadSOSAlerts(String studentId) {
        repo.getStudentAlerts(studentId).observe(this, alerts -> {
            if (alerts == null || alerts.isEmpty()) {
                binding.tvSosStatus.setText("✅ No active SOS alerts");
                binding.tvSosStatus.setTextColor(getColor(com.campusbondhu.R.color.teal_success));
            } else {
                binding.tvSosStatus.setText("🚨 EMERGENCY! " + alerts.size() + " active alert(s)");
                binding.tvSosStatus.setTextColor(getColor(com.campusbondhu.R.color.red_sos));
            }
        });
    }
}
