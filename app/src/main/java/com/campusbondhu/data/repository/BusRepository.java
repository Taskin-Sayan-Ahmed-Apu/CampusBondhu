package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.Bus;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration busListener;

    public LiveData<Boolean> addBus(Bus bus) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("buses").add(bus)
            .addOnSuccessListener(ref -> {
                ref.update("busId", ref.getId());
                r.setValue(true);
            })
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public LiveData<List<Bus>> getAllBuses() {
        MutableLiveData<List<Bus>> ld = new MutableLiveData<>();
        busListener = db.collection("buses").addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            List<Bus> list = new ArrayList<>();
            snap.forEach(d -> {
                Bus b = d.toObject(Bus.class);
                if (b != null) { b.setBusId(d.getId()); list.add(b); }
            });
            ld.setValue(list);
        });
        return ld;
    }

    public LiveData<Boolean> updateLocation(String busId, double lat, double lng) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        Map<String, Object> updates = new HashMap<>();
        updates.put("latitude", lat);
        updates.put("longitude", lng);
        updates.put("lastUpdated", System.currentTimeMillis());

        db.collection("buses").document(busId).update(updates)
            .addOnSuccessListener(a -> r.setValue(true))
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public void removeListeners() {
        if (busListener != null) busListener.remove();
    }
}
