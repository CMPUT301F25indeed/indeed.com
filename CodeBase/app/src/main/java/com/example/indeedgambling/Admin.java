package com.example.indeedgambling;

/**
 * Represents an Admin user in the Event Lottery System.
 *
 * Admins have the highest level of access:
 * - Can verify organizer accounts
 * - Can manage system-wide logs / user roles
 * - Can review events if needed
 *
 * This class extends {@link Profile} and forces the role to "admin".
 * Admin accounts are always role-verified by default.
 */
public class Admin extends Profile {

    /**
     * Default constructor required for Firestore deserialization.
     * Automatically sets:
     * - role = "admin"
     * - roleVerified = true (admin accounts are trusted)
     */
    public Admin() { super(); setRole("admin"); setRoleVerified(true); }

    /**
     * Creates a new Admin profile with required details.
     *
     * @param profileId Unique profile ID (usually hashed email+password)
     * @param personName Admin name
     * @param email Admin email
     * @param phone Admin phone number
     * @param passwordHash SHA-256 hashed password for secure backend storage
     */
    public Admin(String profileId, String personName, String email, String phone, String passwordHash) {
        super(profileId, personName, email, phone, "admin", passwordHash);
        setRoleVerified(true);
    }
}
