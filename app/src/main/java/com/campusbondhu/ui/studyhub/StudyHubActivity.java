package com.campusbondhu.ui.studyhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.StudyResource;
import com.campusbondhu.data.repository.StudyHubRepository;
import com.campusbondhu.databinding.ActivityStudyHubBinding;
import com.campusbondhu.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudyHubActivity extends AppCompatActivity {
    private ActivityStudyHubBinding binding;
    private final List<StudyResource> data = new ArrayList<>();
    private ResourceAdapter adapter;
    private StudyHubRepository repo = new StudyHubRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudyHubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        overridePendingTransition(com.campusbondhu.R.anim.slide_in_right, com.campusbondhu.R.anim.fade_out);
        
        binding.btnBack.setOnClickListener(v -> finish());

        SessionManager session = new SessionManager(this);
        if ("admin".equals(session.getRole()) || "teacher".equals(session.getRole())) {
            binding.fabAdd.setVisibility(View.VISIBLE);
            binding.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, UploadResourceActivity.class)));
        }

        binding.rvResources.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResourceAdapter();
        binding.rvResources.setAdapter(adapter);

        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.rvResources.setVisibility(View.GONE);

        repo.getAllResources().observe(this, list -> {
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
            binding.rvResources.setVisibility(View.VISIBLE);

            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();

            if (data.isEmpty()) {
                binding.emptyState.getRoot().setVisibility(View.VISIBLE);
                binding.emptyState.tvEmptyEmoji.setText("📖");
                binding.emptyState.tvEmptyTitle.setText("Study Hub is Empty");
                binding.emptyState.tvEmptySubtitle.setText("No notes or past questions uploaded yet.");
            } else {
                binding.emptyState.getRoot().setVisibility(View.GONE);
            }
        });
    }

    @Override public void finish() { super.finish(); overridePendingTransition(com.campusbondhu.R.anim.fade_in, com.campusbondhu.R.anim.slide_out_left); }
    @Override protected void onDestroy() { super.onDestroy(); repo.removeListeners(); }

    private class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(com.campusbondhu.R.layout.item_resource, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            StudyResource res = data.get(pos);
            h.tvTitle.setText(res.getTitle());
            h.tvCourse.setText(res.getCourseCode() + " • " + res.getType());
            h.tvDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(res.getTimestamp())));
            h.itemView.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(res.getFileUrl()))));
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvCourse, tvDate;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(com.campusbondhu.R.id.tvResTitle);
                tvCourse = v.findViewById(com.campusbondhu.R.id.tvResCourse);
                tvDate = v.findViewById(com.campusbondhu.R.id.tvResDate);
            }
        }
    }
}
