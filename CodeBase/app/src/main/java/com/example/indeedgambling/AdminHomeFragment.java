package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdminHomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        AdminActivity activity = (AdminActivity) getActivity();

        Button btnProfiles = view.findViewById(R.id.btnProfiles);
        Button btnEvents = view.findViewById(R.id.btnEvents);
        Button btnImages = view.findViewById(R.id.btnImages);
        Button btnLogs = view.findViewById(R.id.btnLogs);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        btnProfiles.setOnClickListener(v -> activity.loadFragment(new BrowseProfilesFragment()));
        btnEvents.setOnClickListener(v -> activity.loadFragment(new BrowseEventsFragment()));
        btnImages.setOnClickListener(v -> activity.loadFragment(new BrowseImagesFragment()));
        btnLogs.setOnClickListener(v -> activity.loadFragment(new ViewLogsFragment()));
        btnLogout.setOnClickListener(v -> activity.finish()); // End activity

        return view;
    }
}
