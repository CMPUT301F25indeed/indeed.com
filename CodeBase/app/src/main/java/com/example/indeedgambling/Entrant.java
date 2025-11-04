package com.example.indeedgambling;

/**
 * Represents an Entrant user in the Event Lottery System.
 *
 * This class extends {@link Profile} and automatically sets
 * the role to "entrant".
 *
 * Notes:
 * - Firestore requires a no-argument constructor (provided)
 * - roleVerified is set to true by default for entrants
 * - Used when mapping profile data to an Entrant-specific object
 */
public class Entrant extends Profile {

    /**
     * Empty constructor required for Firestore data mapping.
     */
    public Entrant() { super(); }

    /**
     * Creates a new Entrant profile.
     *
     * @param profileId Unique profile ID (usually SHA-256 hash of email+password)
     * @param name Display name of entrant
     * @param email Email of entrant
     * @param phone Optional contact number
     * @param passwordHash SHA-256 hashed password
     */
    public Entrant(String profileId, String name, String email, String phone, String passwordHash) {
        super(profileId, name, email, phone, "entrant", passwordHash);
        setRoleVerified(true);
    }
}
