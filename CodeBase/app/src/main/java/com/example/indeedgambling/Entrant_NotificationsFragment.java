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

public class Entrant_NotificationsFragment extends Fragment {

    private EntrantViewModel entrantVM;
    private FirebaseViewModel firebaseVM;

    private ListView listView;
    private ArrayAdapter<Notification> adapter;

    private static final String TAG = "EntrantNotifs";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.entrant_notifications_fragment, container, false);

        entrantVM  = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        listView = view.findViewById(R.id.entrant_notifications_list);
        Button home = view.findViewById(R.id.entrant_notifications_home_button);

        home.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        // Use the current entrant directly
        Entrant entrant = entrantVM.getCurrentEntrant();
        if (entrant == null || entrant.getProfileId() == null) {
            Log.d(TAG, "Entrant is null or has no profileId – no notifications.");
            Toast.makeText(requireContext(),
                    "No entrant loaded. Please log in again.",
                    Toast.LENGTH_SHORT).show();
            return view;
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

    private void updateList(List<Notification> notifications) {
        if (notifications == null)
            notifications = new ArrayList<>();

        if (adapter == null) {
            adapter = new ArrayAdapter<Notification>(
                    requireContext(),
                    R.layout.item_notification,
                    R.id.notification_message,
                    notifications) {

                @NonNull
                @Override
                public View getView(int position,
                                    @Nullable View convertView,
                                    @NonNull ViewGroup parent) {

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
                    String time = (ts != null)
                            ? DateFormat.format("MMM d, h:mm a", ts).toString()
                            : "";

                    meta.setText(type + " • " + time);

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