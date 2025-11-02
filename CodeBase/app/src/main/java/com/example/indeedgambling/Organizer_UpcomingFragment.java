package com.example.indeedgambling;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class Organizer_UpcomingFragment extends Fragment {

    private OrganizerViewModel organizerVM;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.organization_upcomingevents_fragment, container, false);

        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);

        ListView list = view.findViewById(R.id.Organizer_UpcomingEventList);

        Button home = view.findViewById(R.id.Organizer_Upcoming_HomeButton);
        Button newEvent = view.findViewById(R.id.Organizer_Upcoming_NewEventButton);

        home.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.organizerHomeFragment)
        );

        newEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.organizerCreateEventFragment)
        );

        return view;
    }
}
