package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class Organizer_HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organization_home_fragment, container, false);

        Button browse = view.findViewById(R.id.Organizer_HomeBrowseEventsButton);
        Button upcoming = view.findViewById(R.id.Organizer_HomeUpcomingEventsButton);
        Button history = view.findViewById(R.id.Organizer_HomeHistoryButton);
        Button profile = view.findViewById(R.id.Organizer_HomeProfileButton);
        Button logout = view.findViewById(R.id.Organizer_HomeLogoutButton);

        browse.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_orgHome_to_browse));

        upcoming.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_orgHome_to_upcoming));

        history.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_orgHome_to_history));

        profile.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_orgHome_to_profile));

        logout.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_orgHome_to_start));

        return view;
    }
}
