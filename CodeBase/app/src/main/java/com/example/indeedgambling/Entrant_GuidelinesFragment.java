package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class Entrant_GuidelinesFragment extends Fragment {

    public Entrant_GuidelinesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_guidelines_fragment, container, false);

        Button homeButton = view.findViewById(R.id.guidelines_home_button);
        homeButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrant_GuidelinesFragment_to_entrantHomeFragment)
        );

        return view;
    }
}
