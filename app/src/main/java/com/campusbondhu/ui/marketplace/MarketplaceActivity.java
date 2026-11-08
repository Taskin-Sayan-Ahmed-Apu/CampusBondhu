package com.campusbondhu.ui.marketplace;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.MarketplaceListing;
import com.campusbondhu.data.repository.MarketplaceRepository;
import com.campusbondhu.databinding.ActivityMarketplaceBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceActivity extends AppCompatActivity {
    private ActivityMarketplaceBinding binding;
    private final List<MarketplaceListing> data = new ArrayList<>();
    private MarketAdapter adapter;
    private MarketplaceRepository repo = new MarketplaceRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMarketplaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        overridePendingTransition(com.campusbondhu.R.anim.slide_in_right, com.campusbondhu.R.anim.fade_out);
        
        binding.btnBack.setOnClickListener(v -> finish());
        binding.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddListingActivity.class)));

        binding.rvMarket.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MarketAdapter();
        binding.rvMarket.setAdapter(adapter);

        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.rvMarket.setVisibility(View.GONE);

        repo.getAllListings().observe(this, list -> {
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
            binding.rvMarket.setVisibility(View.VISIBLE);

            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();

            if (data.isEmpty()) {
                binding.emptyState.getRoot().setVisibility(View.VISIBLE);
                binding.emptyState.tvEmptyEmoji.setText("🛒");
                binding.emptyState.tvEmptyTitle.setText("Marketplace is Empty");
                binding.emptyState.tvEmptySubtitle.setText("Be the first to list an item for sale!");
            } else {
                binding.emptyState.getRoot().setVisibility(View.GONE);
            }
        });
    }

    @Override public void finish() { super.finish(); overridePendingTransition(com.campusbondhu.R.anim.fade_in, com.campusbondhu.R.anim.slide_out_left); }
    @Override protected void onDestroy() { super.onDestroy(); repo.removeListeners(); }

    private class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(com.campusbondhu.R.layout.item_marketplace, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            MarketplaceListing item = data.get(pos);
            h.tvTitle.setText(item.getTitle());
            h.tvPrice.setText("৳ " + String.format("%.2f", item.getPrice()));
            h.tvSeller.setText("By " + item.getSellerName());
            if ("SOLD".equals(item.getStatus())) {
                h.tvStatus.setText("SOLD"); h.tvStatus.setTextColor(getColor(com.campusbondhu.R.color.red_sos));
            } else {
                h.tvStatus.setText("AVAILABLE"); h.tvStatus.setTextColor(getColor(com.campusbondhu.R.color.teal_success));
            }
            h.itemView.setOnClickListener(v -> {
                if ("AVAILABLE".equals(item.getStatus()) && !item.getSellerId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    new AlertDialog.Builder(MarketplaceActivity.this)
                        .setTitle("Buy Item")
                        .setMessage("Price: ৳ " + item.getPrice() + "\n\nDo you want to buy this item using Demo Wallet?")
                        .setPositiveButton("Buy", (d, w) -> {
                            repo.buyItem(item, FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .observe(MarketplaceActivity.this, ok -> {
                                    if (Boolean.TRUE.equals(ok)) {
                                        android.widget.Toast.makeText(MarketplaceActivity.this, "Item Purchased!", android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                });
                        })
                        .setNegativeButton("Cancel", null).show();
                }
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvPrice, tvSeller, tvStatus;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(com.campusbondhu.R.id.tvMpTitle);
                tvPrice = v.findViewById(com.campusbondhu.R.id.tvMpPrice);
                tvSeller = v.findViewById(com.campusbondhu.R.id.tvMpSeller);
                tvStatus = v.findViewById(com.campusbondhu.R.id.tvMpStatus);
            }
        }
    }
}
