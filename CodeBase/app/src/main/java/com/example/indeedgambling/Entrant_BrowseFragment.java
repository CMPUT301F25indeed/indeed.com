package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Entrant_BrowseFragment extends Fragment implements EventsAdapter.OnEventClick {

    private FirebaseViewModel firebaseVM;
    private EventsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.entrant_browse_fragment, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        RecyclerView recyclerView = v.findViewById(R.id.entrant_events_browse);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventsAdapter(this);
        recyclerView.setAdapter(adapter);

        firebaseVM.getEventsLive().observe(getViewLifecycleOwner(), events -> {
            if (events != null) adapter.setData(events);
        });

        Button homeBtn = v.findViewById(R.id.entrant_home_button_browse);
        homeBtn.setOnClickListener(view ->
                NavHostFragment.findNavController(Entrant_BrowseFragment.this)
                        .navigate(R.id.action_entrant_BrowseFragment_to_entrantHomeFragment));

        return v;
    }

    @Override
    public void clicked(Event e) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", e);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_entrant_BrowseFragment_to_eventDetailsFragment, bundle);
    }
}
