package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for profile behavior and settings
 */
class ProfileBehaviorTest {

    @Test
    void testNameOutputFromToString() {
        Profile p = new Profile("id12", "Amritpal", "amrit@example.com", "99999", "organizer", "passHash");
        assertEquals("Amritpal", p.toString());
    }

    @Test
    void testToggleNotifications() {
        Profile p = new Profile();
        p.setNotificationsEnabled(false);
        assertFalse(p.isNotificationsEnabled());
        p.setNotificationsEnabled(true);
        assertTrue(p.isNotificationsEnabled());
    }

    @Test
    void testRoleVerificationChange() {
        Profile p = new Profile();
        assertFalse(p.isRoleVerified());
        p.setRoleVerified(true);
        assertTrue(p.isRoleVerified());
    }
}
