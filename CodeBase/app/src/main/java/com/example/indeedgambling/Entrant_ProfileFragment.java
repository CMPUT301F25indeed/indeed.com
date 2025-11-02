package com.example.indeedgambling;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class Entrant_ProfileFragment extends Fragment {

    private EntrantViewModel entrantVM;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.entrant_profile_fragment, container, false);

        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        TextView name = v.findViewById(R.id.entrant_profile_name);
        Button backBtn = v.findViewById(R.id.entrant_profile_back);
        Button deleteBtn = v.findViewById(R.id.entrant_profile_delete);

        Profile p = entrantVM.getCurrentEntrant();
        if (p != null) {
            name.setText("Profile: " + p.getPersonName());
        } else {
            name.setText("Profile: Unknown");
        }

        backBtn.setOnClickListener(b ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrantProfileFragment_to_entrantHomeFragment)
        );

        deleteBtn.setOnClickListener(b ->
                Toast.makeText(getContext(), "Delete TBD", Toast.LENGTH_SHORT).show()
        );

        return v;
    }
}
