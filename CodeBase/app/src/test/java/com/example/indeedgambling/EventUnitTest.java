package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class EventUnitTest {

    Event MockEvent(){
        Date now = new Date();
        Date past = new Date(now.getTime() - 1);
        Date later = new Date(now.getTime() + 10000);
        return new Event("SampleEvent", past, later, past, later, "ORG1", "desc", "criteria", "Music", "qr.png", "location");

        //Phony waitlist implementon next
    }

    @Test
    void testToStringReturnsName() {
        Event e = MockEvent();
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
        Event e = MockEvent();
        assertTrue(e.RegistrationOpen());
    }

    @Test
    void testOpenStatus(){
        Date now = new Date();
        Event e = MockEvent();
        assertEquals("Open",e.getStatus());

    }

    @Test
    void testClosedStatus(){
        Date now = new Date();
        Event e = MockEvent();
        //Setting registration in the past.
        e.setRegistrationStart(new Date(now.getTime() - 10));
        e.setRegistrationEnd(new Date(now.getTime() - 5));
        assertEquals("Closed",e.getStatus());

    }

    @Test
    void testCompletedStatus(){
        Date now = new Date();
        Date recent = new Date(now.getTime() - 5);
        Date Before = new Date(now.getTime() - 10);
        Event e = MockEvent();

        //Setting event to be ended already.
        e.setEventStart(Before);
        e.setEventEnd(recent);
        e.setRegistrationStart(Before);
        e.setRegistrationEnd(recent);

        assertEquals("Completed",e.getStatus());
    }

    @Test
    void testPlannedStatus(){
        Date now = new Date();
        Event e = MockEvent();

        //Setting registration in the future
        e.setRegistrationStart(new Date(now.getTime() + 20));
        e.setRegistrationEnd(new Date(e.getRegistrationStart().getTime() + 20));

        assertEquals("Planned",e.getStatus());
    }

    @Test
    void testUnknownStatus(){
        Date now = new Date();
        Event e = MockEvent();

        //Setting registration in the future
        e.setRegistrationEnd(new Date(now.getTime() + 20));
        e.setRegistrationStart(new Date(now.getTime() + 40));

        assertEquals("Planned",e.getStatus());
    }
}
