package com.example.indeedgambling;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public interface OnItemClickListener {
        void onItemClick(Notification n);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setNotifications(List<Notification> list) {
        this.notifications = list;
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

        // Timestamp
        holder.timestamp.setText(formatTimestamp(n.getTimestamp()));

        // Message
        holder.message.setText(n.getMessage());

        // Sender email
        if ("system".equals(n.getSenderId())) {
            holder.senderEmail.setText("system");
        } else {
            holder.senderEmail.setText(n.getSenderEmail() != null ? n.getSenderEmail() : "Unknown Sender");
        }

        // Event name
        holder.eventName.setText(n.getEventName() != null ? n.getEventName() : "N/A");

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onItemClick(n);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotiViewHolder extends RecyclerView.ViewHolder {
        TextView timestamp, message, senderEmail, eventName;

        NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            timestamp   = itemView.findViewById(R.id.noti_timestamp);
            message     = itemView.findViewById(R.id.noti_message);
            senderEmail = itemView.findViewById(R.id.noti_sender_email);
            eventName   = itemView.findViewById(R.id.noti_event_name);
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
