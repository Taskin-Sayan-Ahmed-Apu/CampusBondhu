package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.AttendanceRecord;
import com.campusbondhu.data.model.AttendanceSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<String> createSession(String courseCode, String courseName, int durationMinutes) {
        MutableLiveData<String> result = new MutableLiveData<>();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long now = System.currentTimeMillis();

        Map<String, Object> session = new HashMap<>();
        session.put("courseCode", courseCode);
        session.put("courseName", courseName);
        session.put("teacherId", uid);
        session.put("teacherName", "");
        session.put("createdAt", now);
        session.put("expiresAt", now + durationMinutes * 60_000L);
        session.put("active", true);
        session.put("durationMinutes", durationMinutes);

        db.collection("attendanceSessions").add(session)
            .addOnSuccessListener(ref -> {
                ref.update("sessionId", ref.getId());
                result.setValue(ref.getId());
            })
            .addOnFailureListener(e -> result.setValue(null));
        return result;
    }

    public LiveData<AttendanceSession> getSession(String sessionId) {
        MutableLiveData<AttendanceSession> ld = new MutableLiveData<>();
        db.collection("attendanceSessions").document(sessionId).get()
            .addOnSuccessListener(d -> {
                if (d.exists()) {
                    AttendanceSession s = d.toObject(AttendanceSession.class);
                    if (s != null) s.setSessionId(d.getId());
                    ld.setValue(s);
                } else {
                    ld.setValue(null);
                }
            })
            .addOnFailureListener(e -> ld.setValue(null));
        return ld;
    }

    public LiveData<Boolean> stopSession(String sessionId) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("attendanceSessions").document(sessionId)
            .update("active", false)
            .addOnSuccessListener(a -> r.setValue(true))
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    /** Returns "OK", "DUPLICATE", "EXPIRED", "CLOSED", or "ERROR". */
    public LiveData<String> submitAttendance(String sessionId, String studentId, String studentName) {
        MutableLiveData<String> r = new MutableLiveData<>();
        db.collection("attendanceSessions").document(sessionId).get()
            .addOnSuccessListener(d -> {
                if (!d.exists()) { r.setValue("CLOSED"); return; }
                AttendanceSession s = d.toObject(AttendanceSession.class);
                if (s == null) { r.setValue("ERROR"); return; }
                if (!s.isActive()) { r.setValue("CLOSED"); return; }
                if (System.currentTimeMillis() > s.getExpiresAt()) { r.setValue("EXPIRED"); return; }

                db.collection("attendanceRecords")
                    .whereEqualTo("sessionId", sessionId)
                    .whereEqualTo("studentId", studentId)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.isEmpty()) { r.setValue("DUPLICATE"); return; }
                        Map<String, Object> rec = new HashMap<>();
                        rec.put("sessionId", sessionId);
                        rec.put("courseCode", s.getCourseCode());
                        rec.put("studentId", studentId);
                        rec.put("studentName", studentName);
                        rec.put("timestamp", System.currentTimeMillis());
                        rec.put("faceVerified", true);
                        db.collection("attendanceRecords").add(rec)
                            .addOnSuccessListener(ref -> {
                                ref.update("recordId", ref.getId());
                                r.setValue("OK");
                            })
                            .addOnFailureListener(e -> r.setValue("ERROR"));
                    })
                    .addOnFailureListener(e -> r.setValue("ERROR"));
            })
            .addOnFailureListener(e -> r.setValue("ERROR"));
        return r;
    }

    public LiveData<List<AttendanceRecord>> getRecordsForStudent(String studentId) {
        MutableLiveData<List<AttendanceRecord>> ld = new MutableLiveData<>();
        db.collection("attendanceRecords")
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snap -> {
                List<AttendanceRecord> list = new ArrayList<>();
                snap.forEach(doc -> {
                    AttendanceRecord rec = doc.toObject(AttendanceRecord.class);
                    if (rec != null) { rec.setRecordId(doc.getId()); list.add(rec); }
                });
                ld.setValue(list);
            })
            .addOnFailureListener(e -> ld.setValue(new ArrayList<>()));
        return ld;
    }

    public LiveData<List<AttendanceSession>> getSessionsByTeacher(String teacherId) {
        MutableLiveData<List<AttendanceSession>> ld = new MutableLiveData<>();
        db.collection("attendanceSessions")
            .whereEqualTo("teacherId", teacherId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snap -> {
                List<AttendanceSession> list = new ArrayList<>();
                snap.forEach(doc -> {
                    AttendanceSession s = doc.toObject(AttendanceSession.class);
                    if (s != null) { s.setSessionId(doc.getId()); list.add(s); }
                });
                ld.setValue(list);
            })
            .addOnFailureListener(e -> ld.setValue(new ArrayList<>()));
        return ld;
    }

    public LiveData<Integer> countRecordsForSession(String sessionId) {
        MutableLiveData<Integer> ld = new MutableLiveData<>();
        db.collection("attendanceRecords").whereEqualTo("sessionId", sessionId).get()
            .addOnSuccessListener(snap -> ld.setValue(snap.size()))
            .addOnFailureListener(e -> ld.setValue(0));
        return ld;
    }
}
