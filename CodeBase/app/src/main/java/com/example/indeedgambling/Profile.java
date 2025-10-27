package com.example.indeedgambling;

public class Profile {
    public String profileName;
    private String password;

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Profile(String password, String profileName) {
        this.password = password;
        this.profileName = profileName;
    }
}
