package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.CanteenItem;
import com.campusbondhu.data.model.FoodOrder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CanteenRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration itemsListener;

    public LiveData<List<CanteenItem>> getItems() {
        MutableLiveData<List<CanteenItem>> ld = new MutableLiveData<>();
        itemsListener = db.collection("canteenItems").addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            List<CanteenItem> list = new ArrayList<>();
            snap.forEach(d -> {
                CanteenItem item = d.toObject(CanteenItem.class);
                if (item != null) { item.setItemId(d.getId()); list.add(item); }
            });
            ld.setValue(list);
        });
        return ld;
    }

    public LiveData<Boolean> addItem(CanteenItem item) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("canteenItems").add(item)
            .addOnSuccessListener(ref -> {
                ref.update("itemId", ref.getId());
                r.setValue(true);
            })
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public LiveData<String> placeOrder(FoodOrder order) {
        MutableLiveData<String> r = new MutableLiveData<>();
        // Generate a 6-character Pickup Code
        String pickupCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        order.setPickupCode(pickupCode);
        
        new WalletRepository().deductBalance(order.getTotalAmount(), 
            "Canteen Order: " + order.getItemName(), "").observeForever(success -> {
            if (Boolean.TRUE.equals(success)) {
                db.collection("orders").add(order)
                    .addOnSuccessListener(ref -> {
                        ref.update("orderId", ref.getId());
                        r.setValue(pickupCode);
                    })
                    .addOnFailureListener(e -> r.setValue("ERROR"));
            } else {
                r.setValue("INSUFFICIENT_FUNDS");
            }
        });
        return r;
    }

    public LiveData<List<FoodOrder>> getMyOrders(String uid) {
        MutableLiveData<List<FoodOrder>> ld = new MutableLiveData<>();
        db.collection("orders")
            .whereEqualTo("uid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<FoodOrder> list = new ArrayList<>();
                snap.forEach(d -> {
                    FoodOrder o = d.toObject(FoodOrder.class);
                    if (o != null) { o.setOrderId(d.getId()); list.add(o); }
                });
                ld.setValue(list);
            });
        return ld;
    }

    public LiveData<List<FoodOrder>> getAllPendingOrders() {
        MutableLiveData<List<FoodOrder>> ld = new MutableLiveData<>();
        db.collection("orders")
            .whereEqualTo("status", "PENDING")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<FoodOrder> list = new ArrayList<>();
                snap.forEach(d -> {
                    FoodOrder o = d.toObject(FoodOrder.class);
                    if (o != null) { o.setOrderId(d.getId()); list.add(o); }
                });
                ld.setValue(list);
            });
        return ld;
    }

    public LiveData<Boolean> markCollected(String orderId) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("orders").document(orderId).update("status", "COLLECTED")
            .addOnSuccessListener(a -> r.setValue(true))
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public void removeListeners() {
        if (itemsListener != null) itemsListener.remove();
    }
}
