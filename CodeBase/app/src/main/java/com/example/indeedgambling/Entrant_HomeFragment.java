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

/**
 * Displays the Entrant home screen.
 *
 * This fragment acts as the main dashboard for an Entrant user after login.
 * It provides navigation to all entrant-related features such as browsing events,
 * viewing history, editing profile settings, checking notifications, reading event
 * guidelines, and scanning QR codes.
 *
 * Features:
 * - Displays a personalized greeting using the entrant’s name
 * - Fetches and shows the latest notification (if available)
 * - Provides navigation menu options for all entrant functionalities
 * - Allows logout and clears saved deviceId to disable auto-login upon exit
 */
public class Entrant_HomeFragment extends Fragment {

    private EntrantViewModel entrantVM;
    private FirebaseViewModel fvm;

    public Entrant_HomeFragment() {}

    /**
     * Creates and initializes the entrant home screen layout,
     * sets up menu interactions, loads notifications,
     * and sets logout behavior.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.entrant_home_fragment, container, false);

        ListView options = view.findViewById(R.id.entrant_home_buttons);
        Button logoutButton = view.findViewById(R.id.entrant_logout_button_home);
        TextView greeting = view.findViewById(R.id.entrant_greeting_home);

<<<<<<< HEAD
        String[] entries = {"Browse", "History", "Profile", "Guidelines", "Notifications", "Scan QR Code"};
=======
        String[] optionsString = {
                "Browse", "History", "Profile", "Guidelines",
                "Notifications", "Scan QR Code"
        };

>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_entrant_home_option,
                R.id.entrant_option_text,
                entries
        );

        options.setAdapter(adapter);

        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

<<<<<<< HEAD
        // -------------------------
        // NEW: START GLOBAL LISTENER
        // -------------------------
        entrantVM.startNotificationListener(fvm);

        Profile p = entrantVM.getCurrentEntrant();
        greeting.setText("Hi " + p.getPersonName());

        if (p.getProfileId() != null) {
            String entrantId = p.getProfileId();

            // popup of latest notification
            fvm.fetchLatestNotification(entrantId,
=======
        Profile entrant = entrantVM.getCurrentEntrant();
        greeting.setText("Hi " + entrant.getPersonName());

        /**
         * Attempts to load the most recent notification for the entrant
         * and displays it in a popup dialog if one exists.
         */
        if (entrant.getProfileId() != null) {
            String entrantId = entrant.getProfileId();

            fvm.fetchLatestNotification(
                    entrantId,
>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
                    notification -> {
                        if (notification != null)
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Notification")
                                    .setMessage(notification.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                    },
<<<<<<< HEAD
                    error -> Log.e("Entrant_HomeFragment", "Latest notification error", error));
=======
                    error -> Log.e(
                            "Entrant_HomeFragment",
                            "Failed to load latest notification",
                            error
                    )
            );
>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
        }

        /**
         * Handles navigation to all Entrant features based on menu selection.
         */
        options.setOnItemClickListener((parent, itemView, position, id) -> {
<<<<<<< HEAD
            switch (position) {
                case 0:
                    NavHostFragment.findNavController(this).navigate(R.id.entrant_BrowseFragment);
                    break;
                case 1:
                    NavHostFragment.findNavController(this).navigate(R.id.entrant_HistoryFragment);
                    break;
                case 2:
                    Bundle args = new Bundle();
                    args.putString("profileID", p.getProfileId());
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.settingsFragment, args);
                    break;
                case 3:
                    NavHostFragment.findNavController(this).navigate(R.id.entrant_GuidelinesFragment);
                    break;
                case 4:
                    NavHostFragment.findNavController(this).navigate(R.id.entrant_NotificationsFragment);
                    break;
                case 5:
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_entrantHomeFragment_to_scanQRFragment);
                    break;
            }
        });

        LogoutButton.setOnClickListener(v -> {
            fvm.updateProfile(
                    p.getProfileId(),
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
=======
            if (position == 0) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.entrant_BrowseFragment);
            } else if (position == 1) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.entrant_HistoryFragment);
            } else if (position == 2) {
                Bundle args = new Bundle();
                args.putString("profileID", entrant.getProfileId());
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

        /**
         * Logs out the entrant by clearing the saved deviceId,
         * disabling auto-login, and returning the user to the startup screen.
         */
        logoutButton.setOnClickListener(v -> {

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
>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
        });

        return view;
    }
}
