package com.example.indeedgambling;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Unit tests for US 02.04.01 and US 02.04.02:
 * Uploading and Updating Event Posters.
 */
public class OrganizerPosterTest {

    @Test
    void testImageToBase64EncodingAndDecoding() {
        // ✅ Simulate a small bitmap (100x100)
        Bitmap original = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

        // Encode to Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        original.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        String encoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        // Decode back
        byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

        assertNotNull(encoded, "Base64 string should not be null");
        assertNotNull(decodedBitmap, "Decoded bitmap should not be null");
        assertEquals(100, decodedBitmap.getWidth(), "Width should match original");
        assertEquals(100, decodedBitmap.getHeight(), "Height should match original");
    }

    @Test
    void testUpdatePosterFieldInEvent() {
        // ✅ Mock event
        Event event = new Event();
        String fakeBase64 = "dGVzdA=="; // "test" in Base64
        event.setImageUrl(fakeBase64);

        // Update field check
        assertEquals(fakeBase64, event.getImageUrl(), "Poster should be saved as Base64 string");
    }

    @Test
    void testEmptyPosterDoesNotCrash() {
        // ✅ Ensure event without image still safe
        Event event = new Event();
        event.setImageUrl("");

        assertTrue(event.getImageUrl().isEmpty(), "Empty poster URL should be handled safely");
    }
}
