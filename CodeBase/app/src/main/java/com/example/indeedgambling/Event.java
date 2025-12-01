package com.example.indeedgambling;

import android.graphics.Bitmap;
import android.util.Pair;

import androidx.annotation.NonNull;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/** Represents an event in spacetime that entrants can sign up to, and can be invited to.
 */
public class Event implements Serializable {
    private String eventId;
    private String eventName;
    private String description;
    private String organizerId;
    private String category;
    private double latitude;
    private double longitude;
    private boolean registrationRadiusEnabled;
    private double registerableRadius;
    private Date eventStart;
    private Date eventEnd;
    private Date registrationStart;
    private Date registrationEnd;
    //US: 02.03.01
    private int maxWaitingEntrants;
    private String imageUrl;
    //private String qrCodeURL;
    private String status;    // planned/open/closed/completed
    private String criteria;  // lottery notes
    private ArrayList<String> waitingList = new ArrayList<String>(); // entrant IDs who signed up
    private ArrayList<String> invitedList = new ArrayList<String>(); // Entrant IDs who were randomly selected
    private ArrayList<String> lostList = new ArrayList<>(); //Entrant Ids who were not randomly selected
    private ArrayList<String> cancelledEntrants = new ArrayList<String>(); //invited entrants who declined or were removed
    private ArrayList<String> acceptedEntrants = new ArrayList<String>(); //invited entrants who accepted

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
     * @param Location          Location of the event
     */
    Event(String EventName, Date RegistrationOpen, Date RegistrationClose, Date EventStart, Date EventEnd, String OrgID, String Description, String Critera, String Category, String QRCodeURL, String Location) {
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

        //qrCodeURL = QRCodeURL;

        this.status = getStatus();

        //Event ID : Hash of OrgIdEventnameEventstart
        this.eventId = new HashUtil().sha256(organizerId.concat(eventName).concat(EventStart.toString()));
    }

    /** No MaxEntrant, no Location constructor. DO NOT USE, as it does not include mandatory location.
     *
     * @param EventName
     * @param RegistrationOpen
     * @param RegistrationClose
     * @param EventStart
     * @param EventEnd
     * @param OrgID
     * @param Description
     * @param Critera
     * @param Category
     * @param QRCodeURL
     * @param MaxEntrants
     */
    @Deprecated
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

        //qrCodeURL = QRCodeURL;

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

        //qrCodeURL = "";

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

    /** Sets the EventID. NOT RECOMMENED, as IDs are key to accessing events, and IDs are generated.
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

    /** Overwrites the Events Description with the arguments'
     * @param description New description for event.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Returns the current Event's owner's ID.
     * @return Organizer ID String
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /** Overwrites the organizer's ID with the arugment's.
     * @param organizerId New OrgID.
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /** Provides the event's saved category.
     * @return Category of event; a string
     */
    public String getCategory() {
        return category;
    }

    /** Overwrites the event's category with the arguments, no questions asked.
     * @param category new Category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /** Returns the URL where the Image is saved.
     * @return String for the URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /** Updates the URL for the event. Does not affect server.
     * @param imageUrl New URL for the event
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /** Returns the string of the URL for the QR.
     * @return QR URL string
     */
//    public String getQrCodeURL() {
//        return qrCodeURL;
//    }
//
//    /** Updates the QRCodeURL
//     * @param qrCodeURL
//     */
    public Bitmap generateQRCode() {
        QRCodeGenerator qrService = new QRCodeGenerator();
        return qrService.generateEventQRCode(this);
    }
//    public void setQrCodeURL(String qrCodeURL) {
//        this.qrCodeURL = qrCodeURL;
//    }

    /** Updates the status of the event to the current moment before returning.
     * Registration not yet open: Planned
     * Registration Open and not yet closed: Open
     * After Registration end and before Event end: Closed
     * After Event End: Completed
     * Otherwise: Unknown
     * @return Current Status of event, based on the above
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

    /** Sets the status of the event to the passed argument.
     * @param status new Status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Returns a list of EntrantIDs who signed up to the waitlist
     * @return List of EntrantID strings
     */
    public List<String> getWaitingList() {
        return waitingList;
    }

    /** Overwrites the waitinglist of the event with the argument. Does not affect serverside.
     * @param waitingList What overwrites the event's waiting list
     */
    public void setWaitingList(ArrayList<String> waitingList) {
        this.waitingList = waitingList;
    }

    /** Returns the saved criteria of the event
     * @return String of the Criteria
     */
    public String getCriteria() {
        return criteria;
    }

    /** Change the criteria needed to sign up to the event
     * @param criteria new critera. Overwrites the old.
     */
    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    /** The open datetime of the event is returned
     * @return Datetime, start of event
     */
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

    public ArrayList<String> getLostList() {
        return lostList;
    }

    public void setLostList(ArrayList<String> lostList) {
        this.lostList = lostList;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        checkLatitudeValid(latitude);
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        checkLongitudeValid(longitude);
        this.longitude = longitude;
    }

    public double getregisterableRadius() {
        return registerableRadius;
    }

    /** Updates the radius, in meters. Uses absolute values for arguments
     * @param registerableRadius Distance in Meters to allow for signup.
     */
    public void setregisterableRadius(double registerableRadius) {
        this.registerableRadius = Math.abs(registerableRadius);
    }

    public boolean isregistrationRadiusEnabled() {
        return registrationRadiusEnabled;
    }

