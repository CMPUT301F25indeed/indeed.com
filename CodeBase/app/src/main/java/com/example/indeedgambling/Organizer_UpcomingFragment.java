package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.api.ResourceDescriptor;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class Organizer_UpcomingFragment extends Fragment {

    private EventHandler EH;

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.organization_upcomingevents_fragment, container, false);
        EH = new EventHandler();

        //HomeButton Function
        Button Home = view.findViewById(R.id.Organizer_Upcoming_HomeButton);
        Home.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_organizerUpcomingFragment_to_organizerHomeFragment);
        });


        EH.AddEvent(new Event("test1",new Date(), new Date(),new Organizer("diggle","piggle")));

        //Displaying Organizer's events
        ListView Events = view.findViewById(R.id.Organizer_UpcomingEventList);
        List<Event> eventsList = EH.GetEvents(); //Placeholder until profile handling added
        ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, eventsList);

        Events.setAdapter(adapter);




        return view;
    }
}
