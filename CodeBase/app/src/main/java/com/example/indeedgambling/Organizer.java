package com.example.indeedgambling;

/**
 * Represents an Organizer user in the Event Lottery System.
 *
 * An Organizer can create and manage events. This class extends {@link Profile}
 * and sets the user role to "organizer".
 *
 * Notes:
 * - Firestore requires a no-argument constructor (included)
 * - roleVerified defaults to false until admin verification
 */
public class Organizer extends Profile {

    /**
     * Legacy / placeholder constructor (not used in production logic).
     * Firestore still requires it to exist when deserializing objects.
     *
     * @param number unused placeholder argument
     * @param billyBob unused placeholder argument
     * @param mail unused placeholder argument
     * @param id unused placeholder argument
     */
    public Organizer(String number, String billyBob, String mail, String id) { super(); }

    /**
     * Creates a new Organizer profile.
     *
     * @param profileId Unique profile ID (typically SHA-256 hash of email+password)
     * @param name Organizer's display name
     * @param email Organizer email
     * @param phone Organizer phone number
     * @param passwordHash SHA-256 hashed password
     */
    public Organizer(String profileId, String name, String email, String phone, String passwordHash) {
        super(profileId, name, email, phone, "organizer", passwordHash);
        setRoleVerified(false); // Organizer accounts require verification
    }

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public Organizer(){ super(); }
}
