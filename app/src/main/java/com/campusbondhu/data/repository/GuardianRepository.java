package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.AttendanceRecord;
import com.campusbondhu.data.model.EmergencyAlert;
import com.campusbondhu.data.model.GuardianLink;
import com.campusbondhu.data.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class GuardianRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<String> linkStudent(String guardianId, String studentRoll) {
        MutableLiveData<String> r = new MutableLiveData<>();
        db.collection("users")
            .whereEqualTo("studentId", studentRoll)
            .whereEqualTo("role", "student")
            .get()
            .addOnSuccessListener(snap -> {
                if (snap.isEmpty()) { r.setValue("NOT_FOUND"); return; }
                String studentId = snap.getDocuments().get(0).getId();
                
                GuardianLink link = new GuardianLink();
                link.setGuardianId(guardianId);
                link.setStudentId(studentId);
                
                db.collection("guardianLinks").add(link)
                    .addOnSuccessListener(ref -> r.setValue("OK"))
                    .addOnFailureListener(e -> r.setValue("ERROR"));
            }).addOnFailureListener(e -> r.setValue("ERROR"));
        return r;
    }

    // রিয়েল-টাইম লিসেনার যুক্ত করা হলো
    public LiveData<User> getLinkedStudent(String guardianId) {
        MutableLiveData<User> ld = new MutableLiveData<>();
        db.collection("guardianLinks")
            .whereEqualTo("guardianId", guardianId)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                if (!snap.isEmpty()) {
                    String studentId = snap.getDocuments().get(0).getString("studentId");
                    if (studentId != null) {
                        db.collection("users").document(studentId).addSnapshotListener((d, err) -> {
                            if (d != null && d.exists()) {
                                ld.setValue(d.toObject(User.class));
                            }
                        });
                    }
                } else {
                    ld.setValue(null);
                }
            });
        return ld;
    }

    public LiveData<List<AttendanceRecord>> getStudentAttendance(String studentId) {
        MutableLiveData<List<AttendanceRecord>> ld = new MutableLiveData<>();
        db.collection("attendanceRecords")
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<AttendanceRecord> list = new ArrayList<>();
                snap.forEach(d -> {
                    AttendanceRecord rec = d.toObject(AttendanceRecord.class);
                    if (rec != null) list.add(rec);
                });
                ld.setValue(list);
            });
        return ld;
    }

    public LiveData<List<EmergencyAlert>> getStudentAlerts(String studentId) {
        MutableLiveData<List<EmergencyAlert>> ld = new MutableLiveData<>();
        db.collection("emergencyAlerts")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<EmergencyAlert> list = new ArrayList<>();
                snap.forEach(d -> {
                    EmergencyAlert alert = d.toObject(EmergencyAlert.class);
                    if (alert != null) list.add(alert);
                });
                ld.setValue(list);
            });
        return ld;
    }
}
