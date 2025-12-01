/**
 * Adapter for displaying logged notifications in the admin view.
 *
 * This RecyclerView adapter supports:
 * - Binding notification data (timestamp, sender, message, event name)
 * - Handling item click actions through OnItemClickListener
 * - Handling removal actions through OnRemoveClickListener
 *
 * Notes:
 * - Notifications are shown newest → oldest based on the provided list
 * - Timestamp is displayed in a human-readable format
 * - Uses item_noti_admin_view.xml for layout
 */
package com.example.indeedgambling;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationLoggedAdapter extends RecyclerView.Adapter<NotificationLoggedAdapter.NotiViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private OnItemClickListener itemClickListener;
    private OnRemoveClickListener removeClickListener;

    /**
     * Listener for entire item clicks.
     */
    public interface OnItemClickListener {
        void onItemClick(Notification n);
    }

    /**
     * Listener for remove button clicks.
     */
    public interface OnRemoveClickListener {
        void onRemoveClick(Notification n);
    }

    /**
     * Assigns a listener for item clicks.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /**
     * Assigns a listener for remove button clicks.
     */
    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.removeClickListener = listener;
    }

    /**
     * Updates the list of notifications displayed by the adapter.
     */
    public void setNotifications(List<Notification> list) {
        this.notifications = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Inflates the notification row layout and returns a ViewHolder.
     */
    @NonNull
    @Override
    public NotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_noti_admin_view, parent, false);
        return new NotiViewHolder(v);
    }

    /**
     * Binds notification data to the ViewHolder for each row.
     */
    @Override
    public void onBindViewHolder(@NonNull NotiViewHolder holder, int position) {
        Notification n = notifications.get(position);

        String time = formatTimestamp(n.getTimestamp());
        String email = "system".equals(n.getSenderId())
                ? "system"
                : (n.getSenderEmail() != null ? n.getSenderEmail() : "Unknown Sender");

        holder.header.setText(time + "  |  " + email);
        holder.message.setText(n.getMessage());
        holder.eventName.setText(n.getEventName() != null ? n.getEventName() : "N/A");

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(n);
        });

        holder.removeBtn.setOnClickListener(v -> {
            if (removeClickListener != null) removeClickListener.onRemoveClick(n);
        });
    }

    /**
     * Returns number of notifications shown.
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Holds references to views inside each notification row.
     */
    static class NotiViewHolder extends RecyclerView.ViewHolder {
        TextView header, message, eventName;
        Button removeBtn;

        NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            header     = itemView.findViewById(R.id.noti_header);
            message    = itemView.findViewById(R.id.noti_message);
            eventName  = itemView.findViewById(R.id.noti_event_name);
            removeBtn  = itemView.findViewById(R.id.noti_remove_btn);
        }
    }

    /**
     * Formats timestamps into relative or full date formats.
     */
    @SuppressLint("SimpleDateFormat")
    private String formatTimestamp(Date date) {
        if (date == null) return "";

        long diff = new Date().getTime() - date.getTime();
        long mins = diff / (1000 * 60);
        long hours = mins / 60;
        long days = hours / 24;

        if (mins < 60) return mins + " min ago";
        if (hours < 24) return hours + " hours ago";
        if (days < 7) return days + " days ago";

        return new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                .format(date);
    }
}
