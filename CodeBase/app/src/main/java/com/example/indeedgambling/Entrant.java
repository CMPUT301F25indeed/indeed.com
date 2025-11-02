package com.example.indeedgambling;

public class Entrant extends Profile {
    public Entrant() { super(); }

    public Entrant(String profileId, String name, String email, String phone, String passwordHash) {
        super(profileId, name, email, phone, "entrant", passwordHash);
        setRoleVerified(true);
    }
}
