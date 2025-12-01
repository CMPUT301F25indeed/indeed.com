/**
 * Adapter for displaying organizer-created events as card items.
 *
 * This adapter populates a ListView with card-style event previews
 * containing:
 * - Poster image (loaded from Firestore Base64)
 * - Event title
 *
 * Features:
 * - ViewHolder pattern used for efficient row recycling
 * - Prevents image flicker by binding imageDocId to each row
 * - Loads event poster asynchronously from Firestore /images collection
 */
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

import java.util.List;

public class OrganizerEventCardAdapter extends ArrayAdapter<Event> {

    private final LayoutInflater inflater;
    private final FirebaseViewModel firebaseVM;

    /**
     * Creates a new adapter for event cards displayed to an organizer.
     *
     * @param context  Activity or fragment context
     * @param events   List of event objects to render
     * @param firebaseVM ViewModel used for Firestore image retrieval
     */
    public OrganizerEventCardAdapter(@NonNull Context context,
                                     @NonNull List<Event> events,
                                     @NonNull FirebaseViewModel firebaseVM) {
        super(context, 0, events);
        this.inflater = LayoutInflater.from(context);
        this.firebaseVM = firebaseVM;
    }

    /**
     * ViewHolder pattern to reduce repeated layout inflations and
     * maintain correct poster-image binding during list recycling.
     */
    static class ViewHolder {
        ImageView poster;
        TextView title;
        String boundImageDocId;
    }

    /**
     * Populates or recycles the event card layout for a single row.
     */
    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.organizer_event_card_item, parent, false);
            holder = new ViewHolder();
            holder.poster = convertView.findViewById(R.id.organizerEventPosterImage);
            holder.title = convertView.findViewById(R.id.organizerEventTitleText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = getItem(position);
        if (event == null) {
            return convertView;
        }

        holder.title.setText(event.getEventName() != null
                ? event.getEventName()
                : "Untitled event");

        holder.poster.setImageResource(android.R.drawable.ic_menu_report_image);
        holder.boundImageDocId = event.getImageUrl();

        String imageDocId = event.getImageUrl();
        if (imageDocId == null || imageDocId.isEmpty()) {
            return convertView;
        }

        firebaseVM.getDb().collection("images")
                .document(imageDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!imageDocId.equals(holder.boundImageDocId)) return;

                    if (doc == null || !doc.exists()) {
                        holder.poster.setImageResource(android.R.drawable.ic_menu_report_image);
                        return;
                    }

                    String base64 = doc.getString("url");
                    if (base64 == null || base64.isEmpty()) {
                        holder.poster.setImageResource(android.R.drawable.ic_menu_report_image);
                        return;
                    }

                    try {
                        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        holder.poster.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        holder.poster.setImageResource(android.R.drawable.ic_menu_report_image);
                    }
                })
                .addOnFailureListener(e ->
                        holder.poster.setImageResource(android.R.drawable.ic_menu_report_image)
                );

        return convertView;
    }
}
