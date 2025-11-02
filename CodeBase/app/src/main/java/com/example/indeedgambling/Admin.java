package com.example.indeedgambling;

public class Admin extends Profile {

    public Admin() { super(); setRole("admin"); setRoleVerified(true); }

    public Admin(String profileId, String personName, String email, String phone, String passwordHash) {
        super(profileId, personName, email, phone, "admin", passwordHash);
        setRoleVerified(true);
    }
}
