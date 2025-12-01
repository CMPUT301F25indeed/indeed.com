/**
 * Utility class responsible for generating QR codes for events.
 *
 * This class uses the ZXing library to:
 * - Build a QR code from event information
 * - Encode event details into a single formatted string
 * - Produce a Bitmap that can be displayed or shared
 *
 * The generated QR codes contain:
 * - Event name
 * - Event ID
 * - Event location (text form)
 * - Event status
 *
 * This QR code is used primarily by organizers for event check-in
 * and may also be scanned by entrants to load event details.
 */
package com.example.indeedgambling;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeGenerator {

    /**
     * Creates a QR code bitmap that encodes the event's key information.
     *
     * @param event The event to encode into the QR code.
     * @return A 512x512 Bitmap containing the QR code, or null on failure.
     */
    public Bitmap generateEventQRCode(Event event) {
        try {
            String qrData = createEventQRData(event);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    512,
                    512
            );

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(
                            x,
                            y,
                            bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE
                    );
                }
            }

            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Builds the encoded text content stored inside the QR code.
     *
     * @param event Event whose data will be encoded.
     * @return A formatted String containing all event details for embedding.
     */
    private String createEventQRData(Event event) {
        return String.format(
                "Event: %s\nID: %s\nLocation: %s\nStatus: %s",
                event.getEventName(),
                event.getEventId(),
                event.getLocationString(),
                event.getStatus()
        );
    }
}
