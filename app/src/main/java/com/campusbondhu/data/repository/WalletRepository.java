package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.Wallet;
import com.campusbondhu.data.model.WalletTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration walletListener;
    private ListenerRegistration txnListener;

    // Auto-create wallet with 1000 BDT if not exists
    public LiveData<Wallet> getWallet() {
        MutableLiveData<Wallet> ld = new MutableLiveData<>();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        walletListener = db.collection("wallets").document(uid).addSnapshotListener((doc, e) -> {
            if (e != null) return;
            if (doc != null && doc.exists()) {
                ld.setValue(doc.toObject(Wallet.class));
            } else {
                // Initialize wallet
                Wallet w = new Wallet();
                w.setUid(uid);
                w.setBalance(1000.00); // 1000 Demo Taka
                db.collection("wallets").document(uid).set(w);
            }
        });
        return ld;
    }

    public LiveData<List<WalletTransaction>> getTransactions() {
        MutableLiveData<List<WalletTransaction>> ld = new MutableLiveData<>();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        txnListener = db.collection("transactions")
            .whereEqualTo("uid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<WalletTransaction> list = new ArrayList<>();
                snap.forEach(d -> {
                    WalletTransaction t = d.toObject(WalletTransaction.class);
                    if (t != null) { t.setTxnId(d.getId()); list.add(t); }
                });
                ld.setValue(list);
            });
        return ld;
    }

    public LiveData<Boolean> deductBalance(double amount, String description, String orderId) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        db.collection("wallets").document(uid).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) { r.setValue(false); return; }
            Wallet w = doc.toObject(Wallet.class);
            if (w == null || w.getBalance() < amount) { r.setValue(false); return; }

            double newBalance = w.getBalance() - amount;
            WalletTransaction txn = new WalletTransaction();
            txn.setUid(uid);
            txn.setAmount(amount);
            txn.setType("DEBIT");
            txn.setDescription(description);
            txn.setTimestamp(System.currentTimeMillis());

            db.runBatch(batch -> {
                batch.update(db.collection("wallets").document(uid), "balance", newBalance);
                batch.set(db.collection("transactions").document(), txn);
            }).addOnSuccessListener(a -> r.setValue(true))
              .addOnFailureListener(e -> r.setValue(false));
        });
        return r;
    }

    public void removeListeners() {
        if (walletListener != null) walletListener.remove();
        if (txnListener != null) txnListener.remove();
    }
}
