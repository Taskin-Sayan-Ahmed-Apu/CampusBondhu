package com.campusbondhu.ui.library;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.LibraryLoan;
import com.campusbondhu.data.repository.LibraryRepository;
import com.campusbondhu.databinding.ActivityMyLoansBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyLoansActivity extends AppCompatActivity {
    private ActivityMyLoansBinding binding;
    private final List<LibraryLoan> data = new ArrayList<>();
    private LoanAdapter adapter;
    private LibraryRepository repo = new LibraryRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyLoansBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        overridePendingTransition(com.campusbondhu.R.anim.slide_in_right, com.campusbondhu.R.anim.fade_out);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.rvLoans.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoanAdapter();
        binding.rvLoans.setAdapter(adapter);

        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.rvLoans.setVisibility(View.GONE);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repo.getMyLoans(uid).observe(this, list -> {
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
            binding.rvLoans.setVisibility(View.VISIBLE);

            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();

            if (data.isEmpty()) {
                binding.emptyState.getRoot().setVisibility(View.VISIBLE);
                binding.emptyState.tvEmptyEmoji.setText("📄");
                binding.emptyState.tvEmptyTitle.setText("No Active Loans");
                binding.emptyState.tvEmptySubtitle.setText("You haven't issued any books yet.");
            } else {
                binding.emptyState.getRoot().setVisibility(View.GONE);
            }
        });
    }

    @Override public void finish() { super.finish(); overridePendingTransition(com.campusbondhu.R.anim.fade_in, com.campusbondhu.R.anim.slide_out_left); }
    @Override protected void onDestroy() { super.onDestroy(); repo.removeListeners(); }

    private class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(com.campusbondhu.R.layout.item_loan, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            LibraryLoan l = data.get(pos);
            h.tvTitle.setText(l.getBookTitle());
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            h.tvIssueDate.setText("Issued: " + sdf.format(new Date(l.getIssueDate())));
            h.tvDueDate.setText("Due: " + sdf.format(new Date(l.getDueDate())));
            if ("ISSUED".equals(l.getStatus())) {
                h.tvStatus.setText("ISSUED"); h.tvStatus.setTextColor(getColor(com.campusbondhu.R.color.orange_warning));
                h.btnReturn.setVisibility(View.VISIBLE);
                h.btnReturn.setOnClickListener(v -> {
                    h.btnReturn.setEnabled(false);
                    repo.returnBook(l.getLoanId(), l.getBookId()).observe(MyLoansActivity.this, ok -> {
                        if (Boolean.TRUE.equals(ok)) android.widget.Toast.makeText(MyLoansActivity.this, "Book Returned", android.widget.Toast.LENGTH_SHORT).show();
                        else h.btnReturn.setEnabled(true);
                    });
                });
            } else {
                h.tvStatus.setText("RETURNED"); h.tvStatus.setTextColor(getColor(com.campusbondhu.R.color.teal_success));
                h.btnReturn.setVisibility(View.GONE);
            }
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvIssueDate, tvDueDate, tvStatus;
            com.google.android.material.button.MaterialButton btnReturn;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(com.campusbondhu.R.id.tvLoanTitle);
                tvIssueDate = v.findViewById(com.campusbondhu.R.id.tvLoanIssueDate);
                tvDueDate = v.findViewById(com.campusbondhu.R.id.tvLoanDueDate);
                tvStatus = v.findViewById(com.campusbondhu.R.id.tvLoanStatus);
                btnReturn = v.findViewById(com.campusbondhu.R.id.btnReturnBook);
            }
        }
    }
}
