package com.campusbondhu.ui.canteen;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.model.CanteenItem;
import com.campusbondhu.data.repository.CanteenRepository;
import com.campusbondhu.databinding.ActivityAddFoodBinding;

public class AddFoodActivity extends AppCompatActivity {
    private ActivityAddFoodBinding binding;
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityAddFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String priceStr = binding.etPrice.getText().toString().trim();
            String desc = binding.etDesc.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                Toast.makeText(this, "Name and Price required", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            CanteenItem item = new CanteenItem();
            item.setName(name);
            item.setPrice(price);
            item.setDescription(desc);
            item.setAvailable(true);

            binding.progressBar.setVisibility(View.VISIBLE);
            new CanteenRepository().addItem(item).observe(this, ok -> {
                binding.progressBar.setVisibility(View.GONE);
                if (Boolean.TRUE.equals(ok)) {
                    Toast.makeText(this, "Food Added", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
