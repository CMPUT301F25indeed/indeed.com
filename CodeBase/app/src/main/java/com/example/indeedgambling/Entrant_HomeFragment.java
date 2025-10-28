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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Entrant_HomeFragment extends Fragment {
    private FirebaseFirestore db;

    private CollectionReference testRef;


    public Entrant_HomeFragment() {}


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_home_fragment, container, false);

        ListView options = view.findViewById(R.id.entrant_home_buttons);
        String[] optionsString = {"Browse", "History", "Profile"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, optionsString);
        options.setAdapter(adapter);

        options.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position == 0) {
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_entrant_BrowseFragment);
            } else if (position == 1) {
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_entrant_HistoryFragment);
            } else if (position == 2) {
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_entrant_ProfileFragment);
            }

        });

        Button LogoutButton = view.findViewById(R.id.entrant_logout_button_home);
        LogoutButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHomeFragment_to_startUpFragment));


        return view;

   


}}
