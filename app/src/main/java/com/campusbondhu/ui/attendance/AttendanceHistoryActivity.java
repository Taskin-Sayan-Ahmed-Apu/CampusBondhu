package com.campusbondhu.ui.attendance;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.AttendanceRecord;
import com.campusbondhu.data.repository.AttendanceRepository;
import com.campusbondhu.databinding.ActivityAttendanceHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceHistoryActivity extends AppCompatActivity {
    private ActivityAttendanceHistoryBinding binding;
    private final List<AttendanceRecord> data = new ArrayList<>();
    private RecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAttendanceHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());
        binding.rvRecords.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordAdapter();
        binding.rvRecords.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        new AttendanceRepository().getRecordsForStudent(uid).observe(this, list -> {
            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();
            binding.tvTotal.setText("Total records: " + data.size());
            binding.tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(
                com.campusbondhu.R.layout.item_attendance_record, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            AttendanceRecord r = data.get(pos);
            h.tvCourse.setText(r.getCourseCode() != null ? r.getCourseCode() : "Course");
            h.tvTime.setText(new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                .format(new Date(r.getTimestamp())));
            h.tvVerified.setText(r.isFaceVerified() ? "✅ Face verified" : "Marked");
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvCourse, tvTime, tvVerified;
            VH(View v) {
                super(v);
                tvCourse = v.findViewById(com.campusbondhu.R.id.tvRecCourse);
                tvTime = v.findViewById(com.campusbondhu.R.id.tvRecTime);
                tvVerified = v.findViewById(com.campusbondhu.R.id.tvRecStatus);
            }
        }
    }
}
