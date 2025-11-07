package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

/**
 * Notification model tests (JUnit 5)
 */
class NotificationUnitTest {

    @Test
    void testSetAndGetAllFields() {
        Notification n = new Notification();
        n.setSenderId("sender1");
        n.setReceiverId("receiver1");
        n.setEventId("event1");
        n.setType("invite");
        n.setMessage("You are invited!");
        Date now = new Date();
        n.setTimestamp(now);

        assertEquals("sender1", n.getSenderId());
        assertEquals("receiver1", n.getReceiverId());
        assertEquals("event1", n.getEventId());
        assertEquals("invite", n.getType());
        assertEquals("You are invited!", n.getMessage());
        assertEquals(now, n.getTimestamp());
    }
}
