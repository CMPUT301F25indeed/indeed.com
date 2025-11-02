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
        EVENTS.whereGreaterThan("registrationEnd", now)
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
            if (e.getEventId() == null || e.getEventId().isEmpty())
                e.setEventId(UUID.randomUUID().toString());

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

        } else if (item instanceof Profile) {
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

    //Local caches
//    private MutableLiveData<ArrayList<Event>> Events;
//    private MutableLiveData<ArrayList<Profile>> Profiles;


//    public FirebaseViewModel() {
//        super();
//        Events = new MutableLiveData<>(new ArrayList<Event>());
//        Profiles = new MutableLiveData<>(new ArrayList<Profile>());
//        Sync();
//    }


    // Tej code aka chat
//    public FirebaseViewModel() {
//        super();
//
//        // Initialize both local caches
//        Events = new MutableLiveData<>(new ArrayList<Event>());
//        Profiles = new MutableLiveData<>(new ArrayList<Profile>());
//
//        // Start Firebase real-time listeners
//        attachRealtimeListeners();
//
//        // Sync local data with cloud
//        Sync();
//    }
//
//    // ---------- Add (3 overloads) ----------
//    public <T> void add(String collectionPath, String docName, T obj,
//                        Runnable onOk, java.util.function.Consumer<Exception> onErr) {
//        db.collection(collectionPath).document(docName).set(obj)
//                .addOnSuccessListener(unused -> onOk.run())
//                .addOnFailureListener(onErr::accept);
//    }
//
//    public <T> void add(String docName, T obj, Runnable onOk,
//                        java.util.function.Consumer<Exception> onErr) {
//        String collection = collectionFor(obj);
//        add(collection, docName, obj, onOk, onErr);
//    }
//
//    public <T> void add(T obj, Runnable onOk,
//                        java.util.function.Consumer<Exception> onErr) {
//        String collection = collectionFor(obj);
//        String name = nameFor(obj);
//        add(collection, name, obj, onOk, onErr);
//    }
//
//    // ---------- Delete ----------
//    public void delete(String collectionPath, String docName,
//                       Runnable onOk, java.util.function.Consumer<Exception> onErr) {
//        db.collection(collectionPath).document(docName).delete()
//                .addOnSuccessListener(unused -> onOk.run())
//                .addOnFailureListener(onErr::accept);
//    }
//
//    // ---------- Contains (by docName) ----------
//    public void contains(String collectionPath, String docName,
//                         java.util.function.Consumer<Boolean> onResult,
//                         java.util.function.Consumer<Exception> onErr) {
//        db.collection(collectionPath).document(docName).get()
//                .addOnSuccessListener(d -> onResult.accept(d.exists()))
//                .addOnFailureListener(onErr::accept);
//    }
//
//    // ---------- Load profile by hashed id ----------
////    public void loadProfileByLogin(String userName, String password,
////                                   java.util.function.Consumer<DocumentSnapshot> onDoc,
////                                   java.util.function.Consumer<Exception> onErr) {
////        String id = HashUtil.generateId(userName, password);
////        db.collection("profiles").document(id).get()
////                .addOnSuccessListener(onDoc::accept)
////                .addOnFailureListener(onErr::accept);
////    }
//
//    // helpers
//    private <T> String collectionFor(T obj) {
//        String n = obj.getClass().getSimpleName();
//        if (obj instanceof Profile || obj instanceof Entrant
//                || obj instanceof Organizer || obj instanceof Admin) return "profiles";
//        if (n.equals("Event")) return "events";
//        if (n.equals("Invitation")) return "invitations";
//        if (n.equals("AppNotification")) return "notifications";
//        return n.toLowerCase() + "s";
//    }
//
//    private <T> String nameFor(T obj) {
//        if (obj instanceof Profile) return ((Profile) obj).getProfileId();
//        if (obj instanceof Event) return ((Event) obj).getEventId();
//        // fallback random
//        return java.util.UUID.randomUUID().toString();
//    }
//
//    //Xans code
////
////    /**
////     * Sync local data with Firebase
////     * Overwrites local information.
////     */
////    public void Sync() {
////        //Overwriting local with cloud
////
////        //Events Syncing
////        db.collection("Event").get().addOnCompleteListener(task -> {
////            //Replace local with cloud
////            ArrayList<Event> ResultArray = (ArrayList<Event>) task.getResult().toObjects(Event.class);
////
////            //Add difference to cloud.
////            //If cloud result has all local, do nothing
////
////            //If the cloud is missing items, push
////            if (!ResultArray.containsAll(Objects.requireNonNull(Events.getValue()))) {
//                //Push all non-existants to cloud.
//                for (Event e : Events.getValue()) {
//                    if (!ResultArray.contains(e)) {
//                        this.Add(e);
//                    }
//                }
//            }
//            //Updating local to cloud
//            else {
//                Events.setValue(ResultArray);
//            }
//        });
//        //Profile sync
//        db.collection("Profile").get().addOnCompleteListener(task -> {
//            Profiles.setValue((ArrayList<Profile>) task.getResult().toObjects(Profile.class));
//        });
//    }
//
//    /**
//     * Deletes an object from the firebase
//     *
//     * @param CollectionPath ex: event, profile, notification, etc.
//     * @param InstanceName   Name of the object to delete
//     */
//    public void Delete(String CollectionPath, String InstanceName) {
//        db.collection(CollectionPath).document(InstanceName);
//    }
//
//    /**
//     * Deletes an object from the firebase, path found automatically
//     * Deletes an object locally
//     *
//     * @param ItemToDelete Object deleted from firebase
//     */
//    public void Delete(Object ItemToDelete) {
//        db.collection(ItemToDelete.getClass().getSimpleName()).document(ItemToDelete.toString()).delete();
//
//        //Remove from local
//        if (ItemToDelete.getClass() == Event.class) {
//            Log.d("DEBUG Delete", Boolean.toString(Objects.requireNonNull(Events.getValue()).remove((Event) ItemToDelete)));
//        } else if (ItemToDelete.getClass() == Profile.class) {
//            Objects.requireNonNull(Profiles.getValue()).remove((Profile) ItemToDelete);
//        }
//    }
//
//
//    /**
//     * Saves the object under the document with the Class name
//     *
//     * @param InstanceName Save Name of the object
//     * @param ItemToSave   Object to be saved
//     */
//    public void Add(String InstanceName, Object ItemToSave) {
//        db.collection(ItemToSave.getClass().getSimpleName()).document(InstanceName).set(ItemToSave);
//        //Adding to local
//        if (ItemToSave.getClass() == Event.class) {
//            Objects.requireNonNull(Events.getValue()).add((Event) ItemToSave);
//        } else if (ItemToSave.getClass() == Profile.class) {
//            Objects.requireNonNull(Profiles.getValue()).add((Profile) ItemToSave);
//        }
//    }
//
//    /**
//     * Saves object to server using Object's name and Class Name.
//     *
//     * @param ItemToSave
//     */
//    public void Add(Object ItemToSave) {
//
//        //Event object
//        if (ItemToSave.getClass() == Event.class) {
//            //Adding to local for instant change
//            Objects.requireNonNull(Events.getValue()).add((Event) ItemToSave);
//            //Adding to server
//            db.collection(ItemToSave.getClass().getSimpleName()).document(ItemToSave.toString()).set(ItemToSave);
//        }
//        //Profile object
//        else if (ItemToSave.getClass() == Profile.class) {
//            //Adding to local for instant change
//            Objects.requireNonNull(Profiles.getValue()).add((Profile) ItemToSave);
//            //Adding to server
//            db.collection(ItemToSave.getClass().getSimpleName()).document(String.valueOf(ItemToSave.hashCode())).set(ItemToSave);
//        }
//    }
//
//    /**
//     * Checks if the LocalCache contains the object
//     *
//     * @param ItemToCheck Object checking
//     * @return True if object held, False otherwise. Returns FALSE if object not handled
//     */
//    public Boolean Contains(Object ItemToCheck) {
//        //Checking Local
//        //Will need to test some more
//        if (ItemToCheck.getClass() == Event.class) {
//            return Objects.requireNonNull(Events.getValue()).contains((Event) ItemToCheck);
//        } else if (ItemToCheck.getClass() == Profile.class) {
//            return Objects.requireNonNull(Profiles.getValue()).contains((Profile) ItemToCheck);
//        }
//        //Catch all. If we don't have a matching type, we definitely don't have it saved.
//        return false;
//    }
//
//    /**
//     * Returns the local cache of Events, from last sync
//     *
//     * @return ArrayList of Events, sorted
//     */
//    public ArrayList<Event> getEvents() {
//        Objects.requireNonNull(Events.getValue()).sort((Event1, Event2) -> Event1.getRegistrationEnd().compareTo(Event2.getRegistrationEnd()));
//        return Events.getValue();
//    }
//
//    public ArrayList<Profile> getProfiles() {
//        return Profiles.getValue();
//    }
//
//    /**
//     * Returns all profiles that match the hash
//     * May only return 1 profile since duplicate names are not supported in Firebase
//     *
//     * @return
//     */
//    private ArrayList<Profile> getMatchingProfiles(Profile profile) {
//        ArrayList<Profile> ReturnArray = new ArrayList<>();
//        for (Profile p : Profiles.getValue()) {
//            if (p.hashCode() == profile.hashCode()) ReturnArray.add(p);
//        }
//        return ReturnArray;
//    }
//
//    /**
//     * Returns an ArrayList for events that are currently open to register and are not full on waiting Entrants.
//     *
//     * @return Events that can take registration to Waitlist.
//     */
//    private ArrayList<Event> getRegisterableEvents() {
//        ArrayList<Event> ReturnArray = new ArrayList<>();
//        for (Event e : Events.getValue()) {
//            if (e.waitList_registrable()) {
//                ReturnArray.add(e);
//            }
//        }
//
//        return ReturnArray;
//    }
