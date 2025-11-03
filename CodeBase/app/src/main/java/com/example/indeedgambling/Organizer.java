package com.example.indeedgambling;

public class Organizer extends Profile {
    public Organizer(String number, String billyBob, String mail, String id) { super(); }

    public Organizer(String profileId, String name, String email, String phone, String passwordHash) {
        super(profileId, name, email, phone, "organizer", passwordHash);
        setRoleVerified(false);
    }
    public Organizer(){super();}
}
