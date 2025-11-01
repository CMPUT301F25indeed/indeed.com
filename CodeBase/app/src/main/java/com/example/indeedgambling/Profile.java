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

    public Profile() {
    }

    public Profile(String profileId, String personName, String email, String phone, String role) {
        this.profileId = profileId;
        this.personName = personName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.notificationsEnabled = true;
        this.roleVerified = false;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isRoleVerified() {
        return roleVerified;
    }

    public void setRoleVerified(boolean roleVerified) {
        this.roleVerified = roleVerified;
    }

    public List<String> getEventsJoined() {
        return eventsJoined;
    }

    public void setEventsJoined(List<String> eventsJoined) {
        this.eventsJoined = eventsJoined;
    }
}

// Tj commented out Xan code from Xan branch
// import android.util.Log;

// import androidx.annotation.NonNull;

// public class Profile {
//     public String profileName;
//     private String password;

//     public String getProfileName() {
//         return profileName;
//     }

//     public void setProfileName(String profileName) {
//         this.profileName = profileName;
//     }

//     public String getPassword() {
//         return password;
//     }

//     public void setPassword(String password) {
//         this.password = password;
//     }

//     public Profile(String password, String profileName) {
//         this.password = password;
//         this.profileName = profileName;
//     }

//     //No arg constructor for Firebase
//     public Profile(){}


//     /** Overriding hashcode() to add the hash of profileName and password together
//      *  Does not return a unique value, as all hashes do.
//      * @return
//      */
//     @Override
//     public int hashCode(){
//         //If same sign, subtract.
//         //Opposite sign, add.
//         // This should ensure we are within the valid int range.
//         int ProfHash = this.profileName.hashCode();
//         int PassHash = this.password.hashCode();

//         //Same sign
//         if (ProfHash * PassHash > 0){
//             return ProfHash - PassHash;
//         } else{
//             return ProfHash + PassHash;
//         }
//     }

//     //TODO: Equals function to use in Firebase Contains()


//     /** Ovverride of toString() that returns the profile name.
//      * @return Profilename
//      */
//     @NonNull
//     @Override
//     public String toString(){
//         return this.profileName;
//     }
// }
