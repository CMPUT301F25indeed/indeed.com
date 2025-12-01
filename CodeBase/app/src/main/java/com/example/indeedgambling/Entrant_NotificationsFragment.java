package com.example.indeedgambling;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

<<<<<<< HEAD
=======
/**
 * Displays all notifications received by an Entrant.
 *
 * This fragment retrieves real-time notification updates from Firestore
 * and renders them in a scrollable list. Each notification item shows:
 * - The message text
 * - The notification type (Info, Alert, Update, etc.)
 * - The formatted timestamp
 *
 * Features:
 * - Dynamically updates when new notifications arrive
 * - Provides a back-navigation button to return to the Entrant home screen
 * - Uses ArrayAdapter to render each notification with custom layout
 */
>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
public class Entrant_NotificationsFragment extends Fragment {

    private EntrantViewModel entrantVM;
    private FirebaseViewModel firebaseVM;

    private ListView listView;
    private ArrayAdapter<Notification> adapter;

<<<<<<< HEAD
    private static final String TAG = "EntrantNotifs";

=======
    /**
     * Creates the notification screen, loads the user's notifications,
     * and initializes the list adapter.
     */
>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.entrant_notifications_fragment, container, false);

        entrantVM  = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        listView = view.findViewById(R.id.entrant_notifications_list);
        Button home = view.findViewById(R.id.entrant_notifications_home_button);

        home.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Use the current entrant directly
        Entrant entrant = entrantVM.getCurrentEntrant();
<<<<<<< HEAD
        if (entrant == null || entrant.getProfileId() == null) {
            Log.d(TAG, "Entrant is null or has no profileId – no notifications.");
            Toast.makeText(requireContext(),
                    "No entrant loaded. Please log in again.",
                    Toast.LENGTH_SHORT).show();
            return view;
=======
        if (entrant != null && entrant.getProfileId() != null) {
            String entrantId = entrant.getProfileId();

            firebaseVM.observeNotificationsForUser(entrantId)
                    .observe(getViewLifecycleOwner(), notifications -> {

                        if (notifications == null) {
                            notifications = new ArrayList<>();
                        }

                        updateList(notifications);
                    });
>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
        }

        String uid = entrant.getProfileId();
        Log.d(TAG, "Observing notifications for userId = " + uid);

        firebaseVM.observeNotificationsForUser(uid)
                .observe(getViewLifecycleOwner(), notifications -> {
                    if (notifications != null) {
                        Log.d(TAG, "Got " + notifications.size() + " notifications:");
                        for (Notification n : notifications) {
                            Log.d(TAG,
                                    "  type=" + n.getType()
                                            + " msg=" + n.getMessage()
                                            + " ts=" + n.getTimestamp());
                        }
                    } else {
                        Log.d(TAG, "Notifications list is null");
                    }
                    updateList(notifications);
                });

        return view;
    }

<<<<<<< HEAD
    private void updateList(List<Notification> notifications) {
        if (notifications == null)
            notifications = new ArrayList<>();

        if (adapter == null) {
=======
    /**
     * Updates the ListView by creating or refreshing the ArrayAdapter
     * that displays notification message, type, and formatted time.
     *
     * @param notifications List of notifications for the Entrant
     */
    private void updateList(List<Notification> notifications) {

        if (adapter == null) {

>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
            adapter = new ArrayAdapter<Notification>(
                    requireContext(),
                    R.layout.item_notification,
                    R.id.notification_message,
                    notifications
            ) {
                @NonNull
                @Override
<<<<<<< HEAD
                public View getView(int position,
                                    @Nullable View convertView,
                                    @NonNull ViewGroup parent) {

=======
                public View getView(
                        int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent
                ) {
>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
                    View v = convertView;
                    if (v == null) {
                        v = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_notification, parent, false);
                    }

                    Notification n = getItem(position);
                    if (n == null) return v;

                    TextView msg = v.findViewById(R.id.notification_message);
                    TextView meta = v.findViewById(R.id.notification_meta);

                    msg.setText(n.getMessage());

                    String type = n.getType() != null ? n.getType() : "Info";

                    Date ts = n.getTimestamp();
<<<<<<< HEAD
                    String time = (ts != null)
=======
                    String formattedTime = ts != null
>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
                            ? DateFormat.format("MMM d, h:mm a", ts).toString()
                            : "";

                    meta.setText(type + " • " + formattedTime);

                    return v;
                }
            };

            listView.setAdapter(adapter);

        } else {
            adapter.clear();
            adapter.addAll(notifications);
            adapter.notifyDataSetChanged();
        }
    }
}