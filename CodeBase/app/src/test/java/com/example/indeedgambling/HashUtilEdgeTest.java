package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HashUtil edge and consistency tests
 */
class HashUtilEdgeTest {

    @Test
    void testEmptyStringHashLength() {
        String hash = HashUtil.sha256("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    void testSameInputsGiveSameResult() {
        String id1 = HashUtil.generateId("user@mail.com", "abc");
        String id2 = HashUtil.generateId("user@mail.com", "abc");
        assertEquals(id1, id2);
    }
}
