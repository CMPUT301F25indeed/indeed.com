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

public class EntrantHistoryAdapter extends ArrayAdapter<Event> {

    private final String entrantId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public EntrantHistoryAdapter(@NonNull Context context,
                                 @NonNull List<Event> events,
                                 @NonNull String entrantId) {
        super(context, 0, events);
        this.entrantId = entrantId;
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

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

        // ----- Title -----
        String name = event.getEventName();
        if (name == null || name.isEmpty()) {
            name = "Untitled event";
        }
        titleView.setText(name);

        // ----- Status text -----
        // whichList may return things like: "waitlist", "waiting", "waitingList",
        // "invited", "accepted", "cancelled", or maybe null.
        String listName = event.whichList(entrantId);
        String statusText;

        if (listName == null) {
            statusText = "Not active";
        } else {
            switch (listName) {
                case "waitlist":
                case "waiting":
                case "waitingList":
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
        }

        statusView.setText(statusText);

        // ----- Image placeholder (rounded grey background) -----
        imageView.setImageBitmap(null);
        imageView.setBackgroundResource(R.drawable.bg_event_image_rounded);

        String imageDocId = event.getImageUrl();
        if (imageDocId == null || imageDocId.isEmpty()) {
            // no poster → keep grey rounded box
            imageView.setTag(null);
            return convertView;
        }

        // Tag to avoid wrong image on recycled rows
        imageView.setTag(imageDocId);

        db.collection("images")
                .document(imageDocId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc == null || !doc.exists()) return;

                    // still same row?
                    Object tag = imageView.getTag();
                    if (!(tag instanceof String) || !imageDocId.equals(tag)) {
                        return;
                    }

                    String base64 = doc.getString("url");
                    if (base64 == null || base64.isEmpty()) {
                        return;
                    }

                    try {
                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        if (bmp != null) {
                            imageView.setBackground(null); // remove grey background so corners show
                            imageView.setImageBitmap(bmp);
                        }
                    } catch (Exception e) {
                        // if decode fails, just keep placeholder
                    }
                })
                .addOnFailureListener(e -> {
                    // ignore, keep grey box
                });

        return convertView;
    }
}
