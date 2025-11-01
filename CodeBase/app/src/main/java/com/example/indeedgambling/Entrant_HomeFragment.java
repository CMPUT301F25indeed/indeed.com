package com.example.indeedgambling;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class Entrant_HomeFragment extends Fragment {
    public Entrant_HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.entrant_home_fragment, container, false);

        TextView title = v.findViewById(R.id.entrant_title);
        Button viewEvents = v.findViewById(R.id.btn_view_events);
        Button logout = v.findViewById(R.id.btn_logout);

        EntrantViewModel evm = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        FirebaseViewModel fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        Entrant e = evm.getEntrant();
        if (e != null && e.getPersonName() != null) title.setText("Hi " + e.getPersonName());

        viewEvents.setOnClickListener(b ->
                NavHostFragment.findNavController(this).navigate(R.id.action_entrantHome_to_eventsList)
        );

        logout.setOnClickListener(b -> {
            fvm.add(e, () ->
                            NavHostFragment.findNavController(this).navigate(R.id.action_any_to_startUp),
                    ex -> NavHostFragment.findNavController(this).navigate(R.id.action_any_to_startUp)
            );
        });

        return v;
    }
}
