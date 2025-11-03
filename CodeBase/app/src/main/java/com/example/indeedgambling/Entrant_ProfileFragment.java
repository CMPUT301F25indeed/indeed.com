package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class Entrant_ProfileFragment extends Fragment {
    public Entrant_ProfileFragment() {}

    private EntrantViewModel entrantVM;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.entrant_profile_fragment, container, false);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        Button HomeButton = view.findViewById(R.id.entrant_home_button_profile);

        HomeButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_entrant_ProfileFragment_to_entrantHomeFragment));

        TextView name = view.findViewById(R.id.entrant_name_profile);
        Button delete = view.findViewById(R.id.entrant_delete_profile);

        Profile p = entrantVM.getCurrentEntrant();
        if (p != null) {
            name.setText("Profile: " + p.getPersonName());
        } else {
            name.setText("Profile: Unknown");
        }

        delete.setOnClickListener(b ->
                Toast.makeText(getContext(), "Delete TBD", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

}
