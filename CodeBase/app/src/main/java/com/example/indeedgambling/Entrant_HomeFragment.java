/**
 * Displays the Entrant home screen.
 *
 * Features:
 * - Shows entrant options (Browse, History, Profile, etc.)
 * - Displays most recent notification
 * - Provides logout functionality that clears the saved deviceId
 *   so auto-login does not occur next time.
 */
package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class Entrant_HomeFragment extends Fragment {

    private EntrantViewModel entrantVM;
    private FirebaseViewModel fvm;

    public Entrant_HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.entrant_home_fragment, container, false);

        ListView options = view.findViewById(R.id.entrant_home_buttons);
        Button LogoutButton = view.findViewById(R.id.entrant_logout_button_home);
        TextView greeting = view.findViewById(R.id.entrant_greeting_home);

        String[] optionsString = {"Browse", "History", "Profile", "Guidelines", "Notifications", "Scan QR Code"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_entrant_home_option,
                R.id.entrant_option_text,
                optionsString
        );

        options.setAdapter(adapter);

        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        Profile e = entrantVM.getCurrentEntrant();
        greeting.setText("Hi " + e.getPersonName());

        if (e.getProfileId() != null) {
            String entrantId = e.getProfileId();

            fvm.fetchLatestNotification(entrantId,
                    notification -> {
                        if (notification == null) return;

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Notification")
                                .setMessage(notification.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    },
                    error -> Log.e("Entrant_HomeFragment",
                            "Failed to load latest notification", error));
        }

        options.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position == 0) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.entrant_BrowseFragment);
            } else if (position == 1) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.entrant_HistoryFragment);
            } else if (position == 2) {
                Bundle args = new Bundle();
                args.putString("profileID", e.getProfileId());
                NavHostFragment.findNavController(this)
                        .navigate(R.id.settingsFragment, args);
            } else if (position == 3) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.entrant_GuidelinesFragment);
            } else if (position == 4) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.entrant_NotificationsFragment);
            } else if (position == 5) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrantHomeFragment_to_scanQRFragment);
            }
        });

        LogoutButton.setOnClickListener(v -> {
            Entrant current = entrantVM.getCurrentEntrant();

            if (current != null) {
                String profileId = current.getProfileId();

                fvm.updateProfile(
                        profileId,
                        java.util.Collections.singletonMap("deviceId", null),
                        () -> {
                            entrantVM.setEntrant(null);
                            NavHostFragment.findNavController(this)
                                    .navigate(R.id.action_entrantHomeFragment_to_startUpFragment);
                        },
                        err -> {
                            entrantVM.setEntrant(null);
                            NavHostFragment.findNavController(this)
                                    .navigate(R.id.action_entrantHomeFragment_to_startUpFragment);
                        }
                );
            } else {
                entrantVM.setEntrant(null);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrantHomeFragment_to_startUpFragment);
            }
        });

        return view;
    }
}
