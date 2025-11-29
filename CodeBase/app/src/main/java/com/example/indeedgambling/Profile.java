package com.example.indeedgambling;

import java.util.List;

/**
 * Represents a user profile in the Event Lottery System.
 *
 * This model is synced with Firestore and stores all relevant
 * user information including identity, role, login hash, and preferences.
 *
 * Fields include:
 * - Basic identity (name, email, phone)
 * - Role (entrant / organizer / admin)
 * - Notification preference
 * - Events joined history
 * - Security fields (password hash, optional deviceId)
 *
 * Notes:
 * - Firestore requires a no-argument constructor (provided below)
 * - passwordHash stores SHA-256 hash, not plain password
 * - deviceId is reserved for future device-based authentication
 */
public class Profile {

    private String profileId;
    private String personName;
    private String email;
    private String phone;
    private String role; // entrant / organizer / admin
    private boolean notificationsEnabled;
    private List<String> eventsJoined;
    private boolean roleVerified;

    private String passwordHash; // added
    private String deviceId;     // added for device login later
    private Boolean lightModeEnabled;
    private String profileImageUrl;

    /**
     * Required empty constructor for Firestore data mapping.
     */
    public Profile() {}

    /**
     * Creates a new Profile instance.
     *
     * @param profileId Unique hash-based ID generated from email + password
     * @param personName User's display name
     * @param email User's email address
     * @param phone Optional phone number
     * @param role User role: "entrant", "organizer", or "admin"
     * @param passwordHash SHA-256 hashed password
     */
    public Profile(String profileId, String personName, String email, String phone, String role, String passwordHash) {
        this.profileId = profileId;
        this.personName = personName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.passwordHash = passwordHash;
        this.roleVerified = false;
        this.notificationsEnabled = true;
        this.lightModeEnabled = true;
    }

    // Getters & Setters

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public List<String> getEventsJoined() { return eventsJoined; }
    public void setEventsJoined(List<String> eventsJoined) { this.eventsJoined = eventsJoined; }

    public boolean isRoleVerified() { return roleVerified; }
    public void setRoleVerified(boolean roleVerified) { this.roleVerified = roleVerified; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Boolean isLightModeEnabled() {
        return lightModeEnabled;
    }

    public void setLightModeEnabled(Boolean lightModeEnabled) {
        this.lightModeEnabled = lightModeEnabled;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /** Ovverride of toString() that returns the profile name.
     * @return Profilename
     */
    @Override
    public String toString(){
        return this.personName;
    }
}


