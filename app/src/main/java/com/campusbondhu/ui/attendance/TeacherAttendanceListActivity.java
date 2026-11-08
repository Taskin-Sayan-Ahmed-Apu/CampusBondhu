package com.campusbondhu.ui.attendance;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.AttendanceSession;
import com.campusbondhu.data.repository.AttendanceRepository;
import com.campusbondhu.databinding.ActivityTeacherAttendanceListBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TeacherAttendanceListActivity extends AppCompatActivity {
    private ActivityTeacherAttendanceListBinding binding;
    private final AttendanceRepository repo = new AttendanceRepository();
    private final List<AttendanceSession> data = new ArrayList<>();
    private SessionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeacherAttendanceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.rvSessions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionAdapter();
        binding.rvSessions.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repo.getSessionsByTeacher(uid).observe(this, list -> {
            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();
            binding.tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = getLayoutInflater().inflate(com.campusbondhu.R.layout.item_session, p, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            AttendanceSession s = data.get(pos);
            h.tvCode.setText(s.getCourseCode() + (s.getCourseName() != null && !s.getCourseName().isEmpty()
                    ? " — " + s.getCourseName() : ""));
            h.tvTime.setText(new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    .format(new Date(s.getCreatedAt())));
            h.tvStatus.setText(s.isActive() ? "🟢 Active" : "🔴 Ended");
            h.tvStatus.setTextColor(s.isActive()
                    ? getColor(com.campusbondhu.R.color.teal_success)
                    : getColor(com.campusbondhu.R.color.grey_500));
            repo.countRecordsForSession(s.getSessionId()).observe(
                    TeacherAttendanceListActivity.this, count ->
                h.tvCount.setText((count != null ? count : 0) + " students"));
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvCode, tvTime, tvStatus, tvCount;
            VH(View v) {
                super(v);
                tvCode = v.findViewById(com.campusbondhu.R.id.tvSessionCourse);
                tvTime = v.findViewById(com.campusbondhu.R.id.tvSessionTime);
                tvStatus = v.findViewById(com.campusbondhu.R.id.tvSessionStatus);
                tvCount = v.findViewById(com.campusbondhu.R.id.tvSessionCount);
            }
        }
    }
}
