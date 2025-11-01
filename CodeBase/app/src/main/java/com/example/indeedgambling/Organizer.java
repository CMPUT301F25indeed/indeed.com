package com.example.indeedgambling;

public class Organizer extends Profile {
    public Organizer() { super(); }
    public Organizer(String profileId, String personName, String email, String phone) {
        super(profileId, personName, email, phone, "organizer");
    }
}
