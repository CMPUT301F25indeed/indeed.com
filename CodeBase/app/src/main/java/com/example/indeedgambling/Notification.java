package com.example.indeedgambling;

import java.util.Date;

public class Notification {

    private String notificationId;   // doc id in Firestore
    private String senderId;         // organizer/admin/entrant
    private String receiverId;       // user id
    private String eventId;          // optional (for event notifications)
    private String type;             // win/lose/broadcast/custom
    private String message;          // text
    private Date timestamp;          // when sent
    private boolean read;            // for checking if user opened it

    public Notification() {}

    public Notification(String notificationId, String senderId, String receiverId, String eventId,
                        String type, String message, Date timestamp, boolean read) {
        this.notificationId = notificationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.eventId = eventId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

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

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
