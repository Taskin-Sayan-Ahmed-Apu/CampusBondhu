package com.campusbondhu.ui.canteen;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campusbondhu.data.model.FoodOrder;
import com.campusbondhu.data.repository.CanteenRepository;
import com.campusbondhu.databinding.ActivityOrderFoodBinding;
import com.campusbondhu.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class OrderFoodActivity extends AppCompatActivity {
    private ActivityOrderFoodBinding binding;
    private String itemId, itemName;
    private double price;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityOrderFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        itemId = getIntent().getStringExtra("itemId");
        itemName = getIntent().getStringExtra("name");
        price = getIntent().getDoubleExtra("price", 0);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvFoodName.setText(itemName);
        binding.tvPrice.setText("৳ " + price);
        updateTotal();

        binding.btnMinus.setOnClickListener(v -> { if (quantity > 1) { quantity--; updateTotal(); }});
        binding.btnPlus.setOnClickListener(v -> { quantity++; updateTotal(); });
        
        binding.btnPay.setOnClickListener(v -> placeOrder());
    }

    private void updateTotal() {
        binding.tvQty.setText(String.valueOf(quantity));
        binding.tvTotal.setText("Total: ৳ " + String.format("%.2f", price * quantity));
    }

    private void placeOrder() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPay.setEnabled(false);

        SessionManager session = new SessionManager(this);
        FoodOrder order = new FoodOrder();
        order.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
        order.setStudentName(session.getName());
        order.setItemId(itemId);
        order.setItemName(itemName);
        order.setQuantity(quantity);
        order.setTotalAmount(price * quantity);
        order.setStatus("PENDING");
        order.setTimestamp(System.currentTimeMillis());

        new CanteenRepository().placeOrder(order).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            if ("INSUFFICIENT_FUNDS".equals(result)) {
                binding.btnPay.setEnabled(true);
                Toast.makeText(this, "Insufficient Wallet Balance!", Toast.LENGTH_LONG).show();
            } else if ("ERROR".equals(result)) {
                binding.btnPay.setEnabled(true);
                Toast.makeText(this, "Order failed. Try again.", Toast.LENGTH_SHORT).show();
            } else {
                // Success (result is Pickup Code)
                showPickupDialog(result);
            }
        });
    }

    private void showPickupDialog(String code) {
        new AlertDialog.Builder(this)
            .setTitle("✅ Order Placed!")
            .setMessage("Your Pickup Code is:\n\n" + code + "\n\nShow this to the canteen staff.")
            .setCancelable(false)
            .setPositiveButton("OK", (d, w) -> finish())
            .show();
    }
}
