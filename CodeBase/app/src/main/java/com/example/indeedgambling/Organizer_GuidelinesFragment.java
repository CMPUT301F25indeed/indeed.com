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
/**
 * Fragment that displays guidelines and best practices for event organizers.
 *
 * <p>This fragment provides organizers with essential information about
 * how to effectively use the application, manage events, and interact
 * with entrants,etc.
 *
 * <p>Content includes:
 * <ul>
 *   <li>Event creation and management guidelines</li>
 *   <li>Waitlist and invitation protocols</li>
 *   <li>Participant tracking best practices</li>
 *   <li>Notification and communication standards</li>
 *   <li>Export and reporting procedures</li>
 * </ul>
 *
 * @see Organizer_HomeFragment
 * @see Organizer_UpcomingFragment
 */


public class Organizer_GuidelinesFragment extends Fragment {
    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * <p>Inflates the organizer guidelines layout which contains
     * instructional content for organizers.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     *                 any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's
     *                  UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state
     * @return The inflated View containing the guidelines content
     */


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
