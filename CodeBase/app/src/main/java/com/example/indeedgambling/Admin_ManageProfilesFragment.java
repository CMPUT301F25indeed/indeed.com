package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

public class Admin_ManageProfilesFragment extends Fragment {

    private SearchView searchView;
    private Spinner filterSpinner;
    private RecyclerView recyclerView;
    private Button backBtn;
    private FirebaseViewModel fvm;
    private ProfileAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin_browse_profiles_fragment, container, false);

        // UI refs
        searchView = view.findViewById(R.id.admin_profiles_search);
        filterSpinner = view.findViewById(R.id.admin_profiles_filter);
        recyclerView = view.findViewById(R.id.admin_profiles_recycler);
        backBtn = view.findViewById(R.id.admin_browse_back);

        // ViewModel
        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        // Back button
        backBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        // Spinner options
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Entrant", "Organizer", "Admin"}
        );
        filterSpinner.setAdapter(spinnerAdapter);

        // Recycler setup
        adapter = new ProfileAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Load profiles from ViewModel
        fvm.getProfilesLive().observe(getViewLifecycleOwner(), profiles -> {
            if (profiles != null) {
                adapter.setProfiles(profiles);
            }
        });


        adapter.setOnDeleteClickListener(profile -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete this profile?\n\n" + profile.getEmail())
                    .setPositiveButton("Yes", (dialog, which) -> {
                        fvm.deleteProfile(profile.getProfileId());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


        // Search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO: add search filter later
                return false;
            }
        });

        return view;
    }
}
