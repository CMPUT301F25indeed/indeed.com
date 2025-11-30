package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Organizer_HomeFragment extends Fragment {

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.organization_home_fragment, container, false);


        OrganizerViewModel organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        Profile organizer = organizerVM.getCurrentOrganizer();

        //Logout button setup
        Button LogoutButton = view.findViewById(R.id.Organizer_HomeLogoutButton);
        LogoutButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_orgHome_to_start));

        // Browse Button
        Button BrowseButton = view.findViewById(R.id.Organizer_HomeBrowseEventsButton);
        BrowseButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_orgHome_to_browse);
        });

        //Upcoming Events Button
        Button UpcomingEventsButton = view.findViewById(R.id.Organizer_HomeUpcomingEventsButton);
        UpcomingEventsButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_orgHome_to_upcoming);
        });

        //History Button Function
        Button HistoryButton = view.findViewById(R.id.Organizer_HomeHistoryButton);
        HistoryButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_orgHome_to_history);
        });

        //Profile Button function
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
