package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entrant-side event filtering test (JUnit 5)
 */
class EntrantFilterUnitTest {

    @Test
    void testEntrantFilterByCategoryAndDate() {
        Date now = new Date();

        //  Event 1: open, correct category — should match
        Event e1 = new Event(
                "Music Gala",
                new Date(now.getTime() - 1000 * 60 * 60),   // reg open 1 h ago
                new Date(now.getTime() + 1000 * 60 * 60),   // reg closes in 1 h
                new Date(now.getTime() + 1000 * 60 * 120),  // starts in 2 h
                new Date(now.getTime() + 1000 * 60 * 180),  // ends in 3 h
                "ORG1",
                "Live concert",
                "Lottery",
                "Music",
                "https://example.com/qr1.png",
                "Location"
        );

        //  Event 2: closed
        Event e2 = new Event(
                "Old Music Fest",
                new Date(now.getTime() - 1000 * 60 * 600),
                new Date(now.getTime() - 1000 * 60 * 300),
                new Date(now.getTime() - 1000 * 60 * 200),
                new Date(now.getTime() - 1000 * 60 * 100),
                "ORG2",
                "Old concert",
                "Lottery",
                "Music",
                "https://example.com/qr2.png",
                "Location"
        );

        //  Event 3: wrong category
        Event e3 = new Event(
                "Sports Marathon",
                new Date(now.getTime() - 1000 * 60 * 60),
                new Date(now.getTime() + 1000 * 60 * 60),
                new Date(now.getTime() + 1000 * 60 * 120),
                new Date(now.getTime() + 1000 * 60 * 240),
                "ORG3",
                "Sports event",
                "Lottery",
                "Sports",
                "https://example.com/qr3.png",
                "Location"
        );

        //  Event 4: registration not open yet
        Event e4 = new Event(
                "Future Music Night",
                new Date(now.getTime() + 1000 * 60 * 60),
                new Date(now.getTime() + 1000 * 60 * 180),
                new Date(now.getTime() + 1000 * 60 * 240),
                new Date(now.getTime() + 1000 * 60 * 300),
                "ORG4",
                "Future event",
                "Lottery",
                "Music",
                "https://example.com/qr4.png",
                "Location"
        );

        List<Event> allEvents = List.of(e1, e2, e3, e4);

        String chosenCategory = "Music";
        Date filterStart = now;
        Date filterEnd = new Date(now.getTime() + 1000 * 60 * 360);

        List<Event> filtered = new ArrayList<>();

        for (Event e : allEvents) {
            boolean categoryOk = e.getCategory().equalsIgnoreCase(chosenCategory);
            boolean openNow = e.RegistrationOpen();
            boolean inRange = e.getEventStart().after(filterStart)
                    && e.getEventEnd().before(filterEnd);

            if (categoryOk && openNow && inRange) filtered.add(e);
        }

        //  Assertions (JUnit 5 style)
        assertTrue(filtered.contains(e1));
        assertFalse(filtered.contains(e2));
        assertFalse(filtered.contains(e3));
        assertFalse(filtered.contains(e4));
        assertEquals(1, filtered.size());
    }
}
