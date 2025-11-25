package com.example.indeedgambling;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class Entrant_NotificationsFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private EntrantViewModel entrantVM;
    private ListView listView;
    private ArrayAdapter<Notification> adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.entrant_notifications_fragment, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        listView = view.findViewById(R.id.entrant_notifications_list);
        Button home = view.findViewById(R.id.entrant_notifications_home_button);

        home.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        Entrant entrant = entrantVM.getCurrentEntrant();
        if (entrant != null && entrant.getProfileId() != null) {
            String entrantId = entrant.getProfileId();

            firebaseVM.observeNotificationsForUser(entrantId)
                    .observe(getViewLifecycleOwner(), notifications -> {
                        if (notifications == null) {
                            notifications = new ArrayList<>();
                        }
                        updateList(notifications);
                    });
        }

        return view;
    }

    /**
     *
     */
    private void updateList(List<Notification> notifications) {
        if (adapter == null) {
            adapter = new ArrayAdapter<Notification>(requireContext(),
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
                    if (n == null) {
                        return v;
                    }

                    TextView message = v.findViewById(R.id.notification_message);
                    TextView meta = v.findViewById(R.id.notification_meta);

                    message.setText(n.getMessage());

                    String type = n.getType() != null ? n.getType() : "Info";
                    Date ts = n.getTimestamp();
                    String time = ts != null
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
