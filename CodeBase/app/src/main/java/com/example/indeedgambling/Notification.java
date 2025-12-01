package com.example.indeedgambling;

import java.util.Date;

/**
 * Represents an in-app notification stored in Firestore.
 *
 * Each document will now also store its Firestore document ID inside
 * the Notification object using the 'id' field. This makes deletion,
 * admin operations, and list updates 100% reliable.
 */
public class Notification {

    // Firestore document ID (VERY IMPORTANT)
    private String id;

    private String senderId;
    private String receiverId;
    private String eventId;
    private String type;
    private String message;
    private Date timestamp;
    private String senderEmail;
    private String eventName;

    /** Required empty constructor for Firestore */
    public Notification() {}

    // -------------------------
    // ID GETTER/SETTER
    // -------------------------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // -------------------------
    // Other fields
    // -------------------------
    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderEmail() {
        return senderEmail;
    }
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
