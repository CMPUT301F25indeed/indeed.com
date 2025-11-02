package com.example.indeedgambling;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
public class Entrant_BrowseFragment extends Fragment {

    public Entrant_BrowseFragment() {}

    private FirebaseViewModel firebaseVM;
    private EntrantViewModel entrantVM;
    private EventsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.entrant_browse_fragment, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        Button HomeButton = view.findViewById(R.id.entrant_home_button_browse);
        HomeButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_entrant_BrowseFragment_to_entrantHomeFragment));

        RecyclerView rv = view.findViewById(R.id.entrant_events_browse);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EventsAdapter(event -> showEventPopup(event));
        rv.setAdapter(adapter);

        firebaseVM.getEventsLive().observe(getViewLifecycleOwner(), events -> {
            if (events != null) adapter.setData(events);
        });



        return view;
    }


    private void showEventPopup(Event event) {
        Toast.makeText(getContext(), "Event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
        // We will add join/leave popup later

}}

