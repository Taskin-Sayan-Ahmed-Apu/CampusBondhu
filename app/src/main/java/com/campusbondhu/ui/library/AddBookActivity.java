package com.campusbondhu.ui.library;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.model.Book;
import com.campusbondhu.data.repository.LibraryRepository;
import com.campusbondhu.databinding.ActivityAddBookBinding;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;

public class AddBookActivity extends AppCompatActivity {
    private ActivityAddBookBinding binding;

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
        registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                binding.etBarcode.setText(result.getContents());
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnScan.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
            options.setPrompt("Scan Book Barcode");
            options.setBeepEnabled(true);
            scanLauncher.launch(options);
        });

        binding.btnSave.setOnClickListener(v -> {
            String title = binding.etTitle.getText().toString().trim();
            String author = binding.etAuthor.getText().toString().trim();
            String barcode = binding.etBarcode.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(barcode)) {
                Toast.makeText(this, "Title and Barcode are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setBarcode(barcode);
            book.setAvailable(true);
            book.setCurrentHolderId("");

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSave.setEnabled(false);

            new LibraryRepository().addBook(book).observe(this, ok -> {
                binding.progressBar.setVisibility(View.GONE);
                if (Boolean.TRUE.equals(ok)) {
                    Toast.makeText(this, "Book Added", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
