package com.example.indeedgambling;

import java.util.Date;

/**
 * Represents an in-app notification between users in the system.
 *
 * Notifications are stored in Firestore under the "notifications" collection.
 * These help the system inform entrants, organizers, or admins about important updates.
 *
 * Common notification examples:
 * - Organizer invites entrant to an event
 * - Entrant receives acceptance or rejection
 * - System reminders about deadlines or actions
 *
 * Fields:
 * - senderId: ID of the user sending the notification (could be system or organizer)
 * - receiverId: ID of the user receiving the notification
 * - eventId: ID of the related event (if applicable)
 * - type: category of the notification (ex: "INVITE", "ACCEPTED", "REMINDER")
 * - message: human-readable message text
 * - timestamp: when the notification was created
 *
 * Firestore requires a no-argument constructor which is included below.
 */
public class Notification {

    private String senderId;
    private String receiverId;
    private String eventId;
    private String type;
    private String message;
    private Date timestamp;

    /** Required empty constructor for Firestore serialization */
    public Notification() {}

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
