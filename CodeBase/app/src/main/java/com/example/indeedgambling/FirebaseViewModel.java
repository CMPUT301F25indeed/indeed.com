package com.example.indeedgambling;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import androidx.annotation.Nullable;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Firebase ViewModel responsible for handling all Firestore operations
 * including profiles, events, invitations, notifications, images, and logs.
 *
 * It also provides LiveData streams for real-time UI updates and helper
 * methods for CRUD operations and waiting list functionality.
 */
public class FirebaseViewModel extends ViewModel {

    // ---- Firestore ----
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference PROFILES = db.collection("profiles");
    private final CollectionReference EVENTS = db.collection("events");
    private final CollectionReference INVITES = db.collection("invitations");
    private final CollectionReference NOTIFS = db.collection("notifications");
    private final CollectionReference IMAGES = db.collection("images");
    private final CollectionReference LOGS = db.collection("logs");

    // ---- Live caches ----
    private final MutableLiveData<List<Profile>> profilesLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Event>> eventsLive = new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration profilesReg;
    private ListenerRegistration eventsReg;

    /**
     * Constructor: attaches real-time listeners to Firestore on init
     */
    public FirebaseViewModel() {
        attachRealtimeListeners();
    }

    @Override
    protected void onCleared() {
        if (profilesReg != null) profilesReg.remove();
        if (eventsReg != null) eventsReg.remove();
        super.onCleared();
    }

    // -------------------------
    // Realtime listeners
    // -------------------------

    /**
     * Subscribes to Firestore changes for profiles and events
     * and updates LiveData automatically.
     */
    private void attachRealtimeListeners() {
        profilesReg = PROFILES.addSnapshotListener((snap, err) -> {
            if (err != null || snap == null) return;
            List<Profile> list = snap.toObjects(Profile.class);
            profilesLive.postValue(list);
        });

        eventsReg = EVENTS.addSnapshotListener((snap, err) -> {
            if (err != null || snap == null) return;
            List<Event> list = snap.toObjects(Event.class);
            eventsLive.postValue(list);
        });
    }

    /**
     * @return LiveData list of profiles updated in real time
     */
    public LiveData<List<Profile>> getProfilesLive() {
        return profilesLive;
    }

    /**
     * @return LiveData list of events updated in real time
     */
    public LiveData<List<Event>> getEventsLive() {
        return eventsLive;
    }

    // -------------------------
    // Profile CRUD
    // -------------------------

