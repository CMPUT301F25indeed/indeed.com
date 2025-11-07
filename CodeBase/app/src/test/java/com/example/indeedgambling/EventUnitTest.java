package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class EventUnitTest {

    /** No waitlist limit
     *  Valid times set.
     *  ORGID = ORG1
     * @return
     */
    Event MockEvent(){
        Date now = new Date();
        Date past = new Date(now.getTime() - 1);
        Date later = new Date(now.getTime() + 10000);
        Event ReturnEvent =  new Event("SampleEvent", past, later, past, later, "ORG1", "desc", "criteria", "Music", "qr.png", "location");

        //Phony waitlist implementon next
        ArrayList<String> waitlist = new ArrayList<>();
        waitlist.add("1");
        waitlist.add("2");
        waitlist.add("3");
        waitlist.add("4");
        waitlist.add("5");
        waitlist.add("6");
        ReturnEvent.setWaitingList(waitlist);
        return ReturnEvent;
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

    //US 02.03.01 - As an organizer, I want to optionally limit the number of entrants who can join my waiting list
    @Test
    void testWaitlistLimit(){
        Event e = MockEvent();
        assertTrue(e.tryaddtoWaitingList("7"));

        e.setMaxWaitingEntrants(7);
        assertFalse(e.tryaddtoWaitingList("8"));

        assertFalse(e.tryaddtoWaitingList("9"));

        e.setMaxWaitingEntrants(0);
        assertTrue(e.tryaddtoWaitingList("10"));
    }

    //US 02.01.04 - As an organizer, I want to set a registration period
    @Test
    void testRegistrationBounds(){
        Date now = new Date();
        Event e = MockEvent();

        assertTrue(e.tryaddtoWaitingList("7"));


        //Setting new periods
        e.setRegistrationEnd(new Date(now.getTime() - 100));
        e.setRegistrationStart(new Date(now.getTime() - 150));
        e.setMaxWaitingEntrants(0);

        assertFalse(e.tryaddtoWaitingList("8"));
    }

    //US 02.02.01 - As an organizer, I want to view the list of entrants who joined my event waiting list
    //This tests the IDS, which are used to get the entrants.
    @Test
    void testWaitList(){
        Event e = MockEvent();
        ArrayList<String> entrantIDs = new ArrayList<>();
        //Same as mock object
        entrantIDs.add("1");
        entrantIDs.add("2");
        entrantIDs.add("3");
        entrantIDs.add("4");
        entrantIDs.add("5");
        entrantIDs.add("6");

        assertEquals(entrantIDs,e.getWaitingList());
    }

    @Test
    void testInvitation(){
        Event e = MockEvent();
        int listSize = 6;

            //0 Case
        e.InviteEntrants(0);

        //Ensure 2 lists add to old size.
        assertEquals(6,e.getWaitingList().size() + e.getInvitedList().size());


        //Test invitation has the same entrants as before
        List<String> holder = e.getWaitingList();
        holder.addAll(e.getInvitedList());

        //Fresh mockevents waitinglist contians all elements as the old one's waiting list + invite list
        assertTrue(MockEvent().getWaitingList().containsAll(holder));


            //1 Case
        e = MockEvent();
        e.InviteEntrants(1);

        //Ensure 2 lists add to old size.
        assertEquals(6,e.getWaitingList().size() + e.getInvitedList().size());


        //Test invitation has the same entrants as before
        holder = e.getWaitingList();
        holder.addAll(e.getInvitedList());

        //Fresh mockevents waitinglist contians all elements as the old one's waiting list + invite list
        assertTrue(MockEvent().getWaitingList().containsAll(holder));


        //Test that the person made it over.
        e = MockEvent();
        ArrayList<String> PersList = new ArrayList<>();
        PersList.add("testo");

        e.setWaitingList(PersList);
        e.InviteEntrants(1);

        assertEquals(e.getInvitedList().get(0),"testo");
        

        //Lots case
        e = MockEvent();
        e.InviteEntrants(5);

        //Ensure 2 lists add to old size.
        assertEquals(6,e.getWaitingList().size() + e.getInvitedList().size());


        //Test invitation has the same entrants as before
        holder = e.getWaitingList();
        holder.addAll(e.getInvitedList());

        //Fresh mockevents waitinglist contians all elements as the old one's waiting list + invite list
        assertTrue(MockEvent().getWaitingList().containsAll(holder));




            //1 person case



    }

    @Test
    void testInviteList(){
        Event e = MockEvent();
        ArrayList<String> entrantIDs = new ArrayList<>();
        //Same as mock object
        entrantIDs.add("1");
        entrantIDs.add("2");
        entrantIDs.add("3");
        entrantIDs.add("4");
        entrantIDs.add("5");
        entrantIDs.add("6");

        assertNotEquals(entrantIDs,e.getInvitedList());

        //Nobody invited yet
        assertEquals(0,e.getInvitedList().size());


    }
}
