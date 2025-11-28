package com.example.indeedgambling;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


/**
 * ViewModel for Organizer user.
 *
 * Stores the logged-in organizer's profile and the event they have selected.
 * Helps share data between fragments without passing it through fragment arguments.
 *
 * LiveData ensures UI reacts to changes automatically.
 */
public class OrganizerViewModel extends ViewModel {

    /** Holds currently logged-in organizer's profile */
    private final MutableLiveData<Profile> organizer = new MutableLiveData<>();

    /** Holds the organizer's currently selected event (for editing, viewing entrants, etc.) */
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();

    /**
     * Assign the logged-in organizer profile
     *
     * @param p organizer profile
     */
    public void setOrganizer(Profile p) {
        organizer.setValue(p);
    }
    public Profile getCurrentOrganizer(){return organizer.getValue();}

    /**
     * @return LiveData for organizer profile (UI can observe)
     */
    public MutableLiveData<Profile> getOrganizer() {
        return organizer;
    }

    /**
     * Stores the event selected by the organizer
     *
     * @param e event selected
     */
    public void setSelectedEvent(Event e) {
        selectedEvent.setValue(e);
    }

    /**
     * @return LiveData for current selected event
     */
    public MutableLiveData<Event> getSelectedEvent() {
        return selectedEvent;
    }
    /**
     * Notify the waiting list for an event
     *
     * @param eventId The ID of the event
     * @param message The notification message to send
     * @param onSuccess Success callback
     * @param onFailure Failure callback
     */

    public void notifyWaitingList(String eventId, String message,
                                  OnSuccessListener<Void> onSuccess,
                                  OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("message", message);
        notification.put("timestamp", FieldValue.serverTimestamp());
        notification.put("type", "waiting_list");
        notification.put("status", "sent");

        Log.d("FIREBASE", "Sending notification to Firestore: " + notification);
        db.collection("notifications").add(notification)
                .addOnSuccessListener(documentReference -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void notifySelectedEntrants(String eventId, String message,
                                       OnSuccessListener<Void> onSuccess,
                                       OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("message", message);
        notification.put("timestamp", FieldValue.serverTimestamp());
        notification.put("type", "selected_entrants");
        notification.put("status", "sent");

        db.collection("notifications").add(notification)
                .addOnSuccessListener(documentReference -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void notifyCancelledEntrants(String eventId, List<String> cancelledEntrants, String message,
                                        OnSuccessListener<Void> onSuccess,
                                        OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("message", message);
        notification.put("timestamp", FieldValue.serverTimestamp());
        notification.put("type", "cancelled_entrants");
        notification.put("cancelledEntrants", cancelledEntrants);
        notification.put("status", "sent");

        db.collection("notifications").add(notification)
                .addOnSuccessListener(documentReference -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }
    /**
     * Get event by ID from Firestore
     *
     * @param eventId The ID of the event to retrieve
     * @return LiveData containing the event
     */
    public MutableLiveData<Event> getEventById(String eventId) {
        MutableLiveData<Event> eventLiveData = new MutableLiveData<>();

        // TODO: Implement actual Firestore query
        // db.collection("events").document(eventId).get()
        //     .addOnSuccessListener(documentSnapshot -> {
        //         Event event = documentSnapshot.toObject(Event.class);
        //         eventLiveData.setValue(event);
        //     })
        //     .addOnFailureListener(e -> {
        //         // Handle error
        //         eventLiveData.setValue(null);
        //     });

        // For now, return empty LiveData
        return eventLiveData;
    }

    public void updateSettings(Map<String, Object> updates) {
        Profile p = organizer.getValue();
        if (p == null) return;

        for (String key : updates.keySet()) {

            if (key.equals("personName")) {
                p.setPersonName((String) updates.get(key));
            }

            if (key.equals("email")) {
                p.setEmail((String) updates.get(key));
            }

            if (key.equals("phone")) {
                p.setPhone((String) updates.get(key));
            }

            if (key.equals("notificationsEnabled")) {
                p.setNotificationsEnabled((Boolean) updates.get(key));
            }

            if (key.equals("lightMode")) {
                p.setLightMode((Boolean) updates.get(key));
            }
        }

        organizer.setValue(p);
    }

}
