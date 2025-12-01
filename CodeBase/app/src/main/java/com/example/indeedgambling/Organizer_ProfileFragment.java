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
 * Fragment that displays the organizer's profile information.
 *
 * <p>This fragment provides a simple interface for organizers to view
 * their profile details and navigate back to the home screen.
 *
 * <p>Primary functionality:
 * <ul>
 *   <li>Displays organizer profile information</li>
 *   <li>Provides navigation back to organizer home screen</li>
 * </ul>
 *
 * @see Organizer_HomeFragment
 */
public class Organizer_ProfileFragment extends Fragment {
    /**
     * Fragment that displays the organizer's profile information.
     *
     * <p>This fragment provides a simple interface for organizers to view
     * their profile details and they can navigate back to home screen.
     *
     * <p>Primary functionality:
     * <ul>
     *   <li>Displays organizer profile information</li>
     *   <li>Provides navigation back to organizer home screen</li>
     * </ul>
     *
     * @see Organizer_HomeFragment
     */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the organizer profile layout
        View view = inflater.inflate(R.layout.organization_profile_fragment, container, false);
        // Initialize the back button
        Button back = view.findViewById(R.id.org_profile_back);

        back.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_organizerProfileFragment_to_organizerHomeFragment)
        );

        return view;
    }
}
