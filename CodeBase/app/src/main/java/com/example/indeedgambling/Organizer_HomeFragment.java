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
            NavHostFragment.findNavController(this).navigate(R.id.action_orgHome_to_profile);
        });


        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //viewModel = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);

        setupNotificationButton(view); // 🎯 ADD THIS LINE

        // ... your existing code ...
    }
    private void setupNotificationButton(View view) {
        view.findViewById(R.id.button_open_notifications).setOnClickListener(v -> {
            // Navigate to NotificationSenderFragment
            NotificationSenderFragment fragment = new NotificationSenderFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment) // Use your actual container ID
                    .addToBackStack(null)
                    .commit();
        });
    }
}
