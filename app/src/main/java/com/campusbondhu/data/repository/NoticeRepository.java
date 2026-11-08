package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.Notice;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class NoticeRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Notice>> getNotices() {
        MutableLiveData<List<Notice>> ld = new MutableLiveData<>();
        db.collection("notices").orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener(snap -> {
                List<Notice> list = new ArrayList<>();
                snap.forEach(d -> {
                    Notice n = d.toObject(Notice.class);
                    if (n != null) { n.setNoticeId(d.getId()); list.add(n); }
                });
                ld.setValue(list);
            })
            .addOnFailureListener(e -> ld.setValue(new ArrayList<>()));
        return ld;
    }

    public LiveData<Boolean> createNotice(Notice n) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        n.setAuthorId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        n.setTimestamp(System.currentTimeMillis());
        db.collection("notices").add(n)
            .addOnSuccessListener(ref -> { r.setValue(true); })
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }
}
