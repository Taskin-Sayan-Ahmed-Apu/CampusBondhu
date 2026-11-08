package com.campusbondhu.ui.canteen;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.FoodOrder;
import com.campusbondhu.data.repository.CanteenRepository;
import com.campusbondhu.databinding.ActivityManageOrdersBinding;
import java.util.ArrayList;
import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity {
    private ActivityManageOrdersBinding binding;
    private final List<FoodOrder> data = new ArrayList<>();
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityManageOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter();
        binding.rvOrders.setAdapter(adapter);

        new CanteenRepository().getAllPendingOrders().observe(this, list -> {
            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();
            binding.tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(
                com.campusbondhu.R.layout.item_manage_order, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            FoodOrder o = data.get(pos);
            h.tvName.setText(o.getStudentName() + " - " + o.getItemName() + " x" + o.getQuantity());
            h.tvCode.setText("Code: " + o.getPickupCode());
            h.btnCollect.setOnClickListener(v -> {
                h.btnCollect.setEnabled(false);
                new CanteenRepository().markCollected(o.getOrderId()).observe(ManageOrdersActivity.this, ok -> {
                    if (Boolean.TRUE.equals(ok)) {
                        Toast.makeText(ManageOrdersActivity.this, "Order Collected", Toast.LENGTH_SHORT).show();
                    } else {
                        h.btnCollect.setEnabled(true);
                        Toast.makeText(ManageOrdersActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvCode;
            com.google.android.material.button.MaterialButton btnCollect;
            VH(View v) {
                super(v);
                tvName = v.findViewById(com.campusbondhu.R.id.tvManageName);
                tvCode = v.findViewById(com.campusbondhu.R.id.tvManageCode);
                btnCollect = v.findViewById(com.campusbondhu.R.id.btnCollect);
            }
        }
    }
}
