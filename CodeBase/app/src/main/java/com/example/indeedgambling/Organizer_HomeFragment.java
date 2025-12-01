/**
 * Displays the Organizer home screen.
 *
 * This fragment provides navigation to all organizer-related features:
 * - Browse all events
 * - View upcoming created events
 * - View event history
 * - Open guidelines
 * - Open organizer profile settings
 * - Logout and return to the start screen
 *
 * Notes:
 * - Uses OrganizerViewModel to retrieve the current organizer profile
 * - Passes profile ID to settings screen for editing
 */
package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class Organizer_HomeFragment extends Fragment {

    private View view;

    /**
     * Inflates the home screen and initializes all organizer navigation actions.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        view = inflater.inflate(R.layout.organization_home_fragment, container, false);

        OrganizerViewModel organizerVM =
                new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);

        Profile organizer = organizerVM.getCurrentOrganizer();

        /**
         * Handles logout navigation.
         */
        Button LogoutButton = view.findViewById(R.id.Organizer_HomeLogoutButton);
        LogoutButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_orgHome_to_start)
        );

        /**
         * Navigates to event browsing.
         */
        Button BrowseButton = view.findViewById(R.id.Organizer_HomeBrowseEventsButton);
        BrowseButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_orgHome_to_browse)
        );

        /**
         * Opens the organizer’s upcoming events list.
         */
        Button UpcomingEventsButton = view.findViewById(R.id.Organizer_HomeUpcomingEventsButton);
        UpcomingEventsButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_orgHome_to_upcoming)
        );

        /**
         * Opens event history created by the organizer.
         */
        Button HistoryButton = view.findViewById(R.id.Organizer_HomeHistoryButton);
        HistoryButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_orgHome_to_history)
        );

        /**
         * Opens organizer guidelines screen.
         */
        Button btnGuidelines = view.findViewById(R.id.btnOrganizerGuidelines);
        btnGuidelines.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_organizerHomeFragment_to_organizerGuidelinesFragment)
        );

        /**
         * Opens organizer profile settings.
         */
        Button ProfileButton = view.findViewById(R.id.Organizer_HomeProfileButton);
        ProfileButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("profileID", organizer.getProfileId());

            NavHostFragment.findNavController(this)
                    .navigate(R.id.settingsFragment, args);
        });

        return view;
    }
}
