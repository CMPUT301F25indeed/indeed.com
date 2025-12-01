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
        ReturnEvent.setregisterableRadius(100);
        ReturnEvent.setLocation(0,0);
        ReturnEvent.setregistrationRadiusEnabled(true);

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
        ArrayList<String> entrantIDs = new ArrayList<>();
        //Same as mock object
        entrantIDs.add("1");
        entrantIDs.add("2");
        entrantIDs.add("3");
        entrantIDs.add("4");
        entrantIDs.add("5");
        entrantIDs.add("6");

        //Nobody invited, nobody should be invited list or lost list
        //All should still be on the waitlist
        assertNotEquals(entrantIDs,e.getInvitedList());
        assertEquals(0,e.getInvitedList().size());
        assertEquals(0,e.getLostList().size());
        assertTrue(e.getWaitingList().containsAll(entrantIDs));

        //Inviting nobody sends nobody to lost list. Same as not inviting at all
        e.InviteEntrants(0);
        assertNotEquals(entrantIDs,e.getInvitedList());
        assertEquals(0,e.getInvitedList().size());
        assertEquals(0,e.getLostList().size());
        assertTrue(e.getWaitingList().containsAll(entrantIDs));

        e = MockEvent();
        //Inviting one sends the rest to the lost list
        e.InviteEntrants(1);
        assertEquals(e.getWaitingList().size(),0);
        assertEquals(e.getLostList().size(),5);
        assertEquals(e.getInvitedList().size(),1);
        //Check that the IDs are right.
        ArrayList<String> TestList = new ArrayList<>();
        TestList.addAll(e.getLostList());
        TestList.addAll(e.getInvitedList());
        TestList.addAll(e.getWaitingList()); //Should be empty, but doing so anyways for checks
        assertTrue(TestList.containsAll(entrantIDs));
        //Checking no dupes.
        assertTrue(TestList.size() == 6);

        //Several invitations

        e = MockEvent();
        //Inviting several sends them to lost and invited
        e.InviteEntrants(4);
        assertEquals(e.getWaitingList().size(),0);
        assertEquals(e.getLostList().size(),2);
        assertEquals(e.getInvitedList().size(),4);
        //Check that the IDs are right.
        TestList.clear();
        TestList.addAll(e.getLostList());
        TestList.addAll(e.getInvitedList());
        TestList.addAll(e.getWaitingList()); //Should be empty, but doing so anyways for checks
        assertTrue(TestList.containsAll(entrantIDs));
        //Checking no dupes.
        assertTrue(TestList.size() == 6);
    }

    //Check regRadius works
    void testRegRadius(){
        Event e = MockEvent();
    }

    //Check regRadiusToggle works
    void testRegRadiusToggle(){

    }


    //Check coordinates calculations
    @Test
    void testCoordinateCalculation(){
        Event e = MockEvent();
        //78.63 meters away
        assertTrue(e.coordinates_in_range(0.0005,0.0005));

        //124.32 meters away
        assertFalse(e.coordinates_in_range(0.001,0.0005));

        //99.3 meters away
        assertTrue(e.coordinates_in_range(0.00074,0.0005));

        //100.08 meters away
        assertFalse(e.coordinates_in_range(0,0.0009));


        e.setLocation(10,10);
        //Check previous locations are false

        //1,568,477 meters away
        assertFalse(e.coordinates_in_range(0.0005,0.0005));

        //1,568,402 meters away
        assertFalse(e.coordinates_in_range(0.001,0.0005));

        //1,568,423 meters away
        assertFalse(e.coordinates_in_range(0.00074,0.0005));

        //1,568,450 meters away
        assertFalse(e.coordinates_in_range(0,0.0009));

        //New locations
        //109.5 meters away
        assertFalse(e.coordinates_in_range(10,10.001));

        //10.95 meters away
        assertTrue(e.coordinates_in_range(10,10.0001));

        //0 meters away
        assertTrue(e.coordinates_in_range(10,10));
    }

    @Test
    void testCoordinateToggle(){
        Event e = MockEvent();
        e.setregistrationRadiusEnabled(false);

        //10 007 543 meters
        assertTrue(e.coordinates_in_range(90,180));

        //0 meters
        assertTrue(e.coordinates_in_range(0,0));

        //1.5 meter
        assertTrue(e.coordinates_in_range(0.00001,0.00001));


        e.setregistrationRadiusEnabled(true);
        //10 007 543 meters
        assertFalse(e.coordinates_in_range(90,180));

        //0 meters
        assertTrue(e.coordinates_in_range(0,0));

        //1.5 meter
        assertTrue(e.coordinates_in_range(0.00001,0.00001));



        //with non-zero coordinates

        e.setLocation(-12,12);
        assertFalse(e.coordinates_in_range(-10,10));
        assertTrue(e.coordinates_in_range(-12,12));
        assertTrue(e.coordinates_in_range(-12.0001,12));
    }

    //Throw error on invalid coordinate input
    @Test
    void testRejectBadCoordinates(){
        Event e = MockEvent();
        //setLocation
        assertThrows(IllegalArgumentException.class,()->e.setLocation(-91,0));
        assertThrows(IllegalArgumentException.class,()->e.setLocation(1231,0));
        assertThrows(IllegalArgumentException.class,()->e.setLocation(0,200));
        assertThrows(IllegalArgumentException.class,()->e.setLocation(0,-181));
        assertThrows(IllegalArgumentException.class,()->e.setLocation(-100,181));

        //Latitude Update
        assertThrows(IllegalArgumentException.class,()->e.setLatitude(-1928310));
        assertThrows(IllegalArgumentException.class,()->e.setLatitude(-99));
        assertThrows(IllegalArgumentException.class,()->e.setLatitude(9123));

        //Longitude Update
        assertThrows(IllegalArgumentException.class,()->e.setLongitude(913));
        assertThrows(IllegalArgumentException.class,()->e.setLongitude(-203));
        assertThrows(IllegalArgumentException.class,()->e.setLongitude(-91283));
        //Assert that no error is thrown
        assertAll(()->{
            e.setLatitude(-84.07211780548096);
            e.setLongitude(51.05296833016841);
            e.setLocation(85,170);
        });


        //Coordinate Range check
        assertThrows(IllegalArgumentException.class,()->e.coordinates_in_range(-91,0));
        assertThrows(IllegalArgumentException.class,()->e.coordinates_in_range(1231,0));
        assertThrows(IllegalArgumentException.class,()->e.coordinates_in_range(0,200));
        assertThrows(IllegalArgumentException.class,()->e.coordinates_in_range(0,-398723));
        assertThrows(IllegalArgumentException.class,()->e.coordinates_in_range(-100,181));
    }
}
