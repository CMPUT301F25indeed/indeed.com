package com.example.indeedgambling;

public class Admin extends Profile {
    public Admin() { super(); }
    public Admin(String profileId, String personName, String email, String phone) {
        super(profileId, personName, email, phone, "admin");
    }
}
