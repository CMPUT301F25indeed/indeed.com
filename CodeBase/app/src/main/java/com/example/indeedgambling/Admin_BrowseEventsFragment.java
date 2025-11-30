package com.example.indeedgambling;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.indeedgambling.R;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

public class Admin_BrowseEventsFragment extends Fragment {

    private SearchView searchView;
    private ListView listView;
    private Button backBtn;

    private FirebaseViewModel firebaseVM;
    private AdminEventCardAdapter adapter;
    private final List<Event> allEvents = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin_browse_events_fragment, container, false);

        searchView = view.findViewById(R.id.admin_events_search);

        // Make search text white
        int searchTextId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);

        TextView searchText = searchView.findViewById(searchTextId);
        searchText.setTextColor(Color.WHITE);
        searchText.setHintTextColor(Color.GRAY);


        listView   = view.findViewById(R.id.admin_events_list);
        backBtn    = view.findViewById(R.id.admin_events_remove); // reuse as Back

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        adapter = new AdminEventCardAdapter(
                requireContext(),
                new ArrayList<>(),
                firebaseVM
        );
        listView.setAdapter(adapter);


        firebaseVM.getEventsLive().observe(getViewLifecycleOwner(), events -> {
            allEvents.clear();
            if (events != null) allEvents.addAll(events);
            applyFilter(searchView.getQuery() != null ? searchView.getQuery().toString() : "");
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { applyFilter(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { applyFilter(newText); return true; }
        });

        // open details when admin taps an event
        listView.setOnItemClickListener((AdapterView<?> parent, View v1, int position, long id) -> {
            Event e = (Event) parent.getItemAtPosition(position);
            Bundle args = new Bundle();
            args.putSerializable("event", e);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminEventDetailsFragment, args);
        });

        backBtn.setText("Back to Dashboard");
        backBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        return view;
    }

    private void applyFilter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<Event> filtered = new ArrayList<>();

        for (Event e : allEvents) {
            String name = e.getEventName() != null ? e.getEventName().toLowerCase() : "";
            String desc = e.getDescription() != null ? e.getDescription().toLowerCase() : "";
            if (q.isEmpty() || name.contains(q) || desc.contains(q)) {
                filtered.add(e);
            }
        }

        adapter.clear();
        adapter.addAll(filtered);
        adapter.notifyDataSetChanged();
    }
}
