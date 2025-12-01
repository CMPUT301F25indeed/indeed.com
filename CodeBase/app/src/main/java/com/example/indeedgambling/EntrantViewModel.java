package com.example.indeedgambling;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class EntrantViewModel extends ViewModel {

    private Entrant currentEntrant;

    // Optional LiveData for entrant (you can keep it for other screens if needed)
    private final MutableLiveData<Entrant> entrantLiveData = new MutableLiveData<>();

    // LiveData for notifications (used by old system, but still fine to keep)
    private final MutableLiveData<List<Notification>> liveNotifications =
            new MutableLiveData<>(new ArrayList<>());

    // ---------- Entrant ----------
    public Entrant getCurrentEntrant() {
        return currentEntrant;
    }

    public LiveData<Entrant> getEntrantLiveData() {
        return entrantLiveData;
    }

    public void setEntrant(Entrant entrant) {
        this.currentEntrant = entrant;
        entrantLiveData.setValue(entrant);
    }

    // ---------- Notifications ----------
    public LiveData<List<Notification>> getNotificationsLive() {
        return liveNotifications;
    }

    /** Old listener, still usable if some old screen calls it */
    public void startNotificationListener(FirebaseViewModel fvm) {

        if (currentEntrant == null || currentEntrant.getProfileId() == null)
            return;

        String userId = currentEntrant.getProfileId();

        fvm.getDb()
                .collection("notifications")
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null)
                        return;

                    List<Notification> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Notification n = d.toObject(Notification.class);
                        if (n != null) {
                            n.setId(d.getId());   // keep Firestore doc id
                            list.add(n);
                        }
                    }

                    liveNotifications.postValue(list);
                });
    }
}