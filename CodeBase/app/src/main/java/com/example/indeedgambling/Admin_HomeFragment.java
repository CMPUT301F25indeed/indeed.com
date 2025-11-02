package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class Admin_HomeFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin_dashboard_fragment, container, false);

        Button manageProfiles = view.findViewById(R.id.admin_browse_profiles);
        Button browseEvents = view.findViewById(R.id.admin_browse_events);
        Button reviewImages = view.findViewById(R.id.admin_browse_images);
        Button viewLogs = view.findViewById(R.id.admin_view_logs);
        Button logout = view.findViewById(R.id.admin_logout);

        manageProfiles.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.adminManageProfilesFragment));

        browseEvents.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.adminBrowseEventsFragment));

        reviewImages.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.adminReviewImagesFragment));

        viewLogs.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.adminLogsFragment));

        logout.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.startUpFragment));

        return view;
    }
}
