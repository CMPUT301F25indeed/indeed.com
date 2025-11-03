package com.example.indeedgambling;

import android.util.Log;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Event {
    private String eventId;
    private String eventName;
    private String description;
    private String organizerId; //Where are we getting the ID from, and how to we use to it refernce the organizer? Hashes are not unique.
    private String category;
    private String location;
    private Date eventStart;
    private Date eventEnd;
    private Date registrationStart;
    private Date registrationEnd;
    private int maxWaitingEntrants;
    private String imageUrl;
    private String qrCodeURL;
    private String status;    // planned/open/closed/completed
    private String criteria;  // lottery notes
    private ArrayList<String> waitingList; // entrant IDs
    private ArrayList<String> invitedList; // Entrant IDs


    /**
     * Creates an event. Enforces Open is before Closed with an IllegalArgumentException
     *
     * @param EventName         Title for the event
     * @param RegistrationOpen  When entrants are allowed to apply
     * @param RegistrationClose When entrant applications are no longer accepted
     * @param EventStart        When the actual event starts
     * @param EventEnd          When the actual event ends
     * @param OrgID             Creator's Organizer ID
     * @param Description       Description of Event
     * @param Critera           Critera (?)
     * @param Category          Category of Event
     * @param QRCodeURL         URL of the poster image for the event
     */
    Event(String EventName, Date RegistrationOpen, Date RegistrationClose, Date EventStart, Date EventEnd, String OrgID, String Description, String Critera, String Category, String QRCodeURL) {
        //Potential profanity filter.

        //TODO: Check if an exact match exists already ().
        eventName = EventName;

        //Raise error if open date after or equal end date & time
        if (RegistrationClose.before(RegistrationOpen)) {
            throw new IllegalArgumentException("registrationOpen cannot be after registrationClose");
        }
        //Raise error if open date after or equal end date & time
        if (EventEnd.before(EventStart)) {
            throw new IllegalArgumentException("Event Start cannot be after Event End");
        }

        registrationStart = RegistrationOpen;
        registrationEnd = RegistrationClose;

        eventStart = EventStart;
        eventEnd = EventEnd;

        waitingList = new ArrayList<String>();
        invitedList = new ArrayList<String>();

        //0 is value for no limit
        maxWaitingEntrants = 0;

        //Setting reference owner
        organizerId = OrgID;

        description = Description;

        criteria = Critera;

        category = Category;

        qrCodeURL = QRCodeURL;

        this.status = getStatus();
    }
    Event(String EventName, Date RegistrationOpen, Date RegistrationClose, Date EventStart, Date EventEnd, String OrgID, String Description, String Critera, String Category, String QRCodeURL, int MaxEntrants) {
        //Potential profanity filter.

        //TODO: Check if an exact match exists already ().
        eventName = EventName;

        //Raise error if open date after or equal end date & time
        if (RegistrationClose.before(RegistrationOpen)) {
            throw new IllegalArgumentException("registrationOpen cannot be after registrationClose");
        }
        //Raise error if open date after or equal end date & time
        if (EventEnd.before(EventStart)) {
            throw new IllegalArgumentException("Event Start cannot be after Event End");
        }

        registrationStart = RegistrationOpen;
        registrationEnd = RegistrationClose;

        eventStart = EventStart;
        eventEnd = EventEnd;

        waitingList = new ArrayList<String>();
        invitedList = new ArrayList<String>();

        //0 is value for no limit. Can't have negative, so it sets to zero
        maxWaitingEntrants = Math.max(0,MaxEntrants);

        //Setting reference owner
        organizerId = OrgID;

        description = Description;

        criteria = Critera;

        category = Category;

        qrCodeURL = QRCodeURL;

        this.status = getStatus();
    }

    /** NO URL constructor, no Max entrants Constructor.
     * @param EventName
     * @param RegistrationOpen
     * @param RegistrationClose
     * @param EventStart
     * @param EventEnd
     * @param OrgID
     * @param Description
     * @param Criteria
     * @param Category
     */
    Event(String EventName, Date RegistrationOpen, Date RegistrationClose, Date EventStart, Date EventEnd, String OrgID, String Description, String Criteria, String Category) {
        //Potential profanity filter.

        //TODO: Check if an exact match exists already ().
        eventName = EventName;

        //Raise error if open date after or equal end date & time
        if (RegistrationClose.before(RegistrationOpen)) {
            throw new IllegalArgumentException("registrationOpen cannot be after registrationClose");
        }
        //Raise error if open date after or equal end date & time
        if (EventEnd.before(EventStart)) {
            throw new IllegalArgumentException("Event Start cannot be after Event End");
        }

        registrationStart = RegistrationOpen;
        registrationEnd = RegistrationClose;

        eventStart = EventStart;
        eventEnd = EventEnd;

        waitingList = new ArrayList<String>();
        invitedList = new ArrayList<String>();

        //0 is value for no limit. Can't have negative, so it sets to zero
        maxWaitingEntrants = 0;

        //Setting reference owner
        organizerId = OrgID;

        description = Description;

        criteria = Criteria;

        category = Category;

        qrCodeURL = "";

        this.status = getStatus();
    }

    //No arg constructor for Firebase
    Event() {
    }

    //Returns the entrant names for the Entrants

    /** WIP
     * @return NOTHING RIGHT NOW
     */
    public ArrayList<String> getWaitingEntrantNames() {
        //Find the matching names from the IDS.

        return waitingList;
    }

    /** Provides all Profile IDS for entrants who signed onto the Waitlist
     *
     * @return ArrayList of entrant's IDs
     */
    public ArrayList<String> getWaitingEntrantIDs() {
        return waitingList;
    }

    public String getEventName() {
        return eventName;
    }

    public Date getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(Date registrationStart) {
        this.registrationStart = registrationStart;
    }

    public Date getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(Date registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    /** Helper function that checks if RIGHT NOW is before reg period
     *
     * @return True if before Reg Open, false otherwise
     */
    public boolean BeforeRegPeriod(){
        return new Date().before(this.registrationStart);
    }

    /** Helper function that checks if RIGHT NOW is after the reg period
     *
     * @return True if after Reg Close, false otherwise
     */
    public boolean AfterRegPeriod(){
        return new Date().after(this.registrationEnd);
    }

    /** Returns True if the event has finished**/
    public boolean EventPassed(){
        return this.eventEnd.before(new Date());
    }

    public void setWaitingEntrants(ArrayList<String> waitingEntrantIDs) {
        this.waitingList = waitingEntrantIDs;
    }

    public void setInvitedEntrants(ArrayList<String> invitedEntrantIDs) {
        this.invitedList = invitedEntrantIDs;
    }

    public int getMaxWaitingEntrants() {
        return maxWaitingEntrants;
    }

    /**
     *
     * @return
     */
    public String getMaxWaitingEntrantsString(){
        if (maxWaitingEntrants == 0){
            return "Unlimited";
        }
        return Integer.toString(maxWaitingEntrants);
    }

    public ArrayList<String> getInvitedEntrantIDs() {
        return invitedList;
    }

    public ArrayList<String> getInvitedList() {
        return invitedList;
    }

    public void setInvitedList(ArrayList<String> invitedList) {
        this.invitedList = invitedList;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getQrCodeURL() {
        return qrCodeURL;
    }

    public void setQrCodeURL(String qrCodeURL) {
        this.qrCodeURL = qrCodeURL;
    }

    /** Updates the status of the event to the current moment before returning.
     *
     * @return Current Status of event
     */
    public String getStatus() {
        //Checks if reg period is now
        if (this.RegistrationOpen()){
            status = "Open";
        }
        else if (!this.RegistrationOpen()){
            status = "Closed";
        }
        //Checks if reg period is upcoming
        else if (this.BeforeRegPeriod()){
            status = "Planned";
        }
        //Checks if reg period has passed, and there are no invited entrants
        else if (this.AfterRegPeriod() && !invitedList.isEmpty()){
            status = "Completed";
        }
        else{
            status = "Completed";
        }
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(ArrayList<String> waitingList) {
        this.waitingList = waitingList;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    /**
     * Adds an entrant to the waiting list of the event.
     * Duplicant signees are not added.
     *
     * @param signeeID Entrant ID.
     * @return True if entrant added, False otherwise
     */
    public boolean addToWaitingList(String signeeID) {
        if (!this.atCapacity()) {
            waitingList.add(signeeID);
            //Push to cloud.
            return true;
        } else {
            Log.d("Event Debug", "addToWaitingList: " + "Event full");
            return false;
        }
    }

    /**
     * Updates the current maximum number of signees.
     * Enforces positive values, minimum is 0
     *
     * @param max New maximum number of entrants for waiting list
     */
    public void setMaxEntrants(int max) {
        //No negative max. 0 is unlimited entrants
        maxWaitingEntrants = Math.max(max, 0);
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getOwnerID() {
        return organizerId;
    }

    public void setOwner(String owner) {
        organizerId = owner;
    }

    public Date getEventStart() {
        return eventStart;
    }

    public void setEventStart(Date eventStart) {
        this.eventStart = eventStart;
    }

    public Date getEventEnd() {
        return eventEnd;
    }

    public void setEventEnd(Date eventEnd) {
        this.eventEnd = eventEnd;
    }

    /**
     * Overriding toString's function on Events to return the name of the event instead.
     *
     * @return Name of event
     */
    @Override
    public String toString() {
        return eventName;
    }


    /**
     * Overriding equals function for Events to compare name only
     *
     * @param obj Object to compare
     * @return True if they match names
     */
    @Override
    public boolean equals(Object obj) {
        //If perfectly equal, return true
        if (this == obj) return true;

        //If types do not match, return false
        if (obj == null || this.getClass() != obj.getClass()) return false;

        return this.getEventName().equals(((Event) obj).eventName);

    }

    /**
     * Returns true if the WaitingList is out of room for entrants
     *
     * @return True if waitlist CANNOT accept any more entrants
     */
    public boolean atCapacity() {
        //0 check is for no-limit. 0 => no limit
        return (this.maxWaitingEntrants >= this.waitingList.size()) || this.maxWaitingEntrants == 0;
    }

    /**
     * Returns true if the Event registration period is open now.
     *
     * @return True if Registration Open
     */
    public boolean RegistrationOpen() {
        return (new Date().before(this.registrationEnd) && new Date().after(this.registrationStart));
    }

    /**
     * Checks if the event Registration is open AND if waitlist has room.
     *
     * @return True if accepting waitlist Entrants, False otherwise
     */
    public boolean waitList_registrable() {
        return (!this.atCapacity()) && this.RegistrationOpen();

    }

    /** Returns if the event has a defined location
     * May be removed if locations are mandatory
     * @return True if Location defined.
     */
    public boolean hasLocation(){
        return !(location == null);
    }
}