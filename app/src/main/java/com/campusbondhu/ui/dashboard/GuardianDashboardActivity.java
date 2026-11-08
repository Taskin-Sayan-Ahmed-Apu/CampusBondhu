package com.campusbondhu.ui.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.repository.GuardianRepository;
import com.campusbondhu.databinding.ActivityGuardianDashboardBinding;
import com.campusbondhu.ui.auth.LoginActivity;
import com.campusbondhu.ui.profile.ProfileActivity;
import com.campusbondhu.ui.sos.SOSAlertsListActivity;
import com.campusbondhu.utils.AlarmHelper;
import com.campusbondhu.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class GuardianDashboardActivity extends AppCompatActivity {
    private ActivityGuardianDashboardBinding binding;
    private GuardianRepository repo = new GuardianRepository();
    private String guardianId;
    private AlarmHelper alarmHelper = new AlarmHelper();
    private ListenerRegistration sosListener;
    private AlertDialog sosDialog;
    private String linkedStudentId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuardianDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        SessionManager session = new SessionManager(this);
        guardianId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        binding.tvGreeting.setText("Welcome, " + (session.getName() != null ? session.getName() : "Guardian"));
        binding.btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); session.clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i); finish();
        });

        binding.btnLinkStudent.setOnClickListener(v ->
            startActivity(new Intent(this, LinkStudentActivity.class)));

        binding.btnViewStudent.setOnClickListener(v ->
            startActivity(new Intent(this, GuardianViewStudentActivity.class)));

        binding.btnViewNotices.setOnClickListener(v ->
            startActivity(new Intent(this, com.campusbondhu.ui.notice.NoticeListActivity.class)));

        binding.btnSosAlerts.setOnClickListener(v ->
            startActivity(new Intent(this, SOSAlertsListActivity.class)));

        loadLinkedStudent();
    }

    private void loadLinkedStudent() {
        repo.getLinkedStudent(guardianId).observe(this, student -> {
            if (student != null) {
                binding.tvLinkedStatus.setText("Linked to: " + student.getName() + " (" + student.getStudentId() + ")");
                binding.tvLinkedStatus.setTextColor(getColor(com.campusbondhu.R.color.teal_success));
                linkedStudentId = student.getUid();
                listenForSOS();
            } else {
                binding.tvLinkedStatus.setText("No student linked yet. Click 'Link a Student'.");
                binding.tvLinkedStatus.setTextColor(getColor(com.campusbondhu.R.color.orange_warning));
            }
        });
    }

    private void listenForSOS() {
        if (linkedStudentId == null) return;
        if (sosListener != null) sosListener.remove();

        sosListener = FirebaseFirestore.getInstance().collection("emergencyAlerts")
            .whereEqualTo("studentId", linkedStudentId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                
                if (!snap.isEmpty()) {
                    if (sosDialog == null || !sosDialog.isShowing()) {
                        triggerSOSAlarm();
                    }
                } else {
                    stopSOSAlarm();
                }
            });
    }

    private void triggerSOSAlarm() {
        alarmHelper.playAlarm(this);

        sosDialog = new AlertDialog.Builder(this)
            .setTitle("🚨 EMERGENCY SOS ALERT 🚨")
            .setMessage("Your child is in danger! An alarm is ringing until resolved.")
            .setCancelable(false)
            .setPositiveButton("STOP ALARM", (dialog, which) -> {
                stopSOSAlarm();
            })
            .setNegativeButton("View Details", (dialog, which) -> {
                startActivity(new Intent(GuardianDashboardActivity.this, SOSAlertsListActivity.class));
                dialog.dismiss();
            })
            .show();
    }

    private void stopSOSAlarm() {
        alarmHelper.stopAlarm();
        if (sosDialog != null && sosDialog.isShowing()) {
            sosDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sosListener != null) sosListener.remove();
        stopSOSAlarm();
    }
}
