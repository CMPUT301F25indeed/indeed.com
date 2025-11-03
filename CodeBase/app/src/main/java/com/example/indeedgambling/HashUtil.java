package com.example.indeedgambling;

import java.security.MessageDigest;

/**
 * Utility class for hashing operations used in the app.
 *
 * Main Purpose:
 * - Securely hash user credentials for ID and password storage.
 * - Avoid storing raw passwords or predictable document IDs.
 *
 * Current Usage:
 * - SHA-256 hash is used for:
 *   1) Generating secure profile document IDs in Firestore
 *   2) Storing hashed passwords instead of plain text
 *
 * Notes:
 * - SHA-256 outputs a 64-character hex string (256-bit security)
 * - Collision probability is extremely low, safe for our app scale
 */
public class HashUtil {

    /**
     * Applies SHA-256 hashing to the given input string.
     *
     * @param input The text to hash
     * @return A 64-character hex string representing the SHA-256 output.
     *         Returns an empty string if hashing fails.
     */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Generates a unique user ID by hashing both email and password.
     *
     * Why?
     * - Ensures every user has a cryptographically strong identifier
     * - Prevents others from guessing user document IDs
     *
     * @param email User's email
     * @param password User's password
     * @return SHA-256 hash used as Firestore document ID
     */
    public static String generateId(String email, String password) {
        return sha256(email + password);
    }
}
