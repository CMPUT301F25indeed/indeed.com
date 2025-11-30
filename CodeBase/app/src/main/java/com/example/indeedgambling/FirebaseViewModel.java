package com.example.indeedgambling;




import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.Set;


import androidx.annotation.Nullable;

import android.app.Application;
import android.util.Log;
import android.widget.TextView;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.*;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 */
public class FirebaseViewModel extends ViewModel {

    private final com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
    private final com.google.firebase.storage.StorageReference storageRef = storage.getReference();


    // ---- Firestore ----
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference PROFILES = db.collection("profiles");
    private final CollectionReference EVENTS = db.collection("events");
    private final CollectionReference INVITES = db.collection("invitations");
    private final CollectionReference NOTIFS = db.collection("notifications");
    private final CollectionReference IMAGES = db.collection("images");
    private final CollectionReference LOGS = db.collection("logs");

    private final MutableLiveData<List<Profile>> profilesLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Event>> eventsLive = new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration profilesReg;
    private ListenerRegistration eventsReg;

    /**
     *
     */
    public FirebaseViewModel() {
        attachRealtimeListeners();
    }

    @Override
    protected void onCleared() {
        if (profilesReg != null) {
            profilesReg.remove();
        }
        if (eventsReg != null) {
            eventsReg.remove();
        }
        super.onCleared();
    }

    /**
     *
     */
    private void attachRealtimeListeners() {
        profilesReg = PROFILES.addSnapshotListener((snap, err) -> {
            if (err != null || snap == null) {
                return;
            }
            List<Profile> list = snap.toObjects(Profile.class);
            profilesLive.postValue(list);
        });

        eventsReg = EVENTS.addSnapshotListener((snap, err) -> {
            if (err != null || snap == null) {
                return;
            }
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
     * Finds a user profile in Firestore by matching the stored deviceId.
     *
     * Supports device-based automatic login by checking whether this device
     * is linked to any existing profile.
     *
     * Returns:
     * - Matching Profile object if found
     * - null if no linked deviceId exists
     */
    public void findProfileByDeviceId(String deviceId,
                                      Consumer<Profile> onResult,
                                      Consumer<Exception> onError) {

        PROFILES.whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        onResult.accept(null);
                    } else {
                        Profile p = q.getDocuments().get(0).toObject(Profile.class);
                        onResult.accept(p);
                    }
                })
                .addOnFailureListener(onError::accept);
    }


