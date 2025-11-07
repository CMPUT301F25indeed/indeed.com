package com.example.indeedgambling;

import java.util.Date;

public class Notification {
    private String title;
    private String message;

    private String id;
    private String recipientId;
    private String eventId;
    private String type;  // "invitation", "result", etc.
    private String senderId;
    private String receiverId;
    private Date timestamp;

    private String profileId;


    public Notification() {
        // empty constructor for Firestore
    }

    public Notification(String title, String message, String recipientId, String eventId, String type) {
        this.title = title;
        this.message = message;
        this.recipientId = recipientId;
        this.eventId = eventId;
        this.type = type;
        this.profileId = profileId;
    }

    // ----- Getters -----
    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }


    /**
     * Returns the document ID of the notification.
     * @return The notification ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the document ID for the notification.
     * This is typically used after fetching the document from Firestore.
     * @param id The document ID from Firestore.
     */
    public void setId(String id) {
        this.id = id;
    }


    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }



    public String getRecipientId() {
        return recipientId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getType() {
        return type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    // ----- Setters -----
    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
