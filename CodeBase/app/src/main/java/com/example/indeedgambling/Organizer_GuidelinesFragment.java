package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class Organizer_GuidelinesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the organizer guidelines layout
        return inflater.inflate(R.layout.organizer_guidelines_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup home button navigation
        Button homeButton = view.findViewById(R.id.guidelines_home_button);
        homeButton.setOnClickListener(v -> {
            // Navigate back to organizer home
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_organizer_GuidelinesFragment_to_organizerHomeFragment);
        });
    }
}