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
 * Main dashboard screen for administrators after login.
 * Provides quick access to all admin features:
 * - Browse and manage user profiles
 * - View and remove events
 * - Review uploaded event posters
 * - View system logs
 * - Access personal settings
 * - Logout
 */
public class Admin_HomeFragment extends Fragment {


    /**
     * Sets up fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view itself.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from
     *                           a previous saved state as given here.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin_dashboard_fragment, container, false);

        Button manageProfiles = view.findViewById(R.id.admin_browse_profiles);
        Button browseEvents = view.findViewById(R.id.admin_browse_events);
        Button reviewImages = view.findViewById(R.id.admin_browse_images);
        Button viewLogs = view.findViewById(R.id.admin_view_logs);
        Button logout = view.findViewById(R.id.admin_logout);
        Button settings = view.findViewById(R.id.admin_settings);

        AdminViewModel adminVM =
                new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        Profile admin = adminVM.getAdmin();

        manageProfiles.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.adminManageProfilesFragment)
        );

        browseEvents.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.adminBrowseEventsFragment)
        );

        reviewImages.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.adminReviewImagesFragment)
        );

        viewLogs.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.adminLogsFragment)
        );

        settings.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("profileID", admin.getProfileId());

            NavHostFragment.findNavController(this)
                    .navigate(R.id.settingsFragment, args);
        });

        logout.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.startUpFragment)
        );

        return view;
    }
}
