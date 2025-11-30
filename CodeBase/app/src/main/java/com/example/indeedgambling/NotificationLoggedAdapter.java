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

    /** Click for item (optional) */
    public interface OnItemClickListener {
        void onItemClick(Notification n);
    }

    /** Click for Remove button */
    public interface OnRemoveClickListener {
        void onRemoveClick(Notification n);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.removeClickListener = listener;
    }

    public void setNotifications(List<Notification> list) {
        this.notifications = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_noti_admin_view, parent, false);
        return new NotiViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotiViewHolder holder, int position) {
        Notification n = notifications.get(position);

        // Combined header: "time | email"
        String time = formatTimestamp(n.getTimestamp());
        String email = "system".equals(n.getSenderId()) ? "system" :
                (n.getSenderEmail() != null ? n.getSenderEmail() : "Unknown Sender");
        holder.header.setText(time + "  |  " + email);

        // Message
        holder.message.setText(n.getMessage());

        // Event name
        holder.eventName.setText(n.getEventName() != null ? n.getEventName() : "N/A");

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(n);
        });

        // Remove button click
        holder.removeBtn.setOnClickListener(v -> {
            if (removeClickListener != null) removeClickListener.onRemoveClick(n);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

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

    @SuppressLint("SimpleDateFormat")
    private String formatTimestamp(Date date) {
        if (date == null) return "";

        long diff = new Date().getTime() - date.getTime();
        long mins = diff / (1000 * 60);
        long hours = mins / 60;
        long days = hours / 24;

        if (mins < 60)
            return mins + " min ago";
        if (hours < 24)
            return hours + " hours ago";
        if (days < 7)
            return days + " days ago";

        return new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(date);
    }
}
