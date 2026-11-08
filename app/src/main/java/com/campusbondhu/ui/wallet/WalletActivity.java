package com.campusbondhu.ui.wallet;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.WalletTransaction;
import com.campusbondhu.data.repository.WalletRepository;
import com.campusbondhu.databinding.ActivityWalletBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WalletActivity extends AppCompatActivity {
    private ActivityWalletBinding binding;
    private final List<WalletTransaction> data = new ArrayList<>();
    private TxnAdapter adapter;
    private WalletRepository repo = new WalletRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        // ✅ ডেমো টাকা অ্যাড করার বাটন
        binding.btnTopUp.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("wallets").document(uid)
                .update("balance", FieldValue.increment(500)).addOnSuccessListener(a -> {
                    // ট্রানজেকশন রেকর্ড
                    Map<String, Object> txn = new HashMap<>();
                    txn.put("uid", uid);
                    txn.put("amount", 500.0);
                    txn.put("type", "CREDIT");
                    txn.put("description", "Demo Top-Up");
                    txn.put("timestamp", System.currentTimeMillis());
                    FirebaseFirestore.getInstance().collection("transactions").add(txn);
                    Toast.makeText(this, "৳ 500 Added!", Toast.LENGTH_SHORT).show();
                });
        });

        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TxnAdapter();
        binding.rvTransactions.setAdapter(adapter);

        repo.getWallet().observe(this, w -> {
            if (w != null) binding.tvBalance.setText("৳ " + String.format("%.2f", w.getBalance()));
        });

        repo.getTransactions().observe(this, list -> {
            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();
            binding.tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repo.removeListeners();
    }

    private class TxnAdapter extends RecyclerView.Adapter<TxnAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(
                com.campusbondhu.R.layout.item_transaction, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            WalletTransaction tx = data.get(pos);
            h.tvDesc.setText(tx.getDescription());
            h.tvDate.setText(new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                .format(new Date(tx.getTimestamp())));
            
            if ("CREDIT".equals(tx.getType())) {
                h.tvAmount.setText("+ ৳ " + String.format("%.2f", tx.getAmount()));
                h.tvAmount.setTextColor(getColor(com.campusbondhu.R.color.teal_success));
            } else {
                h.tvAmount.setText("- ৳ " + String.format("%.2f", tx.getAmount()));
                h.tvAmount.setTextColor(getColor(com.campusbondhu.R.color.red_sos));
            }
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvDesc, tvDate, tvAmount;
            VH(View v) {
                super(v);
                tvDesc = v.findViewById(com.campusbondhu.R.id.tvTxnDesc);
                tvDate = v.findViewById(com.campusbondhu.R.id.tvTxnDate);
                tvAmount = v.findViewById(com.campusbondhu.R.id.tvTxnAmount);
            }
        }
    }
}
