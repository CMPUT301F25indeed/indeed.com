package com.example.indeedgambling;

public class Entrant extends Profile {
    public Entrant() { super(); }
    public Entrant(String profileId, String personName, String email, String phone) {
        super(profileId, personName, email, phone, "entrant");
    }
}
