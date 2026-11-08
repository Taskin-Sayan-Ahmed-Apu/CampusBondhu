package com.campusbondhu.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.MarketplaceListing;
import com.campusbondhu.databinding.ActivityManageMarketplaceBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class ManageMarketplaceActivity extends AppCompatActivity {
    private ActivityManageMarketplaceBinding binding;
    private final List<MarketplaceListing> data = new ArrayList<>();
    private MarketAdapter adapter;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityManageMarketplaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvMarket.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MarketAdapter();
        binding.rvMarket.setAdapter(adapter);

        listener = FirebaseFirestore.getInstance().collection("marketplaceListings")
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                data.clear();
                snap.forEach(d -> {
                    MarketplaceListing item = d.toObject(MarketplaceListing.class);
                    if (item != null) { item.setListingId(d.getId()); data.add(item); }
                });
                adapter.notifyDataSetChanged();
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }

    private class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(
                com.campusbondhu.R.layout.item_manage_market, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            MarketplaceListing item = data.get(pos);
            h.tvTitle.setText(item.getTitle());
            h.tvSeller.setText("Seller: " + item.getSellerName());
            h.btnDelete.setOnClickListener(v -> {
                FirebaseFirestore.getInstance().collection("marketplaceListings").document(item.getListingId())
                    .delete().addOnSuccessListener(a -> Toast.makeText(ManageMarketplaceActivity.this, "Deleted", Toast.LENGTH_SHORT).show());
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSeller;
            com.google.android.material.button.MaterialButton btnDelete;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(com.campusbondhu.R.id.tvMpManageTitle);
                tvSeller = v.findViewById(com.campusbondhu.R.id.tvMpManageSeller);
                btnDelete = v.findViewById(com.campusbondhu.R.id.btnDeleteListing);
            }
        }
    }
}
