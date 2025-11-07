package com.example.indeedgambling;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.List;

public class NotificationAdapter extends ArrayAdapter<Notification> {

    private final Context context;
    private final List<Notification> notifications;
    private final FirebaseFirestore db;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        super(context, 0, notifications);
        this.context = context;
        this.notifications = notifications;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Notification notification = notifications.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_notification, parent, false);
        }

        TextView title = convertView.findViewById(R.id.notification_title);
        TextView message = convertView.findViewById(R.id.notification_message);
        LinearLayout buttonLayout = convertView.findViewById(R.id.button_layout);
        Button acceptButton = convertView.findViewById(R.id.button_accept);
        Button rejectButton = convertView.findViewById(R.id.button_reject);

        title.setText(notification.getTitle());
        message.setText(notification.getMessage());

        if ("invitation".equals(notification.getType())) {
            buttonLayout.setVisibility(View.VISIBLE);

            acceptButton.setOnClickListener(v -> handleResponse(notification, true, position));
            rejectButton.setOnClickListener(v -> handleResponse(notification, false, position));
        } else {
            buttonLayout.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void handleResponse(Notification notification, boolean accepted, int position) {
        String userId = notification.getProfileId();
        String eventId = notification.getEventId();
        String notifId = notification.getId();

        if (eventId == null || userId == null || notifId == null) return;

        db.collection("Events").document(eventId)
                .update(
                        accepted ? "acceptedEntrants" : "rejectedEntrants", FieldValue.arrayUnion(userId),
                        "invitedList", FieldValue.arrayRemove(userId)
                ).addOnSuccessListener(aVoid -> {
                    Toast.makeText(context,
                            accepted ? "Invitation accepted" : "Invitation rejected",
                            Toast.LENGTH_SHORT).show();

                    // Remove notification from list and refresh
                    notifications.remove(position);
                    notifyDataSetChanged();

                    // Delete notification from Firestore
                    db.collection("Notifications").document(notifId)
                            .delete();
                }).addOnFailureListener(e -> Toast.makeText(context,
                        "Failed to respond: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }
}