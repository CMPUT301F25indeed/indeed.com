package com.example.indeedgambling;

import java.util.Date;

/**
 * Represents an invitation for an entrant to participate in an event.
 *
 * This object connects:
 * - Event (eventId)
 * - Entrant (entrantId)
 * - Organizer (organizerId)
 *
 * At the end of the lottery process, users in the waiting list may receive
 * an invitation. They can choose to accept or decline the invitation.
 *
 * Fields:
 * - eventId: ID of the event the invitation belongs to
 * - entrantId: ID of the user receiving the invite
 * - organizerId: ID of the organizer who owns the event
 * - status: invitation status ("pending", "accepted", "declined", etc.)
 * - responded: whether the entrant has responded or not
 * - joinedAt: when the entrant entered the waiting list / got invite
 * - updatedAt: when the entrant responded or status last changed
 *
 * Used via Firebase: documents stored under "invitations" collection.
 */
public class Invitation {

    private String eventId;
    private String entrantId;
    private String organizerId;
    private String status;
    private boolean responded;
    private Date joinedAt;
    private Date updatedAt;

    /** Default constructor required for Firestore serialization */
    public Invitation() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEntrantId() { return entrantId; }
    public void setEntrantId(String entrantId) { this.entrantId = entrantId; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isResponded() { return responded; }
    public void setResponded(boolean responded) { this.responded = responded; }

    public Date getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
