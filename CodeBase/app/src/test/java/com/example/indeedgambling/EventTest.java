package com.example.indeedgambling;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the Event model class.
 * (Covers part of US 01.01.03 and US 01.06.02)
 */
public class EventTest {


    @Test
    public void testEventCreationAndGetters() {
        Event event = new Event("E001", "Sample Event", "A fun event for testing");

        assertEquals("E001", event.getEventId());
        assertEquals("Sample Event", event.getEventName());
        assertEquals("A fun event for testing", event.getDescription());
    }

    @Test
    public void testSetters() {
        Event event = new Event();
        event.setEventId("E002");
        event.setEventName("New Event");
        event.setDescription("Updated description");

        assertEquals("E002", event.getEventId());
        assertEquals("New Event", event.getEventName());
        assertEquals("Updated description", event.getDescription());
    }


}
