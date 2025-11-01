package com.example.indeedgambling;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.*;

public class FirebaseViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ---------- Add (3 overloads) ----------
    public <T> void add(String collectionPath, String docName, T obj,
                        Runnable onOk, java.util.function.Consumer<Exception> onErr) {
        db.collection(collectionPath).document(docName).set(obj)
                .addOnSuccessListener(unused -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    public <T> void add(String docName, T obj, Runnable onOk,
                        java.util.function.Consumer<Exception> onErr) {
        String collection = collectionFor(obj);
        add(collection, docName, obj, onOk, onErr);
    }

    public <T> void add(T obj, Runnable onOk,
                        java.util.function.Consumer<Exception> onErr) {
        String collection = collectionFor(obj);
        String name = nameFor(obj);
        add(collection, name, obj, onOk, onErr);
    }

    // ---------- Delete ----------
    public void delete(String collectionPath, String docName,
                       Runnable onOk, java.util.function.Consumer<Exception> onErr) {
        db.collection(collectionPath).document(docName).delete()
                .addOnSuccessListener(unused -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    // ---------- Contains (by docName) ----------
    public void contains(String collectionPath, String docName,
                         java.util.function.Consumer<Boolean> onResult,
                         java.util.function.Consumer<Exception> onErr) {
        db.collection(collectionPath).document(docName).get()
                .addOnSuccessListener(d -> onResult.accept(d.exists()))
                .addOnFailureListener(onErr::accept);
    }

    // ---------- Load profile by hashed id ----------
    public void loadProfileByLogin(String userName, String password,
                                   java.util.function.Consumer<DocumentSnapshot> onDoc,
                                   java.util.function.Consumer<Exception> onErr) {
        String id = HashUtil.generateId(userName, password);
        db.collection("profiles").document(id).get()
                .addOnSuccessListener(onDoc::accept)
                .addOnFailureListener(onErr::accept);
    }

    // helpers
    private <T> String collectionFor(T obj) {
        String n = obj.getClass().getSimpleName();
        if (obj instanceof Profile || obj instanceof Entrant
                || obj instanceof Organizer || obj instanceof Admin) return "profiles";
        if (n.equals("Event")) return "events";
        if (n.equals("Invitation")) return "invitations";
        if (n.equals("AppNotification")) return "notifications";
        return n.toLowerCase() + "s";
    }

    private <T> String nameFor(T obj) {
        if (obj instanceof Profile) return ((Profile) obj).getProfileId();
        if (obj instanceof Event) return ((Event) obj).getEventId();
        // fallback random
        return java.util.UUID.randomUUID().toString();
    }
}
