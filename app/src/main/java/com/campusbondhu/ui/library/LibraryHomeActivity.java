package com.campusbondhu.ui.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.Book;
import com.campusbondhu.data.repository.LibraryRepository;
import com.campusbondhu.databinding.ActivityLibraryHomeBinding;
import com.campusbondhu.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class LibraryHomeActivity extends AppCompatActivity {
    private ActivityLibraryHomeBinding binding;
    private final List<Book> data = new ArrayList<>();
    private BookAdapter adapter;
    private LibraryRepository repo = new LibraryRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLibraryHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        overridePendingTransition(com.campusbondhu.R.anim.slide_in_right, com.campusbondhu.R.anim.fade_out);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnMyLoans.setOnClickListener(v -> startActivity(new Intent(this, MyLoansActivity.class)));

        SessionManager session = new SessionManager(this);
        if ("admin".equals(session.getRole())) {
            binding.btnAddBook.setVisibility(View.VISIBLE);
            binding.btnAddBook.setOnClickListener(v -> startActivity(new Intent(this, AddBookActivity.class)));
        }

        binding.rvBooks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter();
        binding.rvBooks.setAdapter(adapter);

        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.rvBooks.setVisibility(View.GONE);

        repo.getBooks().observe(this, list -> {
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
            binding.rvBooks.setVisibility(View.VISIBLE);

            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();

            if (data.isEmpty()) {
                binding.emptyState.getRoot().setVisibility(View.VISIBLE);
                binding.emptyState.tvEmptyEmoji.setText("📚");
                binding.emptyState.tvEmptyTitle.setText("Library is Empty");
                binding.emptyState.tvEmptySubtitle.setText("No books available right now.");
            } else {
                binding.emptyState.getRoot().setVisibility(View.GONE);
            }
        });
    }

    @Override public void finish() { super.finish(); overridePendingTransition(com.campusbondhu.R.anim.fade_in, com.campusbondhu.R.anim.slide_out_left); }
    @Override protected void onDestroy() { super.onDestroy(); repo.removeListeners(); }

    private class BookAdapter extends RecyclerView.Adapter<BookAdapter.VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(com.campusbondhu.R.layout.item_book, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Book b = data.get(pos);
            h.tvTitle.setText(b.getTitle());
            h.tvAuthor.setText(b.getAuthor() != null ? b.getAuthor() : "Unknown Author");
            h.tvStatus.setText(b.isAvailable() ? "Available" : "Issued");
            h.tvStatus.setTextColor(b.isAvailable() ? getColor(com.campusbondhu.R.color.teal_success) : getColor(com.campusbondhu.R.color.red_sos));
            h.itemView.setOnClickListener(v -> {
                if (b.isAvailable()) {
                    Intent intent = new Intent(LibraryHomeActivity.this, IssueBookActivity.class);
                    intent.putExtra("bookId", b.getBookId());
                    intent.putExtra("bookTitle", b.getTitle());
                    startActivity(intent);
                } else {
                    android.widget.Toast.makeText(LibraryHomeActivity.this, "Book is currently issued", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAuthor, tvStatus;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(com.campusbondhu.R.id.tvBookTitle);
                tvAuthor = v.findViewById(com.campusbondhu.R.id.tvBookAuthor);
                tvStatus = v.findViewById(com.campusbondhu.R.id.tvBookStatus);
            }
        }
    }
}