    public void setregistrationRadiusEnabled(boolean registrationRadiusEnabled) {
        this.registrationRadiusEnabled = registrationRadiusEnabled;
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
    @com.google.firebase.firestore.Exclude //Prevents adding the result of the string to Firebase
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

        //If our max is at or larger than the size of the list.
        //If our limit is unset

        //No matter what, with 0 being unlimited, we are never at capacity.
        if (this.maxWaitingEntrants == 0){
            return false;
        }

        //If our waitinglist is as big as our limit, we are at capacity
        return this.waitingList.size() >= this.maxWaitingEntrants;
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

    /** Also checks the locational requirements for waitlisting
     * @param latitude
     * @param longitude
     * @return True if possible, False otherwise
     */
    public boolean waitlist_registerable(double latitude, double longitude) {
        return waitList_registrable() && coordinates_in_range(latitude,longitude);
    }

    /** Checks if the passed coordinates are in range using Haversin formula
     * @param latitude Must be in [-90, +90] degrees
     * @param longitude Must be in [-180, 180] degrees
     * @return True if coordinates in range, or if the range requirement is disabled.
     */
    public boolean coordinates_in_range(double latitude, double longitude) {
        //If there is no enforcement on radius, it cannot be out of range
        if (!isregistrationRadiusEnabled()){
          return true;
        }

        //Throw error if coordinates not valid
        checkLatitudeValid(latitude);
        checkLongitudeValid(longitude);

        final double R = 6371000; // Earth radius in meters

        double dLat = Math.toRadians(latitude - this.latitude);
        double dLon = Math.toRadians(longitude - this.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double distance = R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return distance <= registerableRadius;
    }

    /** Moves a random selection of entrants from the waitlist/lostlist to the invited list. DOES NOT TALK TO SERVER DIRECTLY
     * Entrants who were not selected get moved to the lostlist, or remain there.
     * @param number How many entrants to move. Will not throw error if limit exceeded, just stops.
     */
    public void InviteEntrants(int number){
        //Skip if number passed is 0
        if (number == 0){
            return;
        }


        //Viable-to-invite
        ArrayList<String> viabletoInvite = new ArrayList<String>();
        //Need duplicate checks
        viabletoInvite.addAll(waitingList);
        viabletoInvite.addAll(lostList);

        //Remove all from both, and all to lostList after
        waitingList.clear();
        lostList.clear();

        //Will stop moving entrants if there are no more to invite
        for (int i = 0; i < number && !viabletoInvite.isEmpty(); i++){

            //Random is not allowed on a range of 1 int
            if (viabletoInvite.size() == 1){
                invitedList.add(viabletoInvite.remove(0));
            }
            else{
                //Randomly choose an entrant
                int random_index = new Random().nextInt(0,viabletoInvite.size() - 1);
                invitedList.add(viabletoInvite.remove(random_index));
            }
        }

        //Putting all remaining people to the lostlist.
        lostList.addAll(viabletoInvite);
    }

    /** Attempts to add a ID string to the waitingList. Will not if at capacity. DOES NOT INTERACT WITH SERVER
     *  Does NOT check coordinates
     * @param entrantID ID to try to add
     * @return TRUE if added, FALSE if not.
     */
    public boolean tryaddtoWaitingList(String entrantID){
        if (!atCapacity() && this.RegistrationOpen()){
            this.waitingList.add(entrantID);
            return true;
        }
        return false;
    }

    /** Ends registration now. Does not do any checks for goodness or graciousness.
     */
    public void endRegistration(){
        //Checks if registration ends in the past. Only updates if it would end in the future.

        if (registrationEnd.after(new Date())){
            this.registrationEnd = new Date();
        }
    }

    /** Updates an Events coordinates in degrees to the passed location.
     * @param lat Must be in [-90, +90] degrees
     * @param lng Must be in [-180, 180] degrees
     */
    public void setLocation(double lat, double lng){
        //Throw error if coordinates not valid
        checkLatitudeValid(lat);
        checkLongitudeValid(lng);
        longitude = lng;
        latitude = lat;
    }

    @com.google.firebase.firestore.Exclude
    public Pair<Double,Double> getLocation(){
        return new Pair<>(latitude,longitude);
    }


    @com.google.firebase.firestore.Exclude
    public String getLocationString(){
        return Double.toString(latitude).concat(", ".concat(Double.toString(longitude)));
    }

    /** Argument validity helper
     * Throws an argument if latitude is out of range
     */
    @com.google.firebase.firestore.Exclude
    private void checkLatitudeValid(double lat){
        if (Math.abs(lat) > 90){
            throw new IllegalArgumentException("Latitude must be between -90 and +90 degrees!".concat(this.eventId));
        }
    }

    /** Argument validity helper
     * Throws an argument if longitude is out of range
     * @param lng
     */
    private void checkLongitudeValid(double lng){
        if (Math.abs(lng) > 180){
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees!".concat(this.eventId));
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
        //If registration is over, display so
        if (this.registrationEnd.before(new Date())){
            return "\uD83D\uDCDD❌".concat(" | ").concat(eventName);
        }
        else{
            return "\uD83D\uDCDD✅".concat(" | ").concat(eventName);
        }
        //return eventName;//.concat(" : ".concat(Integer.toString(waitingList.size()).concat(" / ").concat(getMaxWaitingEntrantsString())));
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

    public String whichList(String profileID) {

        if (acceptedEntrants.contains(profileID)) {
            return "accepted";
        }

        if (cancelledEntrants.contains(profileID)) {
            return "cancelled";
        }

        if (invitedList.contains(profileID)) {
            return "invited";
        }

        if (waitingList.contains(profileID)) {
            return "waitlist";
        }
        return "none";
    }




}
