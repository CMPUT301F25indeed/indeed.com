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
    private FirebaseViewModel fvm;

    public NotificationLoggedAdapter(FirebaseViewModel fvm) {
        this.fvm = fvm;
    }

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

        holder.timestamp.setText(formatTimestamp(n.getTimestamp()));
        holder.message.setText(n.getMessage());
        holder.senderName.setText("Loading...");

        holder.senderName.setText(n.getSenderId());
        // Observe sender profile
//        fvm.getProfileLive(n.getSenderId()).observeForever(profile -> {
//
//            if (!holder.senderName.getTag().equals(n.getSenderId())) return;
//
//            if (profile != null) {
//                holder.senderName.setText(profile.getEmail());
//            } else {
//                holder.senderName.setText("Unknown Sender");
//            }
//        });

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
        TextView timestamp, message, senderName;

        NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            timestamp = itemView.findViewById(R.id.noti_timestamp);
            message = itemView.findViewById(R.id.noti_message);
            senderName = itemView.findViewById(R.id.noti_sender_name);
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