    /**
     * Creates or updates a profile in Firestore.
     *
     * @param p Profile object
     * @param onOk callback if success
     * @param onErr callback if failure
     */
    public void upsertProfile(Profile p, Runnable onOk, Consumer<Exception> onErr) {
        if (p.getProfileId() == null || p.getProfileId().isEmpty()) {
            onErr.accept(new IllegalArgumentException("profileId is required"));
            return;
        }
        PROFILES.document(p.getProfileId()).set(p)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Updates specific fields of a profile in Firestore.
     *
     * @param profileId ID of profile
     * @param updates fields to update
     */
    public void updateProfile(String profileId, Map<String, Object> updates, Runnable onOk, Consumer<Exception> onErr) {
        PROFILES.document(profileId).update(updates)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Loads a profile based on email+password hash
     */
    public void loadProfileByLogin(String userName, String password,
                                   Consumer<DocumentSnapshot> onDoc, Consumer<Exception> onErr) {
        String id = HashUtil.generateId(userName, password);
        PROFILES.document(id).get()
                .addOnSuccessListener(onDoc::accept)
                .addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Events
    // -------------------------

    /**
     * Creates a new event in Firestore.
     */
    public void createEvent(Event e, Runnable onOk, Consumer<Exception> onErr) {
        if (e.getEventId() == null || e.getEventId().isEmpty()) {
            e.setEventId(UUID.randomUUID().toString());
        }
        if (e.getEventName() == null) e.setEventName("");
        if (e.getStatus() == null) e.setStatus("open");
        if (e.getWaitingList() == null) e.setWaitingList(new ArrayList<>());

        EVENTS.document(e.getEventId()).set(e)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Update event details
     */
    public void updateEvent(String eventId, Map<String, Object> updates, Runnable onOk, Consumer<Exception> onErr) {
        EVENTS.document(eventId).update(updates)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Fetch open events where registration is still active
     */
    public void fetchOpenEvents(Consumer<List<Event>> onResult, Consumer<Exception> onErr) {
        Date now = new Date();
        EVENTS.whereGreaterThan("registrationStart", now)
                .orderBy("registrationEnd", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(q -> onResult.accept(q.toObjects(Event.class)))
                .addOnFailureListener(onErr::accept);
    }

    public void fetchOrgsEvents(String OrgID, Consumer<List<Event>> onResult, Consumer<Exception> onErr){
        EVENTS.whereEqualTo("organizerId",OrgID)
                .orderBy("registrationEnd", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(q -> onResult.accept(q.toObjects(Event.class)))
                .addOnFailureListener(onErr::accept);
    }

    public void fetchOrgsUpcomingEvents(String OrgID, Consumer<List<Event>> onResult, Consumer<Exception> onErr){
        EVENTS.whereEqualTo("organizerId",OrgID).whereGreaterThan("eventEnd",new Date())
                .orderBy("registrationEnd", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(q -> onResult.accept(q.toObjects(Event.class)))
                .addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Waiting List
    // -------------------------
    public void joinWaitingList(String eventId, String entrantId, Runnable onOk, Consumer<Exception> onErr) {
        Map<String, Object> u = new HashMap<>();
        u.put("waitingList", FieldValue.arrayUnion(entrantId));
        EVENTS.document(eventId).update(u)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    public void leaveWaitingList(String eventId, String entrantId, Runnable onOk, Consumer<Exception> onErr) {
        Map<String, Object> u = new HashMap<>();
        u.put("waitingList", FieldValue.arrayRemove(entrantId));
        EVENTS.document(eventId).update(u)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

/*
    /**
     *
     * @param eventId Event to move entrants between lists on
     * @param entrantIds List of entrants to move
     * @param onOk
     * @param onErr
     *//*
    public void InviteEntrants(String eventId, List<String> entrantIds, Runnable onOk, Consumer<Exception> onErr) {
        for (String ID : entrantIds){
            Map<String, Object> removeFromWaiting = new HashMap<>();
            removeFromWaiting.put("waitingList", FieldValue.arrayRemove(ID));

            //Only adds to invitelist if removal works
            EVENTS.document(eventId).update(removeFromWaiting)
                    .addOnSuccessListener(v -> {
                        //Add to invited list after removal succeeds
                        Map<String,Object> addToInvited = new HashMap<>();
                        addToInvited.put("invitedList", FieldValue.arrayUnion(ID));

                        EVENTS.document(eventId).update(addToInvited)
                                .addOnSuccessListener(v2 -> onOk.run())
                                .addOnFailureListener(onErr::accept);
                    })
                    .addOnFailureListener(onErr::accept);
        }


    }*/

    /** Returns the waitlist for the event matching the eventID
     * onResult is the
     * @param eventID EventID to find waitlist of
     * @param onResult code to be run after success with the Profile Data.
     * @param onErr action on failure to find data
     */
    public void getEventWaitlist(String eventID, Consumer<List<Profile>> onResult, Consumer<Exception> onErr){
        //Get the Profile IDs from the events waitlist
        // Get the names from the profiles with those ids

        //Gets matching event
        EVENTS.document(eventID).get().addOnSuccessListener(e ->{
            //Getting Profiles saved under event waitlist
            List<String> result = e.toObject(Event.class).getWaitingList();
            if (!result.isEmpty()){
                PROFILES.whereIn("profileId",result)
                        .orderBy("personName")
                        .get()
                        .addOnSuccessListener(p -> {onResult.accept(p.toObjects(Profile.class));})
                        .addOnFailureListener(onErr::accept);
            }
        }).addOnFailureListener(onErr::accept);
    }

    /** Returns the
     *
     * @param eventID
     * @param onResult
     * @param onErr
     */
    public void getEventInvitedList(String eventID, Consumer<List<Profile>> onResult, Consumer<Exception> onErr){
        //Get the Profile IDs from the events waitlist
        // Get the names from the profiles with those ids

        //Gets matching event
        EVENTS.document(eventID).get().addOnSuccessListener(e ->{
            //Getting Profiles saved under event waitlist
            List<String> result = e.toObject(Event.class).getInvitedList();
            if (!result.isEmpty()){
                PROFILES.whereIn("profileId",result)
                        .orderBy("personName")
                        .get()
                        .addOnSuccessListener(p -> {onResult.accept(p.toObjects(Profile.class));})
                        .addOnFailureListener(onErr::accept);
            }
        }).addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Invitations
    // -------------------------

    /**
     * Create or update invitation for entrant
     */
    public void upsertInvitation(Invitation inv, Runnable onOk, Consumer<Exception> onErr) {
        String docId = inv.getEventId() + "_" + inv.getEntrantId();
        INVITES.document(docId).set(inv)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Update invitation (accept/decline etc)
     */
    public void updateInvitationStatus(String eventId, String entrantId, String status,
                                       boolean responded, Date updatedAt,
                                       Runnable onOk, Consumer<Exception> onErr) {
        String docId = eventId + "_" + entrantId;
        Map<String, Object> u = new HashMap<>();
        u.put("status", status);
        u.put("responded", responded);
        u.put("updatedAt", updatedAt);
        INVITES.document(docId).update(u)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Notifications
    // -------------------------

    /**
     * Sends an in-app notification to user
     */
    public void sendNotification(Notification n, Runnable onOk, Consumer<Exception> onErr) {
        String docId = UUID.randomUUID().toString();
        Map<String, Object> map = new HashMap<>();
        map.put("senderId", n.getSenderId());
        map.put("receiverId", n.getReceiverId());
        map.put("eventId", n.getEventId());
        map.put("type", n.getType());
        map.put("message", n.getMessage());
        map.put("timestamp", n.getTimestamp() != null ? n.getTimestamp() : new Date());
        NOTIFS.document(docId).set(map)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }
    /**
     * Notify all entrants on waiting list (US 02.07.01)
     */
    public void notifyWaitingList(String eventId, String message, Runnable onOk, Consumer<Exception> onErr) {
        EVENTS.document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            Event event = documentSnapshot.toObject(Event.class);
            if (event != null && event.getWaitingList() != null && !event.getWaitingList().isEmpty()) {
                AtomicInteger completedCount = new AtomicInteger(0);
                int totalEntrants = event.getWaitingList().size();

                for (String entrantId : event.getWaitingList()) {
                    Notification notification = new Notification();
                    notification.setSenderId("system");
                    notification.setReceiverId(entrantId);
                    notification.setEventId(eventId);
                    notification.setType("waiting_list_update");
                    notification.setMessage(message);
                    notification.setTimestamp(new Date());

                    sendNotification(notification,
                            () -> {
                                if (completedCount.incrementAndGet() == totalEntrants) {
                                    onOk.run();
                                }
                            },
                            onErr
                    );
                }
            } else {
                onOk.run(); // No waiting list entrants is not an error
            }
        }).addOnFailureListener(onErr::accept);
    }

    /**
     * US 02.07.02 - Notify all selected entrants
     */
    public void notifySelectedEntrants(String eventId, String message, Runnable onOk, Consumer<Exception> onErr) {
        EVENTS.document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            Event event = documentSnapshot.toObject(Event.class);
            if (event != null && event.getInvitedList() != null && !event.getInvitedList().isEmpty()) {
                AtomicInteger completedCount = new AtomicInteger(0);
                int totalEntrants = event.getInvitedList().size();

                for (String entrantId : event.getInvitedList()) {
                    Notification notification = new Notification();
                    notification.setSenderId("system");
                    notification.setReceiverId(entrantId);
                    notification.setEventId(eventId);
                    notification.setType("selection_notice");
                    notification.setMessage("Congratulations! You've been selected for: " + message);
                    notification.setTimestamp(new Date());

                    sendNotification(notification,
                            () -> {
                                if (completedCount.incrementAndGet() == totalEntrants) {
                                    onOk.run();
                                }
                            },
                            onErr
                    );
                }
            } else {
                onOk.run(); // No selected entrants is not an error
            }
        }).addOnFailureListener(onErr::accept);
    }

    /**
     * US 02.07.03 - Notify all cancelled entrants
     */
    public void notifyCancelledEntrants(String eventId, List<String> cancelledEntrantIds, String message,
                                        Runnable onOk, Consumer<Exception> onErr) {
        if (cancelledEntrantIds == null || cancelledEntrantIds.isEmpty()) {
            onOk.run();
            return;
        }

        AtomicInteger completedCount = new AtomicInteger(0);
        int totalEntrants = cancelledEntrantIds.size();

        for (String entrantId : cancelledEntrantIds) {
            Notification notification = new Notification();
            notification.setSenderId("system");
            notification.setReceiverId(entrantId);
            notification.setEventId(eventId);
            notification.setType("cancellation_notice");
            notification.setMessage("Update: " + message);
            notification.setTimestamp(new Date());

            sendNotification(notification,
                    () -> {
                        if (completedCount.incrementAndGet() == totalEntrants) {
                            onOk.run();
                        }
                    },
                    onErr
            );
        }
    }
    // -------------------------
    // Images
    // -------------------------

    /**
     * Stores metadata for uploaded image
     */
    public void saveImageMeta(ImageUpload img, Runnable onOk, Consumer<Exception> onErr) {
        String docId = UUID.randomUUID().toString();
        IMAGES.document(docId).set(img)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Logs
    // -------------------------

    /**
     * Saves logs of system actions (admin viewable)
     */
    public void writeLog(LogEntry log, Runnable onOk, Consumer<Exception> onErr) {
        String docId = UUID.randomUUID().toString();
        LOGS.document(docId).set(log)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Helpers
    // -------------------------

    /**
     * @return hashed profile ID generated from email + password
     */
    public String makeProfileId(String userName, String password) {
        return HashUtil.generateId(userName, password);
    }

    /**
     * Checks if a doc exists in a Firestore collection
     */
    public void containsById(String collection, String id, Consumer<Boolean> onResult, Consumer<Exception> onErr) {
        db.collection(collection).document(id).get()
                .addOnSuccessListener(doc -> onResult.accept(doc.exists()))
                .addOnFailureListener(onErr::accept);
    }

    /**
     * @return Firebase DB instance
     */
    public FirebaseFirestore getDb() {
        return db;
    }


    // ======================= LOCAL CACHE SECTION (TEAM CODE BELOW) =======================

    /** Adds an object (Event or Profile) both locally and to Firestore */
    private final MutableLiveData<ArrayList<Event>> Events = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ArrayList<Profile>> Profiles = new MutableLiveData<>(new ArrayList<>());

    /**
     * Adds event or profile to local cache and Firestore
     */
    public void Add(Object item) {
        if (item instanceof Event) {
            Event e = (Event) item;
            if (e.getEventId() == null || e.getEventId().isEmpty())
                e.setEventId(UUID.randomUUID().toString());

            ArrayList<Event> list = Events.getValue();
            if (list != null && !list.contains(e)) {
                list.add(e);
                Events.postValue(list);
            }

            EVENTS.document(e.getEventId()).set(e)
                    .addOnSuccessListener(v -> Log.d("FirebaseViewModel", "Event added: " + e.getEventName()))
                    .addOnFailureListener(err -> Log.e("FirebaseViewModel", "Error adding event", err));

        }
        else if (item instanceof Profile) {
            Profile p = (Profile) item;
            if (p.getProfileId() == null || p.getProfileId().isEmpty())
                p.setProfileId(UUID.randomUUID().toString());

            ArrayList<Profile> list = Profiles.getValue();
            if (list != null && !list.contains(p)) {
                list.add(p);
                Profiles.postValue(list);
            }

            PROFILES.document(p.getProfileId()).set(p)
                    .addOnSuccessListener(v -> Log.d("FirebaseViewModel", "Profile added: " + p.getProfileId()))
                    .addOnFailureListener(err -> Log.e("FirebaseViewModel", "Error adding profile", err));
        }
    }

    /** Checks if an Event or Profile exists in the local cache */
    public Boolean Contains(Object item) {
        if (item instanceof Event) {
            ArrayList<Event> list = Events.getValue();
            return list != null && list.contains(item);
        } else if (item instanceof Profile) {
            ArrayList<Profile> list = Profiles.getValue();
            return list != null && list.contains(item);
        }
        return false;
    }

    /** Returns the locally cached events, sorted by registration end date */
    public ArrayList<Event> getEvents() {
        ArrayList<Event> list = Events.getValue();
        if (list == null) return new ArrayList<>();
        list.sort((e1, e2) -> e1.getRegistrationEnd().compareTo(e2.getRegistrationEnd()));
        return list;
    }

    public void signUpForEvent(String eventId, String entrantId, Runnable onSuccess, Consumer<Exception> onFailure) {
        db.collection("events").document(eventId)
                .update("participants", FieldValue.arrayUnion(entrantId))
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }

}
