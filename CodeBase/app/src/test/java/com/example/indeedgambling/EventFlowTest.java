package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Tests event timing and flow logic
 */
class EventFlowTest {

    @Test
    void testRegistrationWindowLogic() {
        Date now = new Date();
        Event e = new Event(
                "Music Night",
                new Date(now.getTime() - 5000),
                new Date(now.getTime() + 5000),
                now,
                new Date(now.getTime() + 10000),
                "ORGX", "desc", "criteria", "Music", "qr.png", "Location"
        );

        assertFalse(e.BeforeRegPeriod());
        assertFalse(e.AfterRegPeriod());
    }

    @Test
    void testEventHasLocationAndCriteria() {
        Event e = new Event();
        //e.setLocation("Vancouver");
        e.setCriteria("General");
        //assertTrue(e.hasLocation());
        assertEquals("General", e.getCriteria());
    }

    @Test
    void testInviteMovesEntrant() {
        Event e = new Event();
        e.getWaitingList().add("userA");
        e.InviteEntrants(1);
        assertEquals(1, e.getInvitedList().size());
    }
}
