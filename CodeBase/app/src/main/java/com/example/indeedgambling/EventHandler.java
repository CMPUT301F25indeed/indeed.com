package com.example.indeedgambling;

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

    public EventHandler(){
        Events = new ArrayList<Event>();
    }

    public EventHandler(Collection<Event> events){
        Events = new ArrayList<Event>();
        Events.addAll(events);
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
                Event1.getRegistrationPeriod().second.compareTo(Event2.getRegistrationPeriod().second));

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
            if (instance.getOwner() == owner && instance.getRegistrationPeriod().second.after(new Date())){
                ReturnArray.add(instance);
            }
        }

        //Sort by closedate Desc (latest close first)
        ReturnArray.sort((Event1, Event2) ->
                Event2.getRegistrationPeriod().second.compareTo(Event1.getRegistrationPeriod().second));

        return ReturnArray;
    }

    /** Returns all events that are currently still open
     * @return Arraylist of Open Events, sorted by closetime desc
     */
    public ArrayList<Event> GetActiveEvents(){
        ArrayList<Event> ReturnArray = new ArrayList<Event>();
        for (Event instance: Events) {
            if (instance.getRegistrationPeriod().second.after(new Date())){
                ReturnArray.add(instance);
            }
        }
        return ReturnArray;
    }

    /**Debug Function
     *
     */
    public void AddEvent(Event e){
        Events.add(e);
    }

    public ArrayList<Event> GetEvents(){
       return Events;
    }
}
