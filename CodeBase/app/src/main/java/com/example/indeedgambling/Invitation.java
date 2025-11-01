package com.example.indeedgambling;

import java.util.Date;

public class Invitation {
    private String eventId;
    private String entrantId;
    private String organizerId;
    private String status;     // selected/accepted/declined
    private boolean responded;
    private Date joinedAt;
    private Date updatedAt;

    public Invitation() {}

    // getters/setters...
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
