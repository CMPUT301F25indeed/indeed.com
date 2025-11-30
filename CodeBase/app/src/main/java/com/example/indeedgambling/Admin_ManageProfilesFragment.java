package com.example.indeedgambling;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        // Make spinner selected text white (initial)
        filterSpinner.post(() -> {
            TextView tv = (TextView) filterSpinner.getSelectedView();
            if (tv != null) tv.setTextColor(Color.WHITE);
        });

        // Re-filter when spinner value changes
        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int position, long id) {
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(Color.WHITE);
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // Recycler setup
        adapter = new ProfileAdapter(fvm);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Load profiles from ViewModel
        fvm.getProfilesLive().observe(getViewLifecycleOwner(), profiles -> {
            if (profiles != null) {
                fullList = profiles;
                applyFilters();
            }
        });

        // Open profile details on click (if your adapter supports it)
        adapter.setOnItemClickListener(profile -> {
            Bundle args = new Bundle();
            args.putString("profileID", profile.getProfileId());

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminManageProfilesFragment_to_profileDetailsFragment, args);
        });

        // Delete profile logic
        adapter.setOnDeleteClickListener(profile -> {

            View dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_confirm, null);

            TextView title = dialogView.findViewById(R.id.dialog_title);
            TextView message = dialogView.findViewById(R.id.dialog_message);
            Button cancelBtn = dialogView.findViewById(R.id.dialog_cancel);
            Button yesBtn = dialogView.findViewById(R.id.dialog_yes);

            // Set dynamic message
            message.setText("Are you sure you want to delete " + profile.getEmail() + "?");

            androidx.appcompat.app.AlertDialog dialog =
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setView(dialogView)
                            .create();

            cancelBtn.setOnClickListener(v -> dialog.dismiss());

            yesBtn.setOnClickListener(v -> {

                // ENTRANT CLEANUP
                if (profile.getRole().equalsIgnoreCase("entrant")) {
                    fvm.deleteProfileAndCleanOpenEvents(
                            profile,
                            () -> Toast.makeText(requireContext(), "Entrant removed from all events", Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                    dialog.dismiss();
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
                    dialog.dismiss();
                    return;
                }

                // DEFAULT delete
                fvm.deleteProfile(profile.getProfileId());
                dialog.dismiss();
            });

            dialog.show();
        });


        // Search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

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

            // Role filter
            boolean matchesRole;
            if (selected.equals("All")) {
                matchesRole = true;
            } else {
                matchesRole = p.getRole().equalsIgnoreCase(selected);
            }

            // Search filter (name or email)
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
