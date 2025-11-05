package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Displaying guidelines for entrant fragment.
 * Contains a button to navigate back to the Entrant Home screen and a bunch of text.
 */
public class Entrant_GuidelinesFragment extends Fragment {

    /**
     * Default constructor.
     */
    public Entrant_GuidelinesFragment() {}

    /**
     * Sets up up guidelines fragment for entrants.
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
