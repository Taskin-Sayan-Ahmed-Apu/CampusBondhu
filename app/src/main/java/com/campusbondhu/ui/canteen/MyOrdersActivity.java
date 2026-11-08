package com.campusbondhu.ui.canteen;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.FoodOrder;
import com.campusbondhu.data.repository.CanteenRepository;
import com.campusbondhu.databinding.ActivityMyOrdersBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyOrdersActivity extends AppCompatActivity {
    private ActivityMyOrdersBinding binding;
    private final List<FoodOrder> data = new ArrayList<>();
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityMyOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        overridePendingTransition(com.campusbondhu.R.anim.slide_in_right, com.campusbondhu.R.anim.fade_out);
        
        binding.btnBack.setOnClickListener(v -> finish());
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter();
        binding.rvOrders.setAdapter(adapter);

        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.rvOrders.setVisibility(View.GONE);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        new CanteenRepository().getMyOrders(uid).observe(this, list -> {
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
            binding.rvOrders.setVisibility(View.VISIBLE);

            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();

            if (data.isEmpty()) {
                binding.emptyState.getRoot().setVisibility(View.VISIBLE);
                binding.emptyState.tvEmptyEmoji.setText("🧾");
                binding.emptyState.tvEmptyTitle.setText("No Orders Yet");
                binding.emptyState.tvEmptySubtitle.setText("Hungry? Order something from the canteen!");
            } else {
                binding.emptyState.getRoot().setVisibility(View.GONE);
            }
        });
    }

    @Override public void finish() { super.finish(); overridePendingTransition(com.campusbondhu.R.anim.fade_in, com.campusbondhu.R.anim.slide_out_left); }

    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(com.campusbondhu.R.layout.item_order, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            FoodOrder o = data.get(pos);
            h.tvName.setText(o.getItemName() + " x" + o.getQuantity());
            h.tvAmount.setText("৳ " + String.format("%.2f", o.getTotalAmount()));
            h.tvCode.setText("Pickup Code: " + o.getPickupCode());
            h.tvDate.setText(new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date(o.getTimestamp())));
            if ("COLLECTED".equals(o.getStatus())) {
                h.tvStatus.setText("COLLECTED"); h.tvStatus.setTextColor(getColor(com.campusbondhu.R.color.teal_success));
            } else {
                h.tvStatus.setText("PENDING"); h.tvStatus.setTextColor(getColor(com.campusbondhu.R.color.orange_warning));
            }
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvAmount, tvCode, tvDate, tvStatus;
            VH(View v) {
                super(v);
                tvName = v.findViewById(com.campusbondhu.R.id.tvOrderName);
                tvAmount = v.findViewById(com.campusbondhu.R.id.tvOrderAmount);
                tvCode = v.findViewById(com.campusbondhu.R.id.tvPickupCode);
                tvDate = v.findViewById(com.campusbondhu.R.id.tvOrderDate);
                tvStatus = v.findViewById(com.campusbondhu.R.id.tvOrderStatus);
            }
        }
    }
}
