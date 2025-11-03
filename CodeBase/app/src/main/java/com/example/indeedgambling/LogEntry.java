package com.example.indeedgambling;

import java.util.Date;

/**
 * Represents a log entry for audit and tracking purposes in the system.
 *
 * Logs are stored to keep track of important user actions such as:
 * - Event creation, update, or deletion
 * - User joining or leaving a waitlist
 * - Invitations sent or responded to
 * - Notifications sent
 *
 * This helps Administrators monitor system activity and debug issues.
 *
 * Fields:
 * - action: short name of the action taken (example: "CREATE_EVENT", "JOIN_WAITLIST")
 * - actorId: ID of the user performing the action
 * - targetId: ID of the affected object or user (eventId, profileId, etc.)
 * - timestamp: when the action happened
 * - details: extra information or context about the action
 *
 * Used by Firebase as an entry in the "logs" collection.
 */
public class LogEntry {

    private String action;
    private String actorId;
    private String targetId;
    private Date timestamp;
    private String details;

    /** Default constructor required for Firestore serialization */
    public LogEntry() {}

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
