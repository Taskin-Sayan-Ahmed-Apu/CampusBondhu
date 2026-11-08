package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.StudyResource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class StudyHubRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration resourceListener;

    public LiveData<Boolean> addResource(StudyResource res) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("studyResources").add(res)
            .addOnSuccessListener(ref -> {
                ref.update("resourceId", ref.getId());
                r.setValue(true);
            })
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public LiveData<List<StudyResource>> getAllResources() {
        MutableLiveData<List<StudyResource>> ld = new MutableLiveData<>();
        resourceListener = db.collection("studyResources")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                List<StudyResource> list = new ArrayList<>();
                snap.forEach(d -> {
                    StudyResource res = d.toObject(StudyResource.class);
                    if (res != null) { res.setResourceId(d.getId()); list.add(res); }
                });
                ld.setValue(list);
            });
        return ld;
    }

    public void removeListeners() {
        if (resourceListener != null) resourceListener.remove();
    }
}
