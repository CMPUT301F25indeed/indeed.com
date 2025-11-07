package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Notification data validation tests
 */
class NotificationCheckTest {

    @Test
    void testTypeAndMessageSetup() {
        Notification n = new Notification();
        n.setType("info");
        n.setMessage("New event available!");
        assertEquals("info", n.getType());
        assertTrue(n.getMessage().contains("event"));
    }

    @Test
    void testManualEqualityFields() {
        Notification n1 = new Notification();
        n1.setSenderId("org1");
        n1.setReceiverId("user1");

        Notification n2 = new Notification();
        n2.setSenderId("org1");
        n2.setReceiverId("user1");

        assertNotSame(n1, n2);
        assertEquals(n1.getSenderId(), n2.getSenderId());
    }
}
