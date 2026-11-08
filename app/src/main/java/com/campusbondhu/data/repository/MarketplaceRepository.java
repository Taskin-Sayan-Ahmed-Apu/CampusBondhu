package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.MarketplaceListing;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listListener;

    public LiveData<Boolean> addListing(MarketplaceListing item) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        item.setSellerId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        item.setStatus("AVAILABLE");
        item.setTimestamp(System.currentTimeMillis()); // Added Timestamp
        db.collection("marketplaceListings").add(item)
            .addOnSuccessListener(ref -> {
                ref.update("listingId", ref.getId());
                r.setValue(true);
            }).addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public LiveData<List<MarketplaceListing>> getAllListings() {
        MutableLiveData<List<MarketplaceListing>> ld = new MutableLiveData<>();
        listListener = db.collection("marketplaceListings").addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            List<MarketplaceListing> list = new ArrayList<>();
            snap.forEach(d -> {
                MarketplaceListing item = d.toObject(MarketplaceListing.class);
                if (item != null) { item.setListingId(d.getId()); list.add(item); }
            });
            ld.setValue(list);
        });
        return ld;
    }

    public LiveData<Boolean> buyItem(MarketplaceListing item, String buyerId) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("marketplaceListings").document(item.getListingId())
            .update("status", "SOLD")
            .addOnSuccessListener(a -> r.setValue(true))
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public void removeListeners() {
        if (listListener != null) listListener.remove();
    }
}
