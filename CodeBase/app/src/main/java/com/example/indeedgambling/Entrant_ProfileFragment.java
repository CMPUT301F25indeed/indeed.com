package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class Entrant_ProfileFragment extends Fragment {
    public Entrant_ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.entrant_profile_fragment, container, false);

        Button HomeButton = view.findViewById(R.id.entrant_home_button_profile);

        HomeButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_entrant_ProfileFragment_to_entrantHomeFragment));


        return view;
    }


}
