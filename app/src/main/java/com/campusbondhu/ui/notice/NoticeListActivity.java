package com.campusbondhu.ui.notice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.Notice;
import com.campusbondhu.data.repository.NoticeRepository;
import com.campusbondhu.databinding.ActivityNoticeListBinding;
import com.campusbondhu.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeListActivity extends AppCompatActivity {
    private ActivityNoticeListBinding binding;
    private final List<Notice> data = new ArrayList<>();
    private NoticeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoticeListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        overridePendingTransition(com.campusbondhu.R.anim.fade_in, com.campusbondhu.R.anim.fade_out);

        binding.btnBack.setOnClickListener(v -> finish());

        SessionManager session = new SessionManager(this);
        if ("teacher".equals(session.getRole()) || "admin".equals(session.getRole())) {
            binding.fabAdd.setVisibility(View.VISIBLE);
            binding.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, CreateNoticeActivity.class)));
        }

        binding.rvNotices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeAdapter();
        binding.rvNotices.setAdapter(adapter);

        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.rvNotices.setVisibility(View.GONE);

        new NoticeRepository().getNotices().observe(this, list -> {
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
            binding.rvNotices.setVisibility(View.VISIBLE);

            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();

            if (data.isEmpty()) {
                binding.emptyState.getRoot().setVisibility(View.VISIBLE);
                binding.emptyState.tvEmptyEmoji.setText("📭");
                binding.emptyState.tvEmptyTitle.setText("No Notices Yet");
                binding.emptyState.tvEmptySubtitle.setText("Check back later for campus updates.");
            } else {
                binding.emptyState.getRoot().setVisibility(View.GONE);
            }
        });
    }

    @Override public void finish() { super.finish(); overridePendingTransition(com.campusbondhu.R.anim.fade_in, com.campusbondhu.R.anim.slide_out_left); }

    private class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(com.campusbondhu.R.layout.item_notice, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Notice n = data.get(pos);
            h.tvTitle.setText(n.getTitle() != null ? n.getTitle() : "No Title");
            h.tvBody.setText(n.getBody() != null ? n.getBody() : "No Details");
            h.tvCategory.setText(n.getCategory() != null ? n.getCategory().toUpperCase() : "GENERAL");
            h.tvAuthor.setText("By " + (n.getAuthorName() != null ? n.getAuthorName() : "Admin"));
            if (n.getTimestamp() > 0) {
                h.tvTime.setText(new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date(n.getTimestamp())));
            }
            h.ivImportant.setVisibility(n.isImportant() ? View.VISIBLE : View.GONE);
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvBody, tvCategory, tvAuthor, tvTime;
            View ivImportant;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(com.campusbondhu.R.id.tvNoticeTitle);
                tvBody = v.findViewById(com.campusbondhu.R.id.tvNoticeBody);
                tvCategory = v.findViewById(com.campusbondhu.R.id.tvNoticeCategory);
                tvAuthor = v.findViewById(com.campusbondhu.R.id.tvNoticeAuthor);
                tvTime = v.findViewById(com.campusbondhu.R.id.tvNoticeTime);
                ivImportant = v.findViewById(com.campusbondhu.R.id.ivImportant);
            }
        }
    }
}
