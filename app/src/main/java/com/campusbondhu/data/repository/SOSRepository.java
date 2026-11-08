package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.EmergencyAlert;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class SOSRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration alertsListener;

    public LiveData<Boolean> triggerSOS(EmergencyAlert alert) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        alert.setStatus("ACTIVE");
        alert.setTimestamp(System.currentTimeMillis());
        db.collection("emergencyAlerts").add(alert)
            .addOnSuccessListener(ref -> {
                ref.update("alertId", ref.getId());
                r.setValue(true);
            }).addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public LiveData<List<EmergencyAlert>> getAllActiveAlerts() {
        MutableLiveData<List<EmergencyAlert>> ld = new MutableLiveData<>();
        alertsListener = db.collection("emergencyAlerts")
            .whereEqualTo("status", "ACTIVE")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<EmergencyAlert> list = new ArrayList<>();
                snap.forEach(d -> {
                    EmergencyAlert alert = d.toObject(EmergencyAlert.class);
                    if (alert != null) { alert.setAlertId(d.getId()); list.add(alert); }
                });
                ld.setValue(list);
            });
        return ld;
    }

    public LiveData<List<EmergencyAlert>> getAlertsForStudent(String studentId) {
        MutableLiveData<List<EmergencyAlert>> ld = new MutableLiveData<>();
        db.collection("emergencyAlerts")
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<EmergencyAlert> list = new ArrayList<>();
                snap.forEach(d -> {
                    EmergencyAlert alert = d.toObject(EmergencyAlert.class);
                    if (alert != null) { alert.setAlertId(d.getId()); list.add(alert); }
                });
                ld.setValue(list);
            });
        return ld;
    }

    public LiveData<Boolean> resolveAlert(String alertId) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("emergencyAlerts").document(alertId).update("status", "RESOLVED")
            .addOnSuccessListener(a -> r.setValue(true))
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public void removeListeners() {
        if (alertsListener != null) alertsListener.remove();
    }
}
