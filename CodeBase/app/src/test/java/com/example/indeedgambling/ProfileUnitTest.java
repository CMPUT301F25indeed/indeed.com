package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

/**
 * Basic profile model tests (JUnit 5)
 */
class ProfileUnitTest {

    @Test
    void testProfileCreationAndDefaults() {
        Profile p = new Profile("id123", "Amrit", "amrit@mail.com", "123456", "entrant", "hash123");

        assertEquals("Amrit", p.getPersonName());
        assertEquals("entrant", p.getRole());
        assertTrue(p.isNotificationsEnabled());
        assertFalse(p.isRoleVerified());
    }

    @Test
    void testSetAndGetEventsJoined() {
        Profile p = new Profile();
        p.setEventsJoined(Arrays.asList("E1", "E2"));
        assertEquals(2, p.getEventsJoined().size());
        assertTrue(p.getEventsJoined().contains("E1"));
    }
}
