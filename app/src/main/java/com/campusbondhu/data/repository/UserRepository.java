package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class UserRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public LiveData<User> getCurrentUser() {
        MutableLiveData<User> ld = new MutableLiveData<>();
        if (auth.getCurrentUser() == null) { ld.setValue(null); return ld; }
        db.collection("users").document(auth.getCurrentUser().getUid()).get()
            .addOnSuccessListener(d -> ld.setValue(d.toObject(User.class)))
            .addOnFailureListener(e -> ld.setValue(null));
        return ld;
    }

    public LiveData<Boolean> updateProfile(String uid, Map<String, Object> updates) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener(a -> r.setValue(true))
            .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public void logout() { auth.signOut(); }
}
