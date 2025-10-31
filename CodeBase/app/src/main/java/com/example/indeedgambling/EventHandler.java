package com.example.indeedgambling;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/** Class to retrieve data from Firebase and aggregate data
 * TODO: Firebase integration
 */
public class EventHandler {
    private ArrayList<Event> Events;
    private FirebaseFirestore db;
    private CollectionReference EventRef;

    public EventHandler(){
        Events = new ArrayList<Event>();
        db = FirebaseFirestore.getInstance();
        EventRef = db.collection("events");
    }

    /** Returns all events under an organizer, regardless of date
     * @param owner Owner of the events to return
     * @return Arraylist of Events, sorted by earliest closedate first
     */
    public ArrayList<Event> EventsOfOrganizer(Organizer owner){
        ArrayList<Event> ReturnArray = new ArrayList<Event>();
        for (Event instance: Events) {
            if (instance.getOwner() == owner){
                ReturnArray.add(instance);
            }
        }

        //Sort by earliest closedate first
        ReturnArray.sort((Event1, Event2) ->
                Event1.getRegistrationEnd().compareTo(Event2.getRegistrationEnd()));

        return ReturnArray;
    }

    /** Returns an ArrayList of upcoming events for an owner
     * @param owner Owner of the events to return
     * @return List of events sorted by latest closedate first
     */
    public ArrayList<Event> UpcomingEventsOfOrganizer(Organizer owner){
        ArrayList<Event> ReturnArray = new ArrayList<Event>();

        //Adds events which end later than now, and are owned by the argument owner
        for (Event instance: Events) {
            if (instance.getOwner() == owner && instance.getRegistrationEnd().after(new Date())){
                ReturnArray.add(instance);
            }
        }

        //Sort by closedate Desc (latest close first)
        ReturnArray.sort((Event1, Event2) ->
                Event2.getRegistrationEnd().compareTo(Event1.getRegistrationEnd()));

        return ReturnArray;
    }

    /** Returns all events that are currently still open
     * @return Arraylist of Open Events
     */
    public ArrayList<Event> GetActiveEvents(){
        ArrayList<Event> ReturnArray = new ArrayList<Event>();
        for (Event instance: Events) {
            if (instance.getRegistrationEnd().after(new Date())){
                ReturnArray.add(instance);
            }
        }
        return ReturnArray;
    }

    /** Adds event to local DB and Firebase
     * @param e Event to be added to db
     */
    public void AddEvent(Event e){
        Events.add(e);
        EventRef.document(e.toString()).set(e);
    }

    /** Returns an Arraylist of all events, regardless of owner or date.
     * @return Event Arraylist
     */
    public ArrayList<Event> GetEvents(){
       return Events;
    }

    /**Tester that should Update local Events with Firebase
     * Want to include pushing to Firebase inside too
     */
    public boolean SyncWithFireBase(){
        //Pulling
        ArrayList<Event> list = new ArrayList<Event>();
        EventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Events = new ArrayList<Event>();
                for (DocumentSnapshot event : task.getResult()) {
                    //Update to server, as local changes shouldn't exist on database
                    //Check duplicates by name and date?

                    //Replace local with server, for now
                    Events.add(event.toObject(Event.class));
                }
            } else{
                Log.e("Firebase Error", "Error getting Events");
            }
        });
        Log.d("Distance","Added OnCompleteListener");
        return false;
    }
}
