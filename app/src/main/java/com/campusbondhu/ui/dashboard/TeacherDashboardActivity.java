package com.campusbondhu.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.databinding.ActivityTeacherDashboardBinding;
import com.campusbondhu.ui.attendance.StartAttendanceActivity;
import com.campusbondhu.ui.attendance.TeacherAttendanceListActivity;
import com.campusbondhu.ui.auth.LoginActivity;
import com.campusbondhu.ui.notice.CreateNoticeActivity;
import com.campusbondhu.ui.profile.ProfileActivity;
import com.campusbondhu.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class TeacherDashboardActivity extends AppCompatActivity {
    private ActivityTeacherDashboardBinding binding;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityTeacherDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SessionManager session = new SessionManager(this);
        binding.tvGreeting.setText("Welcome, " + (session.getName() != null ? session.getName() : "Teacher"));
        
        binding.btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); session.clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i); finish();
        });
        
        binding.btnStartAttendance.setOnClickListener(v ->
            startActivity(new Intent(this, StartAttendanceActivity.class)));
        binding.btnViewAttendance.setOnClickListener(v ->
            startActivity(new Intent(this, TeacherAttendanceListActivity.class)));
        binding.btnPublishNotice.setOnClickListener(v ->
            startActivity(new Intent(this, CreateNoticeActivity.class)));
            
        // ✅ চ্যাট বাটন যোগ করা হলো (লেআউটে বাটনটা অ্যাড করতে হবে)
        // binding.btnChat.setOnClickListener(v -> startActivity(new Intent(this, ChatListActivity.class)));
    }
}
