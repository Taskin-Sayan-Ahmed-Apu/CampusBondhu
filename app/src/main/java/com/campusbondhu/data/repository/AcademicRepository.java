package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.Course;
import com.campusbondhu.data.model.RoutineItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class AcademicRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration courseListener, routineListener;

    public LiveData<Boolean> addCourse(Course c) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("academicCourses").add(c)
            .addOnSuccessListener(ref -> { ref.update("courseId", ref.getId()); r.setValue(true); })
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public LiveData<List<Course>> getAllCourses() {
        MutableLiveData<List<Course>> ld = new MutableLiveData<>();
        courseListener = db.collection("academicCourses").addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            List<Course> list = new ArrayList<>();
            snap.forEach(d -> {
                Course c = d.toObject(Course.class);
                if (c != null) { c.setCourseId(d.getId()); list.add(c); }
            });
            ld.setValue(list);
        });
        return ld;
    }

    public LiveData<Boolean> addRoutine(RoutineItem item) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("routines").add(item)
            .addOnSuccessListener(ref -> { ref.update("routineId", ref.getId()); r.setValue(true); })
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public LiveData<List<RoutineItem>> getAllRoutines() {
        MutableLiveData<List<RoutineItem>> ld = new MutableLiveData<>();
        routineListener = db.collection("routines").addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            List<RoutineItem> list = new ArrayList<>();
            snap.forEach(d -> {
                RoutineItem i = d.toObject(RoutineItem.class);
                if (i != null) { i.setRoutineId(d.getId()); list.add(i); }
            });
            ld.setValue(list);
        });
        return ld;
    }
    
    public void removeListeners() {
        if (courseListener != null) courseListener.remove();
        if (routineListener != null) routineListener.remove();
    }
}
