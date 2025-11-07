package com.example.indeedgambling;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.navigation.Navigation;

public class Entrant_HomeFragment extends Fragment {

    private String currentUserId; // ***

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_home_fragment, container, false);

        // *** Load saved user ID from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("profileId", null);

        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
            return view;
        }

        Button browseBtn = view.findViewById(R.id.btn_browse_events);
        Button historyBtn = view.findViewById(R.id.btn_view_history);
        Button notificationsBtn = view.findViewById(R.id.btn_notifications);

        // *** Navigate to Browse Events
        browseBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_entrantHomeFragment_to_entrantBrowseFragment));

        // *** Navigate to History
        historyBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_entrantHomeFragment_to_entrantHistoryFragment));

        // *** Navigate to Notifications
        notificationsBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_entrantHomeFragment_to_entrantNotificationsFragment));

        return view;
    }
}
