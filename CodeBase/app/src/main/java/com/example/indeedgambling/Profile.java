package com.example.indeedgambling;

import java.util.List;

public class Profile {

    private String profileId;            // doc id: hash(name)+hash(password)
    private String personName;
    private String email;
    private String phone;
    private String role;                 // "entrant" | "organizer" | "admin"
    private boolean notificationsEnabled;
    private boolean roleVerified;        // organizer verified flag
    private List<String> eventsJoined;   // event IDs

    public Profile() {}

    public Profile(String profileId, String personName, String email, String phone, String role) {
        this.profileId = profileId;
        this.personName = personName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.notificationsEnabled = true;
        this.roleVerified = false;
    }

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

    public boolean isRoleVerified() { return roleVerified; }
    public void setRoleVerified(boolean roleVerified) { this.roleVerified = roleVerified; }

    public List<String> getEventsJoined() { return eventsJoined; }
    public void setEventsJoined(List<String> eventsJoined) { this.eventsJoined = eventsJoined; }
}
