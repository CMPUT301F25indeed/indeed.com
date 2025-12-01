package com.example.indeedgambling;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying events in the Entrant browsing screen.
 *
 * Responsibilities:
 * - Displays event name, description/location, and poster image
 * - Loads event images asynchronously from Firestore
 * - Handles item click events through the OnEventClick interface
 * - Uses a placeholder background while images are loading or missing
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventHolder> {

    private final List<Event> list = new ArrayList<>();
    private final OnEventClick listener;
    private final FirebaseViewModel firebaseVM;

    /**
     * Listener interface for handling event item clicks.
     */
    public interface OnEventClick {
        void clicked(Event e);
    }

    /**
     * Creates a new EventsAdapter.
     *
     * @param listener Callback for item clicks
     * @param firebaseVM Reference to FirebaseViewModel for image loading
     */
    public EventsAdapter(OnEventClick listener, FirebaseViewModel firebaseVM) {
        this.listener = listener;
        this.firebaseVM = firebaseVM;
    }

    /**
     * Replaces the current event list with a new one.
     */
    public void setData(List<Event> events) {
        list.clear();
        if (events != null) {
            list.addAll(events);
        }
        notifyDataSetChanged();
    }

    /**
     * Inflates the event item layout.
     */
    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventHolder(v);
    }

    /**
     * Binds event data to an item view, including name, description, and poster image.
     */
    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        Event e = list.get(position);

        holder.title.setText(e.getEventName());
        holder.desc.setText(e.getLocationString());

        holder.itemView.setOnClickListener(v -> listener.clicked(e));

        String imageDocId = e.getImageUrl();

        if (imageDocId == null || imageDocId.isEmpty()) {
            holder.image.setImageDrawable(null);
            holder.image.setBackgroundResource(R.drawable.bg_event_image_placeholder);
            return;
        }

        holder.image.setBackgroundResource(R.drawable.bg_event_image_placeholder);

        firebaseVM.getDb().collection("images")
                .document(imageDocId)
                .get()
                .addOnSuccessListener(doc -> applyImageFromDoc(doc, holder.image))
                .addOnFailureListener(err -> {
                    holder.image.setImageDrawable(null);
                    holder.image.setBackgroundResource(R.drawable.bg_event_image_placeholder);
                });
    }

    /**
     * Loads and applies the poster image from a Firestore document.
     */
    private void applyImageFromDoc(DocumentSnapshot doc, ImageView imageView) {
        if (doc == null || !doc.exists()) {
            imageView.setImageDrawable(null);
            imageView.setBackgroundResource(R.drawable.bg_event_image_placeholder);
            return;
        }

        String base64 = doc.getString("url");
        if (base64 == null || base64.isEmpty()) {
            imageView.setImageDrawable(null);
            imageView.setBackgroundResource(R.drawable.bg_event_image_placeholder);
            return;
        }

        try {
            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            imageView.setImageDrawable(null);
            imageView.setBackgroundResource(R.drawable.bg_event_image_placeholder);
        }
    }

    /**
     * Returns number of events in the list.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder that stores references to UI components for each event item.
     */
    static class EventHolder extends RecyclerView.ViewHolder {
        TextView title, desc;
        ImageView image;

        EventHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            desc  = itemView.findViewById(R.id.event_desc);
            image = itemView.findViewById(R.id.event_image);

            title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            desc.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }
}
