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

import java.util.Collections;

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

        String[] optionsString = {
                "Browse", "History", "Profile", "Guidelines",
                "Notifications", "Scan QR Code"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_entrant_home_option,
                R.id.entrant_option_text,
                optionsString
        );
        options.setAdapter(adapter);

        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        // Start global notification listener
        entrantVM.startNotificationListener(fvm);

        Profile entrant = entrantVM.getCurrentEntrant();
        if (entrant != null && entrant.getPersonName() != null) {
            greeting.setText("Hi " + entrant.getPersonName());
        }

        // Attempts to load the most recent notification for the entrant
        // and displays it in a popup dialog if one exists.
        if (entrant != null && entrant.getProfileId() != null) {
            String entrantId = entrant.getProfileId();

            fvm.fetchLatestNotification(
                    entrantId,
                    notification -> {
                        if (notification != null) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Notification")
                                    .setMessage(notification.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    },
                    error -> Log.e(
                            "Entrant_HomeFragment",
                            "Failed to load latest notification",
                            error
                    )
            );
        }

        /**
         * Handles navigation to all Entrant features based on menu selection.
         */
        options.setOnItemClickListener((parent, itemView, position, id) -> {
            switch (position) {
                case 0:
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.entrant_BrowseFragment);
                    break;
                case 1:
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.entrant_HistoryFragment);
                    break;
                case 2: {
                    if (entrant != null) {
                        Bundle args = new Bundle();
                        args.putString("profileID", entrant.getProfileId());
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.settingsFragment, args);
                    }
                    break;
                }
                case 3:
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.entrant_GuidelinesFragment);
                    break;
                case 4:
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.entrant_NotificationsFragment);
                    break;
                case 5:
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_entrantHomeFragment_to_scanQRFragment);
                    break;
            }
        });

        /**
         * Logs out the entrant by clearing the saved deviceId,
         * disabling auto-login, and returning the user to the startup screen.
         */
        logoutButton.setOnClickListener(v -> {

            Entrant current = entrantVM.getCurrentEntrant();

            if (current != null && current.getProfileId() != null) {

                String profileId = current.getProfileId();

                fvm.updateProfile(
                        profileId,
                        Collections.singletonMap("deviceId", null),
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