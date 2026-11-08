package com.campusbondhu.ui.canteen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.CanteenItem;
import com.campusbondhu.data.repository.CanteenRepository;
import com.campusbondhu.databinding.ActivityCanteenMenuBinding;
import com.campusbondhu.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class CanteenMenuActivity extends AppCompatActivity {
    private ActivityCanteenMenuBinding binding;
    private final List<CanteenItem> data = new ArrayList<>();
    private MenuAdapter adapter;
    private CanteenRepository repo = new CanteenRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCanteenMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        overridePendingTransition(com.campusbondhu.R.anim.slide_in_right, com.campusbondhu.R.anim.fade_out);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnMyOrders.setOnClickListener(v -> startActivity(new Intent(this, MyOrdersActivity.class)));

        SessionManager session = new SessionManager(this);
        if ("admin".equals(session.getRole())) {
            binding.btnAddFood.setVisibility(View.VISIBLE);
            binding.btnAddFood.setOnClickListener(v -> startActivity(new Intent(this, AddFoodActivity.class)));
            binding.btnManageOrders.setVisibility(View.VISIBLE);
            binding.btnManageOrders.setOnClickListener(v -> startActivity(new Intent(this, ManageOrdersActivity.class)));
        }

        binding.rvMenu.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MenuAdapter();
        binding.rvMenu.setAdapter(adapter);

        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.rvMenu.setVisibility(View.GONE);

        repo.getItems().observe(this, list -> {
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
            binding.rvMenu.setVisibility(View.VISIBLE);

            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();

            if (data.isEmpty()) {
                binding.emptyState.getRoot().setVisibility(View.VISIBLE);
                binding.emptyState.tvEmptyEmoji.setText("🍔");
                binding.emptyState.tvEmptyTitle.setText("No Food Available");
                binding.emptyState.tvEmptySubtitle.setText("The canteen menu is currently empty.");
            } else {
                binding.emptyState.getRoot().setVisibility(View.GONE);
            }
        });
    }

    @Override public void finish() { super.finish(); overridePendingTransition(com.campusbondhu.R.anim.fade_in, com.campusbondhu.R.anim.slide_out_left); }
    @Override protected void onDestroy() { super.onDestroy(); repo.removeListeners(); }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(com.campusbondhu.R.layout.item_food, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            CanteenItem item = data.get(pos);
            h.tvName.setText(item.getName());
            h.tvDesc.setText(item.getDescription());
            h.tvPrice.setText("৳ " + String.format("%.2f", item.getPrice()));
            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(CanteenMenuActivity.this, OrderFoodActivity.class);
                i.putExtra("itemId", item.getItemId());
                i.putExtra("name", item.getName());
                i.putExtra("price", item.getPrice());
                startActivity(i);
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc, tvPrice;
            VH(View v) {
                super(v);
                tvName = v.findViewById(com.campusbondhu.R.id.tvFoodName);
                tvDesc = v.findViewById(com.campusbondhu.R.id.tvFoodDesc);
                tvPrice = v.findViewById(com.campusbondhu.R.id.tvFoodPrice);
            }
        }
    }
}
