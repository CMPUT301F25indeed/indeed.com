package com.example.indeedgambling;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Event {
    private String eventId;
    private String eventName;
    private String description;
    private String organizerId; //Where are we getting the ID from, and how to we use to it refernce the organizer? Hashes are not unique.
    private Organizer Owner; //Xans
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
    private List<String> waitingList; // entrant IDs
    private ArrayList<Entrant> waitingEntrants; //Xans
    private ArrayList<Entrant> invitedEntrants;




     /** Creates an event. Enforces Open is before Closed with an IllegalArgumentException
      * @param EventName Title for the event
      * @param RegistrationOpen When entrants are allowed to apply
      * @param RegistrationClose When entrant applications are no longer accepted
      * @param EventStart When the actual event starts
      * @param EventEnd When the actual event ends
      * @param owner Organizer
      */
     Event(String EventName, Date RegistrationOpen, Date RegistrationClose, Date EventStart, Date EventEnd, Organizer owner){
         //Potential profanity filter.

         //Check if an exact match exists already ().
         eventName = EventName;

         //Raise error if open date after or equal end date & time
         if (RegistrationClose.before(RegistrationOpen)){
             throw new IllegalArgumentException("registrationOpen cannot be after registrationClose");
         }
         //Raise error if open date after or equal end date & time
         if (EventEnd.before(EventStart)){
             throw new IllegalArgumentException("Event Start cannot be after Event End");
         }

         registrationStart = RegistrationOpen;
         registrationEnd = RegistrationClose;

         eventStart = EventStart;
         eventEnd = EventEnd;

         waitingEntrants = new ArrayList<Entrant>();
         invitedEntrants = new ArrayList<Entrant>();

         //0 is value for no limit
         maxWaitingEntrants = 0;

         //Setting reference owner
         Owner = owner;
     }

     /** Creates an event. Enforces Open is before Closed with an IllegalArgumentException
      * @param EventName Title for the event
      * @param RegistrationOpen When entrants are allowed to apply
      * @param RegistrationClose When entrant applications are no longer accepted
      * @param EventStart When the actual event starts
      * @param EventEnd When the actual event ends
      * @param owner Organizer
      * @param MaxEntrants Maximum number of waiting entrants
      */
     Event(String EventName, Date RegistrationOpen, Date RegistrationClose, Date EventStart, Date EventEnd, Organizer owner, int MaxEntrants){
         //Potential profanity filter.

         //Check if an exact match exists already ().
         eventName = EventName;


         //Raise error if open date after or equal end date & time
         if (RegistrationClose.before(RegistrationOpen)){
             throw new IllegalArgumentException("registrationOpen cannot be after registrationClose");
         }
         //Raise error if open date after or equal end date & time
         if (EventEnd.before(EventStart)){
             throw new IllegalArgumentException("Event Start cannot be after Event End");
         }

         registrationStart = RegistrationOpen;
         registrationEnd = RegistrationClose;

         eventStart = EventStart;
         eventEnd = EventEnd;

         //TODO: Throw error if MaxEntrants < 0
         maxWaitingEntrants = MaxEntrants;

         //Setting reference owner
         Owner = owner;
     }


     //No arg constructor for Firebase
     Event(){}
     /**
      * Returns a set of all entrants on the waiting list
      * @return Entrants???
      */
     public ArrayList<Entrant> getWaitingEntrants() {
         return waitingEntrants;
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

     public void setWaitingEntrants(ArrayList<Entrant> waitingEntrants) {
         this.waitingEntrants = waitingEntrants;
     }

     public void setInvitedEntrants(ArrayList<Entrant> invitedEntrants) {
         this.invitedEntrants = invitedEntrants;
     }

     public int getMaxWaitingEntrants() {
         return maxWaitingEntrants;
     }

     public void setMaxWaitingEntrants(int maxWaitingEntrants) {
         this.maxWaitingEntrants = maxWaitingEntrants;
     }

     public ArrayList<Entrant> getInvitedEntrants() {
         return invitedEntrants;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(List<String> waitingList) {
        this.waitingList = waitingList;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    /** Adds an entrant to the waiting list of the event.
      * Duplicant signees are not added.
      * @param signee Entrant object type.
      * @return True if entrant added, False otherwise
      */
     public boolean addToWaitingList(Entrant signee){
         if (!this.atCapacity()){
             waitingEntrants.add(signee);
             //Push to cloud.
             return true;
         }
         else{
             Log.d("Event Debug", "addToWaitingList: " + "Event full");
             return false;
         }
     }

     /** Updates the current maximum number of signees.
      * Enforces positive values, minimum is 0
      * @param max New maximum number of entrants for waiting list
      */
     public void setMaxEntrants(int max){
         //No negative max. 0 is unlimited entrants
         maxWaitingEntrants = Math.max(max, 0);

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

     /** Overriding toString's function on Events to return the name of the event instead.
      * @return Name of event
      */
     @Override
     public String toString(){
         return eventName;
     }


     /** Overriding equals function for Events to compare name only
      * @param obj Object to compare
      * @return True if they match names
      */
     @Override
     public boolean equals(Object obj){
         //If perfectly equal, return true
         if (this == obj) return true;

         //If types do not match, return false
         if (obj == null || this.getClass() != obj.getClass()) return false;

         return this.getEventName().equals(((Event) obj).eventName);

     }

     /** Returns true if the WaitingList is out of room for entrants
      * @return True if waitlist CANNOT accept any more entrants
      */
     public boolean atCapacity(){
         //0 check is for no-limit. 0 => no limit
         return (this.maxWaitingEntrants >= this.waitingEntrants.size()) || this.maxWaitingEntrants == 0;
     }

     /** Returns true if the Event registration period is open now.
      * @return True if Registration Open
      */
     public boolean RegistrationOpen(){
         return (new Date().before(this.registrationEnd) && new Date().after(this.registrationStart));
     }

     /** Checks if the event Registration is open AND if waitlist has room.
      * @return True if accepting waitlist Entrants, False otherwise
      */
     public boolean waitList_registrable(){
         return (!this.atCapacity()) && this.RegistrationOpen();

     }
 }
