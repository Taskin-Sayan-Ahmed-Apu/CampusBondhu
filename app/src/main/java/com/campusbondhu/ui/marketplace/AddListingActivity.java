package com.campusbondhu.ui.marketplace;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.model.MarketplaceListing;
import com.campusbondhu.data.repository.MarketplaceRepository;
import com.campusbondhu.databinding.ActivityAddListingBinding;
import com.campusbondhu.utils.SessionManager;

public class AddListingActivity extends AppCompatActivity {
    private ActivityAddListingBinding binding;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityAddListingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v -> {
            String title = binding.etTitle.getText().toString().trim();
            String priceStr = binding.etPrice.getText().toString().trim();
            String desc = binding.etDesc.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(priceStr)) {
                Toast.makeText(this, "Title and Price required", Toast.LENGTH_SHORT).show();
                return;
            }

            SessionManager session = new SessionManager(this);
            MarketplaceListing item = new MarketplaceListing();
            item.setTitle(title);
            item.setPrice(Double.parseDouble(priceStr));
            item.setDescription(desc);
            item.setSellerName(session.getName());
            item.setImageUrl(""); // Optional image url

            binding.progressBar.setVisibility(View.VISIBLE);
            new MarketplaceRepository().addListing(item).observe(this, ok -> {
                binding.progressBar.setVisibility(View.GONE);
                if (Boolean.TRUE.equals(ok)) {
                    Toast.makeText(this, "Listed Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
