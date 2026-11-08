package com.campusbondhu.ui.library;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.repository.LibraryRepository;
import com.campusbondhu.databinding.ActivityIssueBookBinding;
import com.campusbondhu.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class IssueBookActivity extends AppCompatActivity {
    private ActivityIssueBookBinding binding;
    private String bookId, bookTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIssueBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");
        bookTitle = getIntent().getStringExtra("bookTitle");

        binding.tvBookTitle.setText(bookTitle);
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnConfirm.setOnClickListener(v -> issueBook());
    }

    private void issueBook() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SessionManager session = new SessionManager(this);
        String name = session.getName();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnConfirm.setEnabled(false);

        new LibraryRepository().issueBook(bookId, uid, name).observe(this, status -> {
            binding.progressBar.setVisibility(View.GONE);
            switch (status) {
                case "OK":
                    Toast.makeText(this, "Book Issued Successfully! Return within 14 days.", Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case "DUPLICATE":
                    binding.btnConfirm.setEnabled(true);
                    Toast.makeText(this, "You already have this book issued.", Toast.LENGTH_LONG).show();
                    break;
                case "UNAVAILABLE":
                    binding.btnConfirm.setEnabled(true);
                    Toast.makeText(this, "Book is not available.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    binding.btnConfirm.setEnabled(true);
                    Toast.makeText(this, "Error issuing book", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
