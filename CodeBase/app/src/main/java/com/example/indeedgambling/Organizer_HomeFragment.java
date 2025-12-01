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
/**
 * Main home screen fragment for event organizers.
 *
 * <p>This fragment serves as the central navigation hub for organizers,
 * providing access to all major organizer features and functionalities.
 *
 * <p>Features accessible from this screen:
 * <ul>
 *   <li>Browse events created by organizers</li>
 *   <li>View and manage upcoming events</li>
 *   <li>Access event history</li>
 *   <li>View organizer guidelines</li>
 *   <li>Access profile settings</li>
 *   <li>Logout from the application</li>
 * </ul>
 *
 * @see Organizer_BrowseFragment
 * @see Organizer_UpcomingFragment
 * @see Organizer_HistoryFragment
 * @see Organizer_GuidelinesFragment
 * @see SettingsFragment
 */

public class Organizer_HomeFragment extends Fragment {
    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * <p>Inflates the organizer home layout and initializes all navigation
     * buttons to their respective destination fragments.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     *                 any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's
     *                  UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state
     * @return The View for the fragment's UI, or null
     */

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.organization_home_fragment, container, false);

        OrganizerViewModel organizerVM =
                new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);

        Profile organizer = organizerVM.getCurrentOrganizer();

        // Logout button
        Button LogoutButton = view.findViewById(R.id.Organizer_HomeLogoutButton);
        LogoutButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_orgHome_to_start)
        );

        // Browse button
        Button BrowseButton = view.findViewById(R.id.Organizer_HomeBrowseEventsButton);
        BrowseButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_orgHome_to_browse)
        );

        // Upcoming events button
        Button UpcomingEventsButton = view.findViewById(R.id.Organizer_HomeUpcomingEventsButton);
        UpcomingEventsButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_orgHome_to_upcoming)
        );

        // History button
        Button HistoryButton = view.findViewById(R.id.Organizer_HomeHistoryButton);
        HistoryButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_orgHome_to_history)
        );
        //Guidelines Button
        Button btnGuidelines = view.findViewById(R.id.btnOrganizerGuidelines);
        btnGuidelines.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_organizerHomeFragment_to_organizerGuidelinesFragment);
        });
        // Profile button
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
