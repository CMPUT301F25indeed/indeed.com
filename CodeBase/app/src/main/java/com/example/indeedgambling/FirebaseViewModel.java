package com.example.indeedgambling;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

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

    public LiveData<List<Profile>> getProfilesLive() {
        return profilesLive;
    }

    public LiveData<List<Event>> getEventsLive() {
        return eventsLive;
    }

    // -------------------------
    // Profile CRUD
    // -------------------------
    public void upsertProfile(Profile p, Runnable onOk, Consumer<Exception> onErr) {
        if (p.getProfileId() == null || p.getProfileId().isEmpty()) {
            onErr.accept(new IllegalArgumentException("profileId is required"));
            return;
        }
        PROFILES.document(p.getProfileId()).set(p)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    public void updateProfile(String profileId, Map<String, Object> updates, Runnable onOk, Consumer<Exception> onErr) {
        PROFILES.document(profileId).update(updates)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

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

    public void updateEvent(String eventId, Map<String, Object> updates, Runnable onOk, Consumer<Exception> onErr) {
        EVENTS.document(eventId).update(updates)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

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


    /**
     *
     * @param eventId Event to move entrants between lists on
     * @param entrantIds List of entrants to move
     * @param onOk
     * @param onErr
     */
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


    }

    /** Returns the waitlist for the event matching the eventID
     * onResult is the
     * @param eventID EventID to find waitlist of
     * @param onResult code to be run after success
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

    public void upsertInvitation(Invitation inv, Runnable onOk, Consumer<Exception> onErr) {
        String docId = inv.getEventId() + "_" + inv.getEntrantId();
        INVITES.document(docId).set(inv)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

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

    // -------------------------
    // Images
    // -------------------------
    public void saveImageMeta(ImageUpload img, Runnable onOk, Consumer<Exception> onErr) {
        String docId = UUID.randomUUID().toString();
        IMAGES.document(docId).set(img)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Logs
    // -------------------------
    public void writeLog(LogEntry log, Runnable onOk, Consumer<Exception> onErr) {
        String docId = UUID.randomUUID().toString();
        LOGS.document(docId).set(log)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Helpers
    // -------------------------
    public String makeProfileId(String userName, String password) {
        return HashUtil.generateId(userName, password);
    }

    public void containsById(String collection, String id, Consumer<Boolean> onResult, Consumer<Exception> onErr) {
        db.collection(collection).document(id).get()
                .addOnSuccessListener(doc -> onResult.accept(doc.exists()))
                .addOnFailureListener(onErr::accept);
    }

    // ✅ Added this method
    public FirebaseFirestore getDb() {
        return db;
    }


    // The following is chat coded cuz i dont care enough anymore good night!

    /** Adds an object (Event or Profile) both locally and to Firestore */
    private final MutableLiveData<ArrayList<Event>> Events = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ArrayList<Profile>> Profiles = new MutableLiveData<>(new ArrayList<>());

    public void Add(Object item) {
        if (item instanceof Event) {
            Event e = (Event) item;

            // Add locally
            ArrayList<Event> list = Events.getValue();
            if (list != null && !list.contains(e)) {
                list.add(e);
                Events.postValue(list);
            }

            // Add to Firestore
            EVENTS.document(e.getEventId()).set(e)
                    .addOnSuccessListener(v -> Log.d("FirebaseViewModel", "Event added: " + e.getEventName()))
                    .addOnFailureListener(err -> Log.e("FirebaseViewModel", "Error adding event", err));

        }
        else if (item instanceof Profile) {
            Profile p = (Profile) item;
            if (p.getProfileId() == null || p.getProfileId().isEmpty())
                p.setProfileId(UUID.randomUUID().toString());

            // Add locally
            ArrayList<Profile> list = Profiles.getValue();
            if (list != null && !list.contains(p)) {
                list.add(p);
                Profiles.postValue(list);
            }

            // Add to Firestore
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

}
