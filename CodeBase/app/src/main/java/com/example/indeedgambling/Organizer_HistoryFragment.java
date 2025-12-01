/**
 * Fragment that displays the organizer's event history.
 *
 * Features:
 * - Shows the history screen for organizers
 * - Provides navigation back to the organizer home page
 *
 * Notes:
 * - Layout file: organization_history_fragment.xml
 */
package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class Organizer_HistoryFragment extends Fragment {

    /**
     * Inflates the organizer history UI and sets up navigation.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.organization_history_fragment, container, false);

        Button home = view.findViewById(R.id.org_history_home);
        home.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_organizerHistoryFragment_to_organizerHomeFragment)
        );

        return view;
    }
}
