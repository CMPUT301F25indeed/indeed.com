package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Entrant Home screen fragment display and logic.
 * Displays a greeting, a list of navigation options (Browse, History, Profile, Guidelines),
 * and a logout button for now.
 */
public class Entrant_HomeFragment extends Fragment {

    /**
     * ViewModel holding the current Entrant data.
     */
    private EntrantViewModel entrantVM;

    /**
     * Default constructor.
     */
    public Entrant_HomeFragment() {}

    /**
     * Sets up home screen fragment for Entrant.
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

        View view = inflater.inflate(R.layout.entrant_home_fragment, container, false);

        ListView options = view.findViewById(R.id.entrant_home_buttons);
        Button LogoutButton = view.findViewById(R.id.entrant_logout_button_home);
        TextView greeting = view.findViewById(R.id.entrant_greeting_home);
        String[] optionsString = {"Browse", "History", "Profile","Guidelines"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, optionsString);
        options.setAdapter(adapter);

        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        // Firebase so below line irrelevant for now
        //FirebaseViewModel fwm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        Profile e = entrantVM.getCurrentEntrant();
        greeting.setText("Hi " + e.getPersonName());

        // Handle option clicks
        options.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position == 0) { // To browse
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_entrant_BrowseFragment);
            } else if (position == 1) { // To History
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_entrant_HistoryFragment);
            } else if (position == 2) { // To Profile
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_entrant_ProfileFragment);
            } else if (position == 3) { // To Guidelines
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_entrant_GuidelinesFragment);
            }
        });


        LogoutButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_startUpFragment));

        return view;
    }
}
