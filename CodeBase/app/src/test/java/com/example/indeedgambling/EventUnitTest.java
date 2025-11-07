package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class EventUnitTest {

    @Test
    void testToStringReturnsName() {
        Date now = new Date();
        Date later = new Date(now.getTime() + 10000);
        Event e = new Event("SampleEvent", now, later, now, later, "ORG1", "desc", "criteria", "Music", "qr.png");
        assertEquals("SampleEvent", e.toString());
    }

    @Test
    void testAtCapacityFalseWhenUnlimited() {
        Event e = new Event();
        e.setMaxWaitingEntrants(0);
        e.setWaitingList(new ArrayList<>());
        assertFalse(e.atCapacity());
    }

    @Test
    void testRegistrationOpenTrue() {
        Date now = new Date();
        Event e = new Event("OpenEvent",
                new Date(now.getTime() - 1000 * 60),
                new Date(now.getTime() + 1000 * 60),
                now, new Date(now.getTime() + 1000 * 120),
                "ORG2", "desc", "criteria", "Music", "qr.png");
        assertTrue(e.RegistrationOpen());
    }
}
