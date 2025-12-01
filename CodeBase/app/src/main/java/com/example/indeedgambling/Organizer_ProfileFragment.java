/**
 * Displays the Organizer profile page.
 *
 * This fragment shows the organizer's profile information
 * and provides a simple navigation option to return to the
 * Organizer home screen. The profile content itself is static,
 * as organizers do not edit profile details here (unlike entrants).
 *
 * Navigation:
 * - Back button → returns to Organizer_HomeFragment
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

public class Organizer_ProfileFragment extends Fragment {

    /**
     * Inflates the layout and initializes navigation actions.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.organization_profile_fragment, container, false);

        /**
         * Returns to organizer home.
         */
        Button back = view.findViewById(R.id.org_profile_back);
        back.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_organizerProfileFragment_to_organizerHomeFragment)
        );

        return view;
    }
}
