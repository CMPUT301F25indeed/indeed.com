package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class Entrant_HomeFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_home_fragment, container, false);

        // Buttons
        Button browse = view.findViewById(R.id.entrant_view_events_button);
        Button profileBtn = view.findViewById(R.id.entrant_profile_button);
        Button logout = view.findViewById(R.id.entrant_logout_button_home);

        // Browse events
        browse.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrantHome_to_browse)
        );

        // Go to profile screen
        profileBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrantHome_to_entrantProfileFragment)
        );

        // Logout
        logout.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrantHome_to_start)
        );

        return view;
    }
}
