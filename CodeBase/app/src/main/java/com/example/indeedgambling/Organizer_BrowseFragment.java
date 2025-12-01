package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.SearchView;

public class Organizer_BrowseFragment extends Fragment {
    View view;
    private FirebaseViewModel firebaseVM;
    private OrganizerViewModel organizerVM;
    private EventsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.organization_browse_fragment, container, false);

        Button home = rootview.findViewById(R.id.org_browse_home);

        home.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_organizerBrowseFragment_to_organizerHomeFragment)
        );
        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);

        RecyclerView rv = rootview.findViewById(R.id.org_browse_recycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EventsAdapter(event -> {
            // your click code
        }, firebaseVM);


        rv.setAdapter(adapter);

        firebaseVM.getEventsLive().observe(getViewLifecycleOwner(), events -> {
            adapter.setData(events);
        });

//        SearchView search = v.findViewById(R.id.org_searchView);
//        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override public boolean onQueryTextSubmit(String q) { return false; }
//            @Override public boolean onQueryTextChange(String q) {
//                // TODO: implement search filter later
//                return true;
//            }
//        });

        return rootview;
    }
}
