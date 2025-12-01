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

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventHolder> {

    private final List<Event> list = new ArrayList<>();
    private final OnEventClick listener;
    private final FirebaseViewModel firebaseVM;

    public interface OnEventClick {
        void clicked(Event e);
    }

    public EventsAdapter(OnEventClick listener, FirebaseViewModel firebaseVM) {
        this.listener = listener;
        this.firebaseVM = firebaseVM;
    }

    public void setData(List<Event> events) {
        list.clear();
        if (events != null) {
            list.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        Event e = list.get(position);

        holder.title.setText(e.getEventName());
        holder.desc.setText(e.getLocationString());   // or description, your choice

        holder.itemView.setOnClickListener(v -> listener.clicked(e));

        // ---------- image logic ----------
        String imageDocId = e.getImageUrl();

        if (imageDocId == null || imageDocId.isEmpty()) {
            // no image → just grey box
            holder.image.setImageDrawable(null);
            holder.image.setBackgroundResource(R.drawable.bg_event_image_placeholder);
            return;
        }

        // keep grey background behind real image
        holder.image.setBackgroundResource(R.drawable.bg_event_image_placeholder);

        firebaseVM.getDb().collection("images")
                .document(imageDocId)
                .get()
                .addOnSuccessListener(doc -> applyImageFromDoc(doc, holder.image))
                .addOnFailureListener(err -> {
                    // on error, fallback to plain grey
                    holder.image.setImageDrawable(null);
                    holder.image.setBackgroundResource(R.drawable.bg_event_image_placeholder);
                });
    }

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

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class EventHolder extends RecyclerView.ViewHolder {
        TextView title, desc;
        ImageView image;

        EventHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            desc  = itemView.findViewById(R.id.event_desc);
            image = itemView.findViewById(R.id.event_image);

            // make sure text is centered
            title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            desc.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }
}
