package com.example.indeedgambling;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Hash utility basic tests (JUnit 5)
 */
class HashUtilUnitTest {

    @Test
    void testSha256Produces64CharHash() {
        String hash = HashUtil.sha256("hello");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    void testGenerateIdDifferentInputs() {
        String id1 = HashUtil.generateId("user1", "pass1");
        String id2 = HashUtil.generateId("user1", "pass2");
        assertNotEquals(id1, id2);
    }
}
