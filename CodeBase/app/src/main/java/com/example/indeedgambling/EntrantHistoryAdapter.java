package com.example.indeedgambling;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter for displaying an Entrant's event history.
 *
 * Each list item shows:
 * - Event title
 * - Status relative to the entrant (waiting, invited, accepted, cancelled)
 * - Event image loaded asynchronously from Firestore
 *
 * Features:
 * - Supports tag-matching to avoid image flicker or mismatch when views are reused
 * - Gracefully handles missing images or missing event fields
 * - Uses item_history.xml as the row layout
 */
public class EntrantHistoryAdapter extends ArrayAdapter<Event> {

    private final String entrantId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Creates an adapter for displaying a list of events associated with an entrant.
     *
     * @param context   Activity or Fragment context
     * @param events    List of events to render
     * @param entrantId The profile ID of the Entrant viewing the history
     */
    public EntrantHistoryAdapter(
            @NonNull Context context,
            @NonNull List<Event> events,
            @NonNull String entrantId
    ) {
        super(context, 0, events);
        this.entrantId = entrantId;
    }

    /**
     * Inflates and populates a single history row with event title, status, and image.
     *
     * @param position    Index of the list item
     * @param convertView Reused row view (if available)
     * @param parent      Parent ListView
     * @return A fully populated view for the given position
     */
    @NonNull
    @Override
    public View getView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_history, parent, false);
        }

        Event event = getItem(position);
        if (event == null) {
            return convertView;
        }

        TextView titleView = convertView.findViewById(R.id.history_event_title);
        TextView statusView = convertView.findViewById(R.id.history_event_status);
        ImageView imageView = convertView.findViewById(R.id.history_event_image);

        String name = event.getEventName();
        if (name == null || name.isEmpty()) {
            name = "Untitled event";
        }
        titleView.setText(name);

        String listName = event.whichList(entrantId);
        String statusText;

        switch (listName) {
            case "waiting":
                statusText = "On waitlist";
                break;
            case "invited":
                statusText = "Invited – tap to respond";
                break;
            case "accepted":
                statusText = "Accepted";
                break;
            case "cancelled":
                statusText = "Cancelled";
                break;
            default:
                statusText = "Not active";
                break;
        }

        statusView.setText(statusText);

        imageView.setImageBitmap(null);
        imageView.setBackgroundResource(R.drawable.bg_event_image_rounded);

        String imageDocId = event.getImageUrl();
        if (imageDocId == null || imageDocId.isEmpty()) {
            imageView.setTag(null);
            return convertView;
        }

        imageView.setTag(imageDocId);

        db.collection("images")
                .document(imageDocId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (!doc.exists()) return;

                    Object tag = imageView.getTag();
                    if (!(tag instanceof String) || !imageDocId.equals(tag)) return;

                    String base64 = doc.getString("url");
                    if (base64 == null || base64.isEmpty()) return;

                    try {
                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                        if (bmp != null) {
                            imageView.setBackground(null);
                            imageView.setImageBitmap(bmp);
                        }
                    } catch (Exception ignored) {}
                });

        return convertView;
    }
}
