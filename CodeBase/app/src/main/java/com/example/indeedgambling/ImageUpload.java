package com.example.indeedgambling;

import java.util.Date;

public class ImageUpload {

    private String eventId;
    private String uploaderId;
    private String url;
    private Date uploadedAt;
    private boolean approved;

    public ImageUpload() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Date getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Date uploadedAt) { this.uploadedAt = uploadedAt; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}
