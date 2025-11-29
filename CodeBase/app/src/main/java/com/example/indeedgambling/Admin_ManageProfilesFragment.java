package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class Admin_ManageProfilesFragment extends Fragment {

    private SearchView searchView;
    private Spinner filterSpinner;
    private RecyclerView recyclerView;
    private Button backBtn;
    private FirebaseViewModel fvm;
    private ProfileAdapter adapter;

    private List<Profile> fullList = new ArrayList<>();
    private List<Profile> filteredList = new ArrayList<>();


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

        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });


        // Recycler setup
        adapter = new ProfileAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Load profiles from ViewModel
        fvm.getProfilesLive().observe(getViewLifecycleOwner(), profiles -> {
            if (profiles != null) {
                fullList = profiles;
                applyFilters();
            }
        });


        adapter.setOnItemClickListener(profile -> {
            Bundle args = new Bundle();
            args.putString("profileID", profile.getProfileId());

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminManageProfilesFragment_to_profileDetailsFragment, args);
        });


        adapter.setOnDeleteClickListener(profile -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete this profile?\n\n" + profile.getEmail())
                    .setPositiveButton("Yes", (dialog, which) -> {

                        // ENTRANT CLEANUP
                        if (profile.getRole().equalsIgnoreCase("entrant")) {
                            fvm.deleteProfileAndCleanOpenEvents(
                                    profile,
                                    () -> Toast.makeText(requireContext(), "Entrant removed from all events", Toast.LENGTH_SHORT).show(),
                                    e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                            return;
                        }

                        // ORGANIZER CLEANUP
                        if (profile.getRole().equalsIgnoreCase("organizer")) {
                            fvm.deleteAllOrganizerEvents(
                                    profile.getProfileId(),
                                    () -> {
                                        fvm.deleteProfile(profile.getProfileId());
                                        Toast.makeText(requireContext(), "Organizer and all their events deleted", Toast.LENGTH_SHORT).show();
                                    },
                                    e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                            return;
                        }

                        // DEFAULT → simple delete (Admin cannot be deleted from adapter anyway)
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
                applyFilters();
                return true;
            }
        });


        return view;
    }


    private void applyFilters() {
        String search = searchView.getQuery().toString().toLowerCase().trim();
        String selected = filterSpinner.getSelectedItem().toString();

        filteredList.clear();

        for (Profile p : fullList) {

            // ----- ROLE FILTER -----
            boolean matchesRole;

            if (selected.equals("All")) {
                matchesRole = true;
            } else {
                matchesRole = p.getRole().equalsIgnoreCase(selected);
            }

            // ----- SEARCH FILTER -----
            boolean matchesSearch =
                    p.getPersonName().toLowerCase().contains(search) ||
                            p.getEmail().toLowerCase().contains(search);

            if (matchesRole && matchesSearch) {
                filteredList.add(p);
            }
        }

        adapter.setProfiles(filteredList);
    }

}
