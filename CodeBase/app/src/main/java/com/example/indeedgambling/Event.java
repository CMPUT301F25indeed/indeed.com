package com.example.indeedgambling;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Event implements Serializable {
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
    //US: 02.03.01
    private int maxWaitingEntrants;
    private String maxWaitingEntrantsString;
    private String imageUrl;
    private String qrCodeURL;
    private String status;    // planned/open/closed/completed
    private String criteria;  // lottery notes
    private ArrayList<String> waitingList = new ArrayList<String>(); // entrant IDs
    private ArrayList<String> invitedList = new ArrayList<String>(); // Entrant IDs
    private ArrayList<String> cancelledEntrants = new ArrayList<String>(); //invited entrants who declined or were removed
    private ArrayList<String> acceptedEntrants = new ArrayList<String>(); //invited entrants who declined or were removed

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
        cancelledEntrants = new ArrayList<String>();

        //0 is value for no limit
        maxWaitingEntrants = 0;

        //Setting reference owner
        organizerId = OrgID;

        description = Description;

        criteria = Critera;

        category = Category;

        qrCodeURL = QRCodeURL;

        this.status = getStatus();

        //Event ID : Hash of OrgIdEventnameEventstart
        this.eventId = new HashUtil().sha256(organizerId.concat(eventName).concat(EventStart.toString()));
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

        //Event ID : Hash of OrgIdEventnameEventstart
        this.eventId = new HashUtil().sha256(organizerId.concat(eventName).concat(EventStart.toString()));
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
        cancelledEntrants = new ArrayList<String>();

        //0 is value for no limit. Can't have negative, so it sets to zero
        maxWaitingEntrants = 0;

        //Setting reference owner
        organizerId = OrgID;

        description = Description;

        criteria = Criteria;

        category = Category;

        qrCodeURL = "";

        this.status = getStatus();

        //Event ID : Hash of OrgIdEventnameEventstart
        this.eventId = new HashUtil().sha256(organizerId.concat(eventName).concat(EventStart.toString()));

    }

    //No arg constructor for Firebase
    Event() {
    }

                                                //--------------------Setters / Getters ----------------------//
    /** Retrieves the event's display name
     * @return String: Events name
     */
    public String getEventName() {
        return eventName;
    }


    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /** Retrieves the open date and time for waitlist registration
     * @return Date object of registrationStart
     */
    public Date getRegistrationStart() {
        return registrationStart;
    }

    /** Assigns the object's registrationStart attribute to the value of the Date object passed.
     * @param registrationStart
     */
    public void setRegistrationStart(Date registrationStart) {
        this.registrationStart = registrationStart;
    }

    /** Retrieves the datetime that the Waitlist Registration closes at.
     * @return Date Object
     */
    public Date getRegistrationEnd() {
        return registrationEnd;
    }

    /** Assigns the object's datetime for when the Waitlist Registration will close.
     * @param registrationEnd
     */
    public void setRegistrationEnd(Date registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    /** Returns the objects attribute that determines the number of entrants who can sign up to the waitlist
     * @return Integer of the maximum waitlist size
     */
    public int getMaxWaitingEntrants() {
        return maxWaitingEntrants;
    }


    /**
     * Updates the current maximum number of signees.
     * Enforces positive values, minimum is 0
     * @param max New maximum number of entrants for waiting list
     */
    public void setMaxWaitingEntrants(int max) {
        //No negative max. 0 is unlimited entrants
        maxWaitingEntrants = Math.max(max, 0);
    }
    /**
     * Sets the string representation of maximum waiting entrants
     * Used by Firebase for serialization/deserialization
     *
     * @param maxWaitingEntrantsString String representation of max waiting entrants
     */
    public void setMaxWaitingEntrantsString(String maxWaitingEntrantsString) {
        this.maxWaitingEntrantsString = maxWaitingEntrantsString;
    }

    /** Returns the ProfileIDs of all Entrants who have been invited
     * @return Array of Strings IDs.
     */
    public List<String> getInvitedList() {
        return invitedList;
    }

    /** Sets the object's record of invited entrants to match the passed object
     * @param invitedList ArrayList of strings to set invitedList to
     */
    public void setInvitedList(ArrayList<String> invitedList) {
        this.invitedList = invitedList;
    }

    /** Returns the events generated ID.
     * @return Event ID hash String
     */
    public String getEventId() {
        return eventId;
    }

    /** Sets the EventID
     *  Needed by Firebase to create the object
     * @return
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /** Retrieves the description of the event as a String object.
     * @return String description
     */
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
     * @return Current Status of event
     */
    public String getStatus() {
        Date now = new Date();

        if (registrationStart == null || registrationEnd == null || eventStart == null || eventEnd == null) {
            return "Unknown";
        }

        if (now.before(registrationStart)) {
            status = "Planned"; // registration not started
        }
        else if (now.after(registrationStart) && now.before(registrationEnd)) {
            status = "Open"; // registration ongoing
        }
        else if (now.after(registrationEnd) && now.before(eventEnd)) {
            status = "Closed"; // registration closed, event not yet done
        }
        else if (now.after(eventEnd)) {
            status = "Completed"; // event is over
        }
        else {
            status = "Unknown";
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

    public Date getEventStart() {
        return eventStart;
    }

    /** Overwrites the start of the event in the object locally.
     *
     * @param eventStart
     */
    public void setEventStart(Date eventStart) {
        this.eventStart = eventStart;
    }

    /** Retrieves the datetime that the event Ends at.
     * @return Date object
     */
    public Date getEventEnd() {
        return eventEnd;
    }

    /** Overwrites the ending datetime for the Event with the passed eventEnd object.
     * @param eventEnd Date object that is the new endtime for the event
     */
    public void setEventEnd(Date eventEnd) {
        this.eventEnd = eventEnd;
    }

    /** Retrieves the list of IDs for Entrants who cancelled
     * @return
     */
    public ArrayList<String> getCancelledEntrants() {
        return cancelledEntrants;
    }

    /** Sets the ID list of cancelled Entrants to the passed ArrayList Object locally. No server changes
     * @param cancelledEntrants ArrayList that overwrites objects'
     */
    public void setCancelledEntrants(ArrayList<String> cancelledEntrants) {
        this.cancelledEntrants = cancelledEntrants;
    }

    /**
     * @return the current ID list of accepted Entrants
     */
    public ArrayList<String> getAcceptedEntrants() {
        return acceptedEntrants;
    }

    /** Overwrites the local Event's list of AcceptedEntrants with the passed Argument
     * @param acceptedEntrants Overwrites the object's acceptedEntrants list.
     */
    public void setAcceptedEntrants(ArrayList<String> acceptedEntrants) {
        this.acceptedEntrants = acceptedEntrants;
    }

    //---------------------------------------------------- Helpers ---------------------------//
    /** Helper function that checks if RIGHT NOW is before reg period opens
     * @return True if before Reg Open, false otherwise
     */
    public boolean BeforeRegPeriod(){
        return new Date().before(this.registrationStart);
    }

    /** Helper function that checks if RIGHT NOW is after the reg period closes
     * @return True if after Reg Close, false otherwise
     */
    public boolean AfterRegPeriod(){
        return new Date().after(this.registrationEnd);
    }

    /** Returns True if the event's runtime has finished**/
    public boolean EventPassed(){
        return this.eventEnd.before(new Date());
    }


    /** Help function that will return the meaning of the max. 0 => unlimited, normal otherwise.
     * @return String of Max Waiting Entrants. Returns "Unlimited" instead of 0.
     */
    public String getMaxWaitingEntrantsString(){
        if (maxWaitingEntrants == 0){
            return "Unlimited";
        }
        return Integer.toString(maxWaitingEntrants);
    }


    /** Answers if the event object can take another entrant on the waitList.
     * @return True if waitlist CANNOT accept 1 more Entrant, FALSE if it is able.
     */
    public boolean atCapacity() {
        // 0 == unlimited, and thus event cannot be at capacity.
        // If limit is met, we cannot fit anybody more, and are at capacity.
        if (this.maxWaitingEntrants == 0 || (this.maxWaitingEntrants <= this.waitingList.size())){
            return false;
        }
        return true;
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

    /** Moves a random selection of entrants from the waitlist to the invited list.
     * @param number How many entrants to move. Will not throw error if limit exceeded
     */
    public void InviteEntrants(int number){
        //Skip if number passed is 0
        if (number == 0){
            return;
        }

        //Will stop moving entrants if there are no more to invite
        for (int i = 0; i < number && !waitingList.isEmpty(); i++){

            //Random is not allowed on a range of 1 int
            if (waitingList.size() == 1){
                invitedList.add(waitingList.remove(0));
            }
            else{
                //Randomly choose an entrant
                int random_index = new Random().nextInt(0,waitingList.size() - 1);

                //Check if entrant not already invited. if so, remove from waitlist only.
                /*if (invitedList.contains(waitingList.get(random_index))){
                    waitingList.remove(random_index);
                }*/
                //else{
                    invitedList.add(waitingList.remove(random_index));
                //}

            }
        }
    }






                                            //-----------------------------Overrides--------------------//

    /**
     * Overriding toString's function on Events to return the name of the event instead.
     * @return Name of event
     */
    @NonNull
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


}