package com.example.indeedgambling;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Entrant user in the Event Lottery System.
 *
 * This class extends {@link Profile} and automatically sets
 * the role to "entrant".
 *
 * Notes:
 * - Firestore requires a no-argument constructor (provided)
 * - roleVerified is set to true by default for entrants
 * - Used when mapping profile data to an Entrant-specific object
 */
public class Entrant extends Profile {
    private List<String> waitlistedEvents;
    private List<String> allEvents;

    /**
     * Empty constructor required for Firestore data mapping.
     */
    public Entrant() { super();
        this.waitlistedEvents = new ArrayList<>();
        this.allEvents = new ArrayList<>();
        setRoleVerified(true);
        setRole("entrant");}




    public List<String> getWaitlistedEvents() {
        return waitlistedEvents;
    }

    public void setWaitlistedEvents(List<String> waitlistedEvents) {this.waitlistedEvents = waitlistedEvents;}

    public List<String> getAllEvents() {
        return allEvents;
    }

    public void setAllEvents(List<String> allEvents) {
        this.allEvents = allEvents;
    }

    public void add2Entrant(String val){
        waitlistedEvents.add(val);
        allEvents.add(val);
    }

    public void remove2Entrant(String val){
        waitlistedEvents.remove(val);
        allEvents.remove(val);
    }

    public void removeWaitlistedEvent(String val){
        waitlistedEvents.remove(val);
    }


    /**
     * Creates a new Entrant profile.
     *
     * @param profileId Unique profile ID (usually SHA-256 hash of email+password)
     * @param name Display name of entrant
     * @param email Email of entrant
     * @param phone Optional contact number
     * @param passwordHash SHA-256 hashed password
     */
    public Entrant(String profileId, String name, String email, String phone, String passwordHash) {
        super(profileId, name, email, phone, "entrant", passwordHash);
        setRoleVerified(true);
        this.waitlistedEvents = new ArrayList<>();
        this.allEvents = new ArrayList<>();
    }


}
