package com.campusbondhu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.campusbondhu.data.model.Book;
import com.campusbondhu.data.model.LibraryLoan;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class LibraryRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration booksListener;
    private ListenerRegistration loansListener;

    public LiveData<Boolean> addBook(Book book) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.collection("books").add(book)
                .addOnSuccessListener(ref -> {
                    ref.update("bookId", ref.getId());
                    r.setValue(true);
                })
                .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public LiveData<List<Book>> getBooks() {
        MutableLiveData<List<Book>> ld = new MutableLiveData<>();
        booksListener = db.collection("books").addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            List<Book> list = new ArrayList<>();
            snap.forEach(d -> {
                Book b = d.toObject(Book.class);
                if (b != null) { b.setBookId(d.getId()); list.add(b); }
            });
            ld.setValue(list);
        });
        return ld;
    }

    public LiveData<String> issueBook(String bookId, String studentId, String studentName) {
        MutableLiveData<String> r = new MutableLiveData<>();
        db.collection("books").document(bookId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) { r.setValue("NOT_FOUND"); return; }
            Book book = doc.toObject(Book.class);

            // এখানে চেক করা হচ্ছে বইটা অলরেডি ইস্যুড কি না
            if (book != null && !book.isAvailable()) { r.setValue("UNAVAILABLE"); return; }

            db.collection("libraryLoans")
                    .whereEqualTo("bookId", bookId)
                    .whereEqualTo("studentId", studentId)
                    .whereEqualTo("status", "ISSUED")
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.isEmpty()) { r.setValue("DUPLICATE"); return; }

                        Calendar cal = Calendar.getInstance();
                        long issueDate = cal.getTimeInMillis();
                        cal.add(Calendar.DAY_OF_MONTH, 14);
                        long dueDate = cal.getTimeInMillis();

                        LibraryLoan loan = new LibraryLoan();
                        loan.setBookId(bookId);
                        loan.setBookTitle(book.getTitle());
                        loan.setStudentId(studentId);
                        loan.setStudentName(studentName);
                        loan.setIssueDate(issueDate);
                        loan.setDueDate(dueDate);
                        loan.setStatus("ISSUED");

                        db.runBatch(batch -> {
                                    // এখানে "isAvailable" এর জায়গায় "available" করা হলো
                                    batch.update(doc.getReference(), "available", false, "currentHolderId", studentId);
                                    batch.set(db.collection("libraryLoans").document(), loan);
                                }).addOnSuccessListener(a -> r.setValue("OK"))
                                .addOnFailureListener(e -> r.setValue("ERROR"));
                    }).addOnFailureListener(e -> r.setValue("ERROR"));
        }).addOnFailureListener(e -> r.setValue("ERROR"));
        return r;
    }

    public LiveData<List<LibraryLoan>> getMyLoans(String studentId) {
        MutableLiveData<List<LibraryLoan>> ld = new MutableLiveData<>();
        loansListener = db.collection("libraryLoans")
                .whereEqualTo("studentId", studentId)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    List<LibraryLoan> list = new ArrayList<>();
                    snap.forEach(d -> {
                        LibraryLoan l = d.toObject(LibraryLoan.class);
                        if (l != null) { l.setLoanId(d.getId()); list.add(l); }
                    });
                    Collections.sort(list, (a, b) -> Long.compare(b.getIssueDate(), a.getIssueDate()));
                    ld.setValue(list);
                });
        return ld;
    }

    public LiveData<Boolean> returnBook(String loanId, String bookId) {
        MutableLiveData<Boolean> r = new MutableLiveData<>();
        db.runBatch(batch -> {
                    batch.update(db.collection("libraryLoans").document(loanId),
                            "status", "RETURNED", "returnDate", System.currentTimeMillis());
                    // এখানেও "isAvailable" এর জায়গায় "available"
                    batch.update(db.collection("books").document(bookId),
                            "available", true, "currentHolderId", "");
                }).addOnSuccessListener(a -> r.setValue(true))
                .addOnFailureListener(e -> r.setValue(false));
        return r;
    }

    public void removeListeners() {
        if (booksListener != null) booksListener.remove();
        if (loansListener != null) loansListener.remove();
    }
}