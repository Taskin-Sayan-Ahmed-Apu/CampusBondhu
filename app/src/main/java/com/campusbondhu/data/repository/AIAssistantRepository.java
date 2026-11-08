package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.Notice;
import com.campusbondhu.data.model.RoutineItem;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class AIAssistantRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<String> askAssistant(String query) {
        MutableLiveData<String> response = new MutableLiveData<>();
        String lower = query.toLowerCase();

        if (lower.contains("notice") || lower.contains("announcement")) {
            db.collection("notices").limit(2).get().addOnSuccessListener(snap -> {
                if (snap.isEmpty()) {
                    response.setValue("There are no notices right now.");
                } else {
                    StringBuilder sb = new StringBuilder("Here are the latest notices:\n");
                    snap.forEach(d -> {
                        Notice n = d.toObject(Notice.class);
                        if (n != null) sb.append("• ").append(n.getTitle()).append("\n");
                    });
                    response.setValue(sb.toString());
                }
            });
        } else if (lower.contains("routine") || lower.contains("class")) {
            db.collection("routines").limit(3).get().addOnSuccessListener(snap -> {
                if (snap.isEmpty()) {
                    response.setValue("No routine has been uploaded yet.");
                } else {
                    StringBuilder sb = new StringBuilder("Here is your upcoming routine:\n");
                    snap.forEach(d -> {
                        RoutineItem r = d.toObject(RoutineItem.class);
                        if (r != null) sb.append("• ").append(r.getCourseCode()).append(" at ").append(r.getTime()).append("\n");
                    });
                    response.setValue(sb.toString());
                }
            });
        } else if (lower.contains("book") || lower.contains("library")) {
            db.collection("books").limit(3).get().addOnSuccessListener(snap -> {
                if (snap.isEmpty()) {
                    response.setValue("The library catalog is empty.");
                } else {
                    StringBuilder sb = new StringBuilder("Some books in the library:\n");
                    snap.forEach(d -> {
                        String title = d.getString("title");
                        if (title != null) sb.append("• ").append(title).append("\n");
                    });
                    response.setValue(sb.toString());
                }
            });
        } else if (lower.contains("hello") || lower.contains("hi")) {
            response.setValue("Hello! I am your CampusBondhu AI Assistant. You can ask me about Notices, Routines, or Library books.");
        } else {
            response.setValue("I am a demo assistant. I can only help you with Notices, Routines, and Library books right now.");
        }
        return response;
    }
}
