package com.example.indeedgambling;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.security.acl.Owner;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Basic model for Events
 */
public class Event {
    private String eventName;
    private Pair<Date,Date> registrationPeriod;

    private Set<Entrant> waitingEntrants;
    private int maxEntrants;
    private Set<Entrant> invitedEntrants;

    private Organizer Owner;

    /**
     * Creates an event. Enforces Open is before Closed with an IllegalArgumentException
     * @param EventName Title for the event
     * @param registrationOpen When entrants are allowed to apply
     * @param registrationClose When entrant applications are no longer accepted
     */
    Event(String EventName, Date registrationOpen, Date registrationClose, Organizer owner){
        //Potential profanity filter.

        //Check if an exact match exists already ().
        eventName = EventName;

        //Raise error if open date after or equal end date & time
        if (registrationClose.before(registrationOpen)){
            throw new IllegalArgumentException("registrationOpen cannot be after registrationClose");
        }

        registrationPeriod = new Pair<>(registrationOpen,registrationClose);

        //Default value for no limit. Cannot have negative maximum entrants, so this should be fine.
        maxEntrants = -1;

        //Setting reference owner
        Owner = owner;
    }

    Event(String EventName, Date registrationOpen, Date registrationClose, Organizer owner, int MaxEntrants){
        //Potential profanity filter.

        //Check if an exact match exists already ().
        eventName = EventName;

        //Raise error if open date after or equal end date & time
        if (registrationClose.before(registrationOpen)){
            throw new IllegalArgumentException("registrationOpen cannot be after registrationClose");
        }

        registrationPeriod = new Pair<>(registrationOpen,registrationClose);

        //Default value for no limit. Cannot have negative maximum entrants, so this should be fine.
        maxEntrants = MaxEntrants;

        //Setting reference owner
        Owner = owner;
    }

    /**
     * Returns a set of all entrants on the waiting list
     * @return Entrants???
     */
    public Set<Entrant> getWaitingEntrants() {
        return waitingEntrants;
    }

    public String getEventName() {
        return eventName;
    }

    public Pair<Date, Date> getRegistrationPeriod() {
        return registrationPeriod;
    }

    public int getMaxEntrants() {
        return maxEntrants;
    }

    public Set<Entrant> getInvitedEntrants() {
        return invitedEntrants;
    }

    /** Adds an entrant to the waiting list of the event.
     * Duplicant signees are not added.
     * @param signee Entrant object type.
     */
    public void addToWaitingList(Entrant signee){
        waitingEntrants.add(signee);
    }

    /** Adds all entrants in a collection to the waitlist for the the event.
     * Duplicant entrants are not added
     * @param signees Collection of entrants to be added
     */
    public void addToWaitingList(Collection<Entrant> signees){
        waitingEntrants.addAll(signees);
    }

    /** Updates the current maximum number of signees.
     * Enforces positive values & Enforces maximum larger than current signees.
     * @param max New maximum number of entrants for waiting list
     */
    public void setMaxEntrants(int max){
        //Prevents empty entrants
        if (max <= 0){
            throw new IllegalArgumentException("Maximum for event must be a positive number!");
        }

        //Raise error if max is less than current signees.
        if (max < waitingEntrants.size()){
            throw new IllegalArgumentException("New maximum less than current signees!");
        }
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Organizer getOwner() {
        return Owner;
    }

    public void setOwner(Organizer owner) {
        Owner = owner;
    }


    /** Overriding toString's function on Events to return the name of the event instead.
     * @return Name of event
     */
    @NonNull
    @Override
    public String toString(){
        return eventName;
    }
}
