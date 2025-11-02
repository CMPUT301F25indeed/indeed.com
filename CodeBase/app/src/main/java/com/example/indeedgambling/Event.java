package com.example.indeedgambling;

import java.util.Date;
import java.util.List;

public class Event {

    private String eventId;
    private String title;
    private String description;
    private String organizerId;
    private String category;
    private String location;
    private Date startDate;
    private Date endDate;
    private Date registrationStart;
    private Date registrationEnd;
    private int capacity;
    private String imageUrl;
    private String qrCodeURL;
    private String status;
    private String criteria;
    private List<String> waitingList;

    public Event() {}

    // Short constructor for creating new events inside the app
    public Event(String title,
                 String description,
                 String organizerId,
                 String category,
                 String location,
                 Date startDate,
                 Date endDate,
                 Date registrationStart,
                 Date registrationEnd,
                 int capacity) {

        this.eventId = null;
        this.title = title;
        this.description = description;
        this.organizerId = organizerId;
        this.category = category;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.capacity = capacity;

        this.imageUrl = "";
        this.qrCodeURL = "";
        this.status = "open";
        this.criteria = "";
        this.waitingList = new java.util.ArrayList<>();
    }

    // Full constructor (Firestore)
    public Event(String eventId,
                 String title,
                 String description,
                 String organizerId,
                 String category,
                 String location,
                 Date startDate,
                 Date endDate,
                 Date registrationStart,
                 Date registrationEnd,
                 int capacity,
                 String imageUrl,
                 String qrCodeURL,
                 String status,
                 String criteria,
                 List<String> waitingList) {

        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.organizerId = organizerId;
        this.category = category;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.capacity = capacity;
        this.imageUrl = imageUrl;
        this.qrCodeURL = qrCodeURL;
        this.status = status;
        this.criteria = criteria;
        this.waitingList = waitingList;
    }

    // getters + setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Date getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(Date registrationStart) { this.registrationStart = registrationStart; }

    public Date getRegistrationEnd() { return registrationEnd; }
    public void setRegistrationEnd(Date registrationEnd) { this.registrationEnd = registrationEnd; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getQrCodeURL() { return qrCodeURL; }
    public void setQrCodeURL(String qrCodeURL) { this.qrCodeURL = qrCodeURL; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCriteria() { return criteria; }
    public void setCriteria(String criteria) { this.criteria = criteria; }

    public List<String> getWaitingList() { return waitingList; }
    public void setWaitingList(List<String> waitingList) { this.waitingList = waitingList; }
}
