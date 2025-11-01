package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Entrant_HomeFragment extends Fragment {

    public Entrant_HomeFragment() {}


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.entrant_home_fragment, container, false);

        ListView options = view.findViewById(R.id.entrant_home_buttons);
        Button LogoutButton = view.findViewById(R.id.entrant_logout_button_home);
        TextView greeting = view.findViewById(R.id.entrant_greeting_home);
        String[] optionsString = {"Browse", "History", "Profile"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, optionsString);
        options.setAdapter(adapter);

        EntrantViewModel EVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        FirebaseViewModel FVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);


        Entrant e = EVM.getEntrant();
        if (e != null && e.getPersonName() != null) {
            greeting.setText("Hi " + e.getPersonName());
        }

        options.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position == 0) {
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_entrant_BrowseFragment);
            } else if (position == 1) {
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_entrant_HistoryFragment);
            } else if (position == 2) {
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_entrant_ProfileFragment);
            }

        });


        LogoutButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_startUpFragment));


        return view;
    }}





// Adds entrant to firebase after logout, but this is bad cuz this becomes reduadent when we only update entrant
        // when they do something to change
        // logging in and out does not change the entrant
//        logout.setOnClickListener(b -> {
//            fvm.add(e, () ->
//                            NavHostFragment.findNavController(this).navigate(R.id.action_any_to_startUp),
//                    ex -> NavHostFragment.findNavController(this).navigate(R.id.action_any_to_startUp)
//            );
//        });