    /**
     * Creates or updates a profile in Firestore.
     *
     * @param p     Profile object
     * @param onOk  callback if success
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
     * Checks if a given email already exists in the Profiles collection.
     *
     * @param email    The email to check.
     * @param onResult Callback invoked with true if the email exists, false otherwise.
     * @param onErr    Callback invoked if an error occurs while querying Firestore.
     */
    public void checkEmailExists(String email, Consumer<Boolean> onResult, Consumer<Exception> onErr) {
        PROFILES.whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean exists = !querySnapshot.isEmpty();
                    onResult.accept(exists);
                })
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Creates or updates an entrant in Firestore.
     *
     * @param e     entrant object
     * @param onOk  callback if success
     * @param onErr callback if failure
     */
    public void upsertEntrant(Entrant e, Runnable onOk, Consumer<Exception> onErr) {
        if (e.getProfileId() == null || e.getProfileId().isEmpty()) {
            onErr.accept(new IllegalArgumentException("profileId is required"));
            return;
        }
        PROFILES.document(e.getProfileId()).set(e)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Updates specific fields of a profile in Firestore.
     *
     * @param profileId ID of profile
     * @param updates   fields to update
     */
    /**
     * Updates fields of a profile document in Firestore.
     *
     * @param profileId ID of the profile to update
     * @param updates   Key–value pairs to update in Firestore
     * @param onSuccess Callback executed on success
     * @param onError   Callback executed on failure
     */
    public void updateProfile(String profileId,
                              Map<String, Object> updates,
                              Runnable onSuccess,
                              java.util.function.Consumer<Exception> onError) {

        if (profileId == null || updates == null) {
            if (onError != null) {
                onError.accept(new Exception("Null arguments"));
            }
            return;
        }

        db.collection("profiles")
                .document(profileId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(e -> {
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
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
        if (e.getEventName() == null) {
            e.setEventName("");
        }
        if (e.getStatus() == null) {
            e.setStatus("open");
        }
        if (e.getWaitingList() == null) {
            e.setWaitingList(new ArrayList<>());
        }

        EVENTS.document(e.getEventId()).set(e)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Fetches an event from Firestore by its unique event ID.
     */
    public void getEventById(String eventId, Consumer<Event> onSuccess, Consumer<Exception> onError) {
        EVENTS.document(eventId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Event event = document.toObject(Event.class);
                        onSuccess.accept(event);
                    } else {
                        onError.accept(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(onError::accept);
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

    public void fetchOrgsEvents(String OrgID, Consumer<List<Event>> onResult, Consumer<Exception> onErr) {
        EVENTS.whereEqualTo("organizerId", OrgID)
                .orderBy("registrationEnd", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(q -> onResult.accept(q.toObjects(Event.class)))
                .addOnFailureListener(onErr::accept);
    }

    public void fetchOrgsUpcomingEvents(String OrgID, Consumer<List<Event>> onResult, Consumer<Exception> onErr) {
        EVENTS.whereEqualTo("organizerId", OrgID).whereGreaterThan("eventEnd", new Date())
                .orderBy("registrationEnd", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(q -> onResult.accept(q.toObjects(Event.class)))
                .addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Entrant history
    // -------------------------

    /**
     *
     */
    public void fetchEntrantHistory(String entrantId,
                                    Consumer<List<Event>> onResult,
                                    Consumer<Exception> onErr) {

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        tasks.add(EVENTS.whereArrayContains("waitingList", entrantId).get());
        tasks.add(EVENTS.whereArrayContains("invitedList", entrantId).get());
        tasks.add(EVENTS.whereArrayContains("acceptedEntrants", entrantId).get());
        tasks.add(EVENTS.whereArrayContains("cancelledEntrants", entrantId).get());

        Tasks.whenAllComplete(tasks)
                .addOnSuccessListener(list -> {
                    Map<String, Event> merged = new HashMap<>();
                    for (Task<QuerySnapshot> t : tasks) {
                        if (!t.isSuccessful()) {
                            continue;
                        }
                        QuerySnapshot qs = t.getResult();
                        if (qs == null) {
                            continue;
                        }
                        for (DocumentSnapshot doc : qs.getDocuments()) {
                            Event e = doc.toObject(Event.class);
                            if (e != null && e.getEventId() != null) {
                                merged.put(e.getEventId(), e);
                            }
                        }
                    }
                    onResult.accept(new ArrayList<>(merged.values()));
                })
                .addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Waiting List
    // -------------------------

    /**
     * Attempts to add the entrant to the waitinglist for the event.
     * Does not add the entrant if there is not room.
     *
     * @param eventId   Event to attempt to add to
     * @param entrantId Entrant to try to add
     * @param onOk      What to be done on success
     * @param onErr     What to be done on failure
     */
    public void joinWaitingList(String eventId, String entrantId, Runnable onOk, Consumer<Exception> onErr) {
        //Does not allow signup if past set limit: US: 02.03.01
        EVENTS.document(eventId).get().addOnSuccessListener(e -> {
            Event event = e.toObject(Event.class);
            if (event == null) {
                onErr.accept(new Exception("Event not found"));
                return;
            }
            if (!event.atCapacity()) {
                Map<String, Object> u = new HashMap<>();
                u.put("waitingList", FieldValue.arrayUnion(entrantId));
                EVENTS.document(eventId).update(u)
                        .addOnSuccessListener(v -> onOk.run())
                        .addOnFailureListener(onErr::accept);
            } else {
                onErr.accept(new Exception("Waitlist is at capacity!"));
            }
        }).addOnFailureListener(onErr::accept);
    }

    public void leaveWaitingList(String eventId, String entrantId, Runnable onOk, Consumer<Exception> onErr) {
        Map<String, Object> u = new HashMap<>();
        u.put("waitingList", FieldValue.arrayRemove(entrantId));
        EVENTS.document(eventId).update(u)
                .addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Returns the waitlist for the event matching the eventID
     *
     * @param eventID  EventID to find waitlist of
     * @param onResult code to be run after success with the Profile Data.
     * @param onErr    action on failure to find data
     */
    public void getEventWaitlist(String eventID, Consumer<List<Profile>> onResult, Consumer<Exception> onErr) {
        //Get the Profile IDs from the events waitlist
        // Get the names from the profiles with those ids

        //Gets matching event
        EVENTS.document(eventID).get().addOnSuccessListener(e -> {
            Event event = e.toObject(Event.class);
            if (event == null) {
                onErr.accept(new Exception("Event not found"));
                return;
            }
            List<String> result = event.getWaitingList();
            if (result != null && !result.isEmpty()) {
                PROFILES.whereIn("profileId", result)
                        .orderBy("personName")
                        .get()
                        .addOnSuccessListener(p -> onResult.accept(p.toObjects(Profile.class)))
                        .addOnFailureListener(onErr::accept);
            } else {
                onResult.accept(new ArrayList<>());
            }
        }).addOnFailureListener(onErr::accept);
    }

    /** Gets the profile objects for all the passed IDs
     *
     * @param ProfileIDs IDs of the profiles we want
     * @param onResult What to do on successful retrieval
     * @param onErr What to do on a failed query
     */
    public void getProfiles(List<String> ProfileIDs, Consumer<List<Profile>> onResult, Consumer<Exception> onErr){
        if (ProfileIDs.isEmpty()){
            onResult.accept(new ArrayList<>());
            return;
        }

        //Create phoney thing to return.
        new Thread(()->{PROFILES.whereIn("profileId",ProfileIDs)
                .orderBy("personName")
                .get()
                .addOnSuccessListener(p->{
                    onResult.accept(p.toObjects(Profile.class));
                })
                .addOnFailureListener(onErr::accept);}).start();
    }

    /**
     * Returns the Entrant objects of the invitedList from the ID for the event.
     *
     * @param eventID  Event whose invitedList is being used
     * @param onResult Action to take on success
     * @param onErr    Action to take on failure
     */
    public void getEventInvitedList(String eventID, Consumer<List<Profile>> onResult, Consumer<Exception> onErr) {
        //Get the Profile IDs from the events waitlist
        // Get the names from the profiles with those ids

        //Gets matching event
        EVENTS.document(eventID).get().addOnSuccessListener(e -> {
            Event event = e.toObject(Event.class);
            if (event == null) {
                onErr.accept(new Exception("Event not found"));
                return;
            }
            List<String> result = event.getInvitedList();
            if (result != null && !result.isEmpty()) {
                PROFILES.whereIn("profileId", result)
                        .orderBy("personName")
                        .get()
                        .addOnSuccessListener(p -> onResult.accept(p.toObjects(Profile.class)))
                        .addOnFailureListener(onErr::accept);
            } else {
                onResult.accept(new ArrayList<>());
            }
        }).addOnFailureListener(onErr::accept);
    }

    // -------------------------
    // Invitations / accept
    // -------------------------
    public void getInvitationStatus(String eventId,
                                    String entrantId,
                                    Consumer<String> onResult,
                                    Consumer<Exception> onErr) {

        String docId = eventId + "_" + entrantId;

        INVITES.document(docId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        onResult.accept("none");
                        return;
                    }
                    String status = doc.getString("status");
                    if (status == null) status = "none";
                    onResult.accept(status);
                })
                .addOnFailureListener(onErr::accept);
    }


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

    /**
     * Entrant accepts invitation.
     * Moves id from invitedList to acceptedEntrants,
     * updates Invitation doc, and sends a lottery-win notification.
     */
    public void acceptInvitation(String eventId,
                                 String entrantId,
                                 Runnable onOk,
                                 Consumer<Exception> onErr) {

        EVENTS.document(eventId).get().addOnSuccessListener(doc -> {
            Event event = doc.toObject(Event.class);
            if (event == null) {
                onErr.accept(new Exception("Event not found"));
                return;
            }

            WriteBatch batch = db.batch();

            DocumentReference eventRef = EVENTS.document(eventId);
            DocumentReference inviteRef = INVITES.document(eventId + "_" + entrantId);

            batch.update(eventRef, "invitedList", FieldValue.arrayRemove(entrantId));
            batch.update(eventRef, "acceptedEntrants", FieldValue.arrayUnion(entrantId));

            Map<String, Object> inviteUpdates = new HashMap<>();
            inviteUpdates.put("eventId", eventId);
            inviteUpdates.put("entrantId", entrantId);
            inviteUpdates.put("status", "accepted");
            inviteUpdates.put("responded", true);
            inviteUpdates.put("updatedAt", new Date());

            batch.set(inviteRef, inviteUpdates, SetOptions.merge());

            batch.commit()
                    .addOnSuccessListener(v ->
                            sendLotteryResultNotification(eventId, entrantId, true, onOk, onErr))
                    .addOnFailureListener(onErr::accept);

        }).addOnFailureListener(onErr::accept);
    }

    public void declineInvitation(String eventId,
                                  String entrantId,
                                  Runnable onOk,
                                  Consumer<Exception> onErr) {

        EVENTS.document(eventId).get().addOnSuccessListener(doc -> {
            Event event = doc.toObject(Event.class);
            if (event == null) {
                onErr.accept(new Exception("Event not found"));
                return;
            }

            WriteBatch batch = db.batch();

            DocumentReference eventRef = EVENTS.document(eventId);
            DocumentReference inviteRef = INVITES.document(eventId + "_" + entrantId);

            // Remove from invited list
            batch.update(eventRef, "invitedList", FieldValue.arrayRemove(entrantId));

            // Add to cancelled list
            batch.update(eventRef, "cancelledEntrants", FieldValue.arrayUnion(entrantId));

            // Update the invitation document
            Map<String, Object> inviteUpdates = new HashMap<>();
            inviteUpdates.put("status", "declined");
            inviteUpdates.put("responded", true);
            inviteUpdates.put("updatedAt", new Date());
            batch.set(inviteRef, inviteUpdates, SetOptions.merge());

            batch.commit()
                    .addOnSuccessListener(v -> onOk.run())
                    .addOnFailureListener(onErr::accept);

        }).addOnFailureListener(onErr::accept);
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
     * Helper used for US 01.04.01 and 01.04.02.
     */
    public void sendLotteryResultNotification(String eventId,
                                              String entrantId,
                                              boolean isWinner,
                                              Runnable onOk,
                                              Consumer<Exception> onErr) {

        EVENTS.document(eventId).get()
                .addOnSuccessListener(doc -> {
                    Event e = doc.toObject(Event.class);
                    String name = (e != null && e.getEventName() != null)
                            ? e.getEventName()
                            : "";

                    Notification n = new Notification();
                    n.setSenderId("system");
                    n.setReceiverId(entrantId);
                    n.setEventId(eventId);
                    n.setType(isWinner ? "LOTTERY_WIN" : "LOTTERY_LOSS");
                    String msg = isWinner
                            ? "You were selected for: " + name
                            : "You were not selected for: " + name;
                    n.setMessage(msg);
                    n.setTimestamp(new Date());

                    sendNotification(n, onOk, onErr);
                })
                .addOnFailureListener(onErr::accept);
    }

    /**
     *
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

    /**
     * Live notifications for a given user id.
     */
    public LiveData<List<Notification>> observeNotificationsForUser(String userId) {
        MutableLiveData<List<Notification>> live = new MutableLiveData<>(new ArrayList<>());

        NOTIFS.whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) {
                        return;
                    }
                    List<Notification> list = snap.toObjects(Notification.class);
                    live.postValue(list);
                });

        return live;
    }

    /**
     * Fetch latest (most recent) notification for popup.
     */
    public void fetchLatestNotification(String userId,
                                        Consumer<Notification> onResult,
                                        Consumer<Exception> onErr) {

        NOTIFS.whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        onResult.accept(null);
                    } else {
                        Notification n = snap.getDocuments()
                                .get(0)
                                .toObject(Notification.class);
                        onResult.accept(n);
                    }
                })
                .addOnFailureListener(onErr::accept);
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
// Images (admin helpers)
// -------------------------

    /**
     * Small wrapper that keeps both the Firestore doc id and the ImageUpload data.
     * Used by the admin "review images" screen.
     */
    public static class ImageDoc {
        private final String docId;
        private final ImageUpload data;

        public ImageDoc(String docId, ImageUpload data) {
            this.docId = docId;
            this.data = data;
        }

        public String getDocId() { return docId; }
        public ImageUpload getData() { return data; }
    }

    /**
     * Fetch all image metadata documents.
     */
    public void fetchAllImages(Consumer<List<ImageDoc>> onResult,
                               Consumer<Exception> onErr) {
        IMAGES.get()
                .addOnSuccessListener(snap -> {
                    List<ImageDoc> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        ImageUpload img = doc.toObject(ImageUpload.class);
                        if (img != null) {
                            list.add(new ImageDoc(doc.getId(), img));
                        }
                    }
                    onResult.accept(list);
                })
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Delete an image metadata doc and also clear the poster URL
     * on the related event (if eventId is not null).
     */
    public void deleteImageAndClearEventPoster(String imageDocId,
                                               @Nullable String eventId,
                                               Runnable onOk,
                                               Consumer<Exception> onErr) {

        WriteBatch batch = db.batch();

        // delete image metadata
        batch.delete(IMAGES.document(imageDocId));

        // optionally clear event imageUrl
        if (eventId != null && !eventId.isEmpty()) {
            DocumentReference eventRef = EVENTS.document(eventId);
            Map<String, Object> u = new HashMap<>();
            u.put("imageUrl", null);
            batch.update(eventRef, u);
        }

        batch.commit()
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
    // Helpers / local cache
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

    /**
     * Adds an object (Event or Profile) both locally and to Firestore
     */
    private final MutableLiveData<ArrayList<Event>> Events = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ArrayList<Profile>> Profiles = new MutableLiveData<>(new ArrayList<>());

    /**
     * Adds event or profile to local cache and Firestore
     */
    public void Add(Object item) {
        if (item instanceof Event) {
            Event e = (Event) item;
            if (e.getEventId() == null || e.getEventId().isEmpty()) {
                e.setEventId(UUID.randomUUID().toString());
            }

            ArrayList<Event> list = Events.getValue();
            if (list != null && !list.contains(e)) {
                list.add(e);
                Events.postValue(list);
            }

            EVENTS.document(e.getEventId()).set(e)
                    .addOnSuccessListener(v -> Log.d("FirebaseViewModel", "Event added: " + e.getEventName()))
                    .addOnFailureListener(err -> Log.e("FirebaseViewModel", "Error adding event", err));

        } else if (item instanceof Profile) {
            Profile p = (Profile) item;
            if (p.getProfileId() == null || p.getProfileId().isEmpty()) {
                p.setProfileId(UUID.randomUUID().toString());
            }

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

    /**
     * Checks if an Event or Profile exists in the local cache
     */
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

    /**
     * Returns the locally cached events, sorted by registration end date
     */
    public ArrayList<Event> getEvents() {
        ArrayList<Event> list = Events.getValue();
        if (list == null) {
            return new ArrayList<>();
        }
        list.sort((e1, e2) -> e1.getRegistrationEnd().compareTo(e2.getRegistrationEnd()));
        return list;
    }

    /**
     *
     */
    public void signUpForEvent(String eventId, String entrantId, Runnable onSuccess, Consumer<Exception> onFailure) {
        db.collection("events").document(eventId)
                .update("acceptedEntrants", FieldValue.arrayUnion(entrantId))
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }

    // Simpler local filtering version (no Firestore index needed)
    public void fetchEventsByCategoryAndDate(String category, Date start, Date end,
                                             Consumer<List<Event>> onResult,
                                             Consumer<Exception> onErr) {

        Query query = EVENTS;

        if (category != null && !category.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("category", category);
        }

        query.get()
                .addOnSuccessListener(q -> {
                    List<Event> allEvents = q.toObjects(Event.class);
                    List<Event> filtered = new ArrayList<>();

                    for (Event e : allEvents) {
                        Date startTime = e.getEventStart();
                        Date endTime = e.getEventEnd();
                        boolean valid = true;

                        // Only show OPEN events
                        if (e.getStatus() == null || !e.getStatus().equalsIgnoreCase("Open")) {
                            valid = false;
                        }

                        // Date range filters
                        if (start != null && startTime != null && startTime.before(start)) {
                            valid = false;
                        }
                        if (end != null && endTime != null && endTime.after(end)){
                            valid = false;
                        }

                        if (valid) {
                            filtered.add(e);
                        }
                    }

                    onResult.accept(filtered);
                })
                .addOnFailureListener(onErr::accept);
    }

    //  Fetch all unique categories from "events" collection
    public void fetchAllCategories(Consumer<List<String>> onResult, Consumer<Exception> onErr) {
        EVENTS.get()
                .addOnSuccessListener(q -> {
                    List<String> categories = new ArrayList<>();
                    for (DocumentSnapshot doc : q.getDocuments()) {
                        String cat = doc.getString("category");
                        if (cat != null && !categories.contains(cat)) {
                            categories.add(cat);
                        }
                    }
                    categories.add(0, "All");
                    onResult.accept(categories);
                })
                .addOnFailureListener(onErr::accept);
    }

    /**
     * Deletes entrant profile and removes all references from events.
     * Works even if profile data in memory is outdated.
     * US 01.02.04
     */
    public void deleteProfileAndCleanOpenEvents(Profile profile, Runnable onOk, Consumer<Exception> onErr) {
        String profileId = profile.getProfileId();
        if (profileId == null || profileId.isEmpty()) {
            onErr.accept(new Exception("Profile ID missing"));
            return;
        }

        // Get latest profile from Firestore (to read waitlistedEvents field)
        PROFILES.document(profileId).get()
                .addOnSuccessListener(snapshot -> {

                    // read waitlistedEvents from profile doc (may be null)
                    List<String> waitlistedEvents = new ArrayList<>();
                    if (snapshot.exists()) {
                        Object raw = snapshot.get("waitlistedEvents");
                        if (raw instanceof List<?>) {
                            for (Object o : (List<?>) raw) {
                                if (o instanceof String) {
                                    waitlistedEvents.add((String) o);
                                }
                            }
                        }
                    }

                    // Query events where this profile is invited / accepted / cancelled
                    Task<QuerySnapshot> invitedTask =
                            EVENTS.whereArrayContains("invitedList", profileId).get();
                    Task<QuerySnapshot> acceptedTask =
                            EVENTS.whereArrayContains("acceptedEntrants", profileId).get();
                    Task<QuerySnapshot> cancelledTask =
                            EVENTS.whereArrayContains("cancelledEntrants", profileId).get();

                    Tasks.whenAllComplete(invitedTask, acceptedTask, cancelledTask)
                            .addOnSuccessListener(tasks -> {

                                // collect eventIds for each list type (to avoid duplicates)
                                Set<String> waitlistEventIds = new HashSet<>();
                                Set<String> invitedEventIds = new HashSet<>();
                                Set<String> acceptedEventIds = new HashSet<>();
                                Set<String> cancelledEventIds = new HashSet<>();

                                // from profile.waitlistedEvents
                                for (String evId : waitlistedEvents) {
                                    if (evId != null && !evId.isEmpty()) {
                                        waitlistEventIds.add(evId);
                                    }
                                }

                                // from query: invitedList contains profileId
                                if (invitedTask.isSuccessful() && invitedTask.getResult() != null) {
                                    for (DocumentSnapshot doc : invitedTask.getResult().getDocuments()) {
                                        invitedEventIds.add(doc.getId());
                                    }
                                }

                                // from query: acceptedEntrants contains profileId
                                if (acceptedTask.isSuccessful() && acceptedTask.getResult() != null) {
                                    for (DocumentSnapshot doc : acceptedTask.getResult().getDocuments()) {
                                        acceptedEventIds.add(doc.getId());
                                    }
                                }

                                // from query: cancelledEntrants contains profileId
                                if (cancelledTask.isSuccessful() && cancelledTask.getResult() != null) {
                                    for (DocumentSnapshot doc : cancelledTask.getResult().getDocuments()) {
                                        cancelledEventIds.add(doc.getId());
                                    }
                                }

                                WriteBatch batch = db.batch();

                                // clean waitlist
                                for (String eventId : waitlistEventIds) {
                                    DocumentReference eventRef = EVENTS.document(eventId);
                                    batch.update(eventRef, "waitingList", FieldValue.arrayRemove(profileId));
                                }

                                // clean invited
                                for (String eventId : invitedEventIds) {
                                    DocumentReference eventRef = EVENTS.document(eventId);
                                    batch.update(eventRef, "invitedList", FieldValue.arrayRemove(profileId));
                                }

                                // clean accepted
                                for (String eventId : acceptedEventIds) {
                                    DocumentReference eventRef = EVENTS.document(eventId);
                                    batch.update(eventRef, "acceptedEntrants", FieldValue.arrayRemove(profileId));
                                }

                                // clean cancelled
                                for (String eventId : cancelledEventIds) {
                                    DocumentReference eventRef = EVENTS.document(eventId);
                                    batch.update(eventRef, "cancelledEntrants", FieldValue.arrayRemove(profileId));
                                }

                                // After cleaning events, delete the profile itself
                                batch.commit()
                                        .addOnSuccessListener(v ->
                                                PROFILES.document(profileId).delete()
                                                        .addOnSuccessListener(x -> onOk.run())
                                                        .addOnFailureListener(onErr::accept)
                                        )
                                        .addOnFailureListener(onErr::accept);
                            })
                            .addOnFailureListener(onErr::accept);

                })
                .addOnFailureListener(onErr::accept);
    }


    /**
     * Retrieves a Profile document from Firestore given its profile ID.
     * @param profileId  The unique ID of the profile document to fetch.
     * @param onSuccess  Callback executed when the profile is successfully retrieved.
     *                   Receives the corresponding Profile object.
     * @param onError    Callback executed when an error occurs, such as when the
     *                   document is missing or the Firestore request fails.
     */
    public void getProfileById(String profileId,
                               Consumer<Profile> onSuccess,
                               Consumer<Exception> onError) {

        PROFILES.document(profileId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Profile p = document.toObject(Profile.class);
                        onSuccess.accept(p);
                    } else {
                        onError.accept(new Exception("Profile not found"));
                    }
                })
                .addOnFailureListener(onError::accept);
    }

    public LiveData<Profile> getProfileLive(String profileId) {
        MutableLiveData<Profile> data = new MutableLiveData<>();

        PROFILES.document(profileId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        data.setValue(doc.toObject(Profile.class));
                    } else {
                        data.setValue(null); // no profile found
                    }
                })
                .addOnFailureListener(e -> data.setValue(null));

        return data;
    }

    private MutableLiveData<List<Notification>> allNotifications = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Notification>> getAllNotificationsLive() {

        NOTIFS.orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {

                    if (err != null || snap == null) {
                        allNotifications.postValue(new ArrayList<>());
                        return;
                    }

                    List<Notification> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Notification n = d.toObject(Notification.class);
                        if (n != null) list.add(n);
                    }

                    allNotifications.postValue(list);
                });

        return allNotifications;
    }




    public void uploadProfilePicture(String profileId, Uri imageUri,
                                     Consumer<String> onSuccess,
                                     Consumer<Exception> onError) {

        String fileName = "profile_pictures/" + profileId + "_" + UUID.randomUUID() + ".jpg";
        StorageReference ref = storageRef.child(fileName);

        ref.putFile(imageUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            onSuccess.accept(uri.toString());
                        }).addOnFailureListener(onError::accept)
                )
                .addOnFailureListener(onError::accept);
    }

    public void updateProfilePicture(String profileId, String downloadUrl,
                                     Runnable onOk, Consumer<Exception> onErr) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", downloadUrl);

        PROFILES.document(profileId)
                .update(updates)
                .addOnSuccessListener(v -> onOk.run());
    }

        // -------------------------
// Admin - delete event + cleanup
// -------------------------

        /**
         * Admin-only: deletes an event and cleans related data:
         * - removes eventId from entrants' arrays (waitlistedEvents, allEvents, eventsJoined)
         * - deletes invitations for this event
         * - deletes notifications for this event
         * - deletes image metadata for this event
         * - deletes the event document itself
         */
        public void adminDeleteEventAndCleanup(
                String eventId,
                Runnable onOk,
                Consumer < Exception > onErr){
            if (eventId == null || eventId.isEmpty()) {
                onErr.accept(new IllegalArgumentException("eventId is required"));
                return;
            }

            EVENTS.document(eventId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        Event event = doc.toObject(Event.class);
                        if (event == null) {
                            onErr.accept(new Exception("Event not found"));
                            return;
                        }

                        // collect all entrant/profile IDs involved
                        List<String> allIds = new ArrayList<>();

                        if (event.getWaitingList() != null) {
                            allIds.addAll(event.getWaitingList());
                        }
                        if (event.getInvitedList() != null) {
                            allIds.addAll(event.getInvitedList());
                        }
                        if (event.getAcceptedEntrants() != null) {
                            allIds.addAll(event.getAcceptedEntrants());
                        }
                        if (event.getCancelledEntrants() != null) {
                            allIds.addAll(event.getCancelledEntrants());
                        }

                        // remove duplicates
                        List<String> uniqueIds = new ArrayList<>();
                        for (String id : allIds) {
                            if (id != null && !uniqueIds.contains(id)) {
                                uniqueIds.add(id);
                            }
                        }

                        // load notifications + images for this event
                        Task<QuerySnapshot> notifTask =
                                NOTIFS.whereEqualTo("eventId", eventId).get();
                        Task<QuerySnapshot> imageTask =
                                IMAGES.whereEqualTo("eventId", eventId).get();

                        Tasks.whenAllComplete(notifTask, imageTask)
                                .addOnSuccessListener(tasks -> {

                                    WriteBatch batch = db.batch();

                                    // delete event doc
                                    DocumentReference eventRef = EVENTS.document(eventId);
                                    batch.delete(eventRef);

                                    // clean entrant profile arrays
                                    for (String profileId : uniqueIds) {
                                        DocumentReference profRef = PROFILES.document(profileId);
                                        batch.update(profRef, "waitlistedEvents",
                                                FieldValue.arrayRemove(eventId));
                                        batch.update(profRef, "allEvents",
                                                FieldValue.arrayRemove(eventId));
                                        batch.update(profRef, "eventsJoined",
                                                FieldValue.arrayRemove(eventId));
                                    }

                                    // delete invitations (eventId_entrantId)
                                    for (String profileId : uniqueIds) {
                                        if (profileId == null) continue;
                                        String invId = eventId + "_" + profileId;
                                        DocumentReference invRef = INVITES.document(invId);
                                        batch.delete(invRef);
                                    }

                                    // delete notifications for this event
                                    if (notifTask.isSuccessful() && notifTask.getResult() != null) {
                                        for (DocumentSnapshot nDoc : notifTask.getResult().getDocuments()) {
                                            batch.delete(nDoc.getReference());
                                        }
                                    }

                                    // delete image metadata for this event
                                    if (imageTask.isSuccessful() && imageTask.getResult() != null) {
                                        for (DocumentSnapshot iDoc : imageTask.getResult().getDocuments()) {
                                            batch.delete(iDoc.getReference());
                                        }
                                    }

                                    batch.commit()
                                            .addOnSuccessListener(v -> onOk.run())
                                            .addOnFailureListener(onErr::accept);

                                })
                                .addOnFailureListener(onErr::accept);

                    })
                    .addOnFailureListener(onErr::accept);
        }


    public void deleteProfile(String profileID) {
        PROFILES.document(profileID).delete();
    }

    public void deleteAllOrganizerEvents(String organizerId,
                                         Runnable onOk,
                                         Consumer<Exception> onErr) {

        if (organizerId == null || organizerId.isEmpty()) {
            onErr.accept(new IllegalArgumentException("Organizer ID required"));
            return;
        }

        EVENTS.whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(query -> {

                    // No events to delete
                    if (query.isEmpty()) {
                        onOk.run();
                        return;
                    }

                    List<DocumentSnapshot> docs = query.getDocuments();
                    final int total = docs.size();
                    final int[] completed = {0};

                    for (DocumentSnapshot doc : docs) {
                        String eventId = doc.getId();

                        adminDeleteEventAndCleanup(
                                eventId,
                                () -> {
                                    completed[0]++;
                                    if (completed[0] == total) {
                                        onOk.run();
                                    }
                                },
                                e -> onErr.accept(e)
                        );
                    }
                })
                .addOnFailureListener(onErr::accept);
    }



}


