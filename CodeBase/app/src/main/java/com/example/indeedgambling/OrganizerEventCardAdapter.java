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

    public OrganizerEventCardAdapter(@NonNull Context context,
                                     @NonNull List<Event> events,
                                     @NonNull FirebaseViewModel firebaseVM) {
        super(context, 0, events);
        this.inflater = LayoutInflater.from(context);
        this.firebaseVM = firebaseVM;
    }

    static class ViewHolder {
        ImageView poster;
        TextView title;
        String boundImageDocId; // to avoid wrong image when reused
    }

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

        // Title
        String name = event.getEventName();
        holder.title.setText(name != null ? name : "Untitled event");

        // Reset poster to placeholder while loading
        holder.poster.setImageResource(android.R.drawable.ic_menu_report_image);
        holder.boundImageDocId = event.getImageUrl();

        String imageDocId = event.getImageUrl();
        if (imageDocId == null || imageDocId.isEmpty()) {
            // no poster, keep placeholder
            return convertView;
        }

        // Fetch Base64 poster from /images doc and decode
        firebaseVM.getDb().collection("images")
                .document(imageDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    // make sure this row is still bound to same image id
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
                .addOnFailureListener(e -> {
                    holder.poster.setImageResource(android.R.drawable.ic_menu_report_image);
                });

        return convertView;
    }
}
