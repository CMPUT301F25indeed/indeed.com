package com.example.indeedgambling;

public class Profile {
    public String ProfileName;
    private String Password;

    public String getProfileName() {
        return ProfileName;
    }

    public void setProfileName(String profileName) {
        ProfileName = profileName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public Profile(String password, String profileName) {
        Password = password;
        ProfileName = profileName;
    }
}
