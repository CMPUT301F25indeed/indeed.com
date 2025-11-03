package com.example.indeedgambling;

import java.util.Date;

/**
 * Represents metadata for an image uploaded to the system.
 *
 * This class does not store the actual image file, only the metadata:
 * - eventId: ID of the event the image relates to (if applicable)
 * - uploaderId: ID of the user who uploaded the image
 * - url: where the image file is stored (ex: Firebase Storage URL)
 * - uploadedAt: timestamp when the image was uploaded
 * - approved: indicates whether the image has been reviewed and approved (ex: by admin/organizer)
 *
 * Firestore requires a no-argument constructor.
 *
 * Usage:
 * - Used for storing profile pictures, event posters, or proof images if required by the system.
 * - Usually paired with Firebase Storage for real file storage.
 *
 * Accessed via FirebaseViewModel using saveImageMeta() method.
 */
public class ImageUpload {

    private String eventId;
    private String uploaderId;
    private String url;
    private Date uploadedAt;
    private boolean approved;

    /** Required empty constructor for Firestore deserialization */
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
