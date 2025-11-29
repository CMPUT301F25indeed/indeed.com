package com.example.indeedgambling;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;
import java.util.stream.Collectors;

public class Admin_SystemLogsFragment extends Fragment {

    private FirebaseViewModel fvm;
    private NotificationLoggedAdapter adapter;

    private final List<Notification> fullList = new ArrayList<>();
    private final List<Notification> filteredList = new ArrayList<>();

    private Spinner filterSpinner;
    private SearchView searchView;

    // Instead of organizerIdToEmail — just store unique senderIds
    private List<String> senderIdList = new ArrayList<>();
    private String selectedSenderId = null; // null = All senders

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_view_logs_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        Button back = view.findViewById(R.id.admin_logs_back);
        back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        searchView    = view.findViewById(R.id.admin_logs_search);
        filterSpinner = view.findViewById(R.id.admin_logs_filter);
        RecyclerView recycler = view.findViewById(R.id.admin_logs_recycler);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationLoggedAdapter(fvm);
        recycler.setAdapter(adapter);

        observeNotifications();
        setupSearch();
        setupSpinner();
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Collections.singletonList("All senders")
        );
        filterSpinner.setAdapter(spinnerAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                String chosen = (String) parent.getItemAtPosition(pos);

                if (chosen.equals("All senders")) {
                    selectedSenderId = null;
                } else {
                    selectedSenderId = chosen; // the senderId string itself
                }

                applyFilters();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {
                selectedSenderId = null;
                applyFilters();
            }
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return true; }
            @Override public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });
    }

    private void observeNotifications() {
        fvm.getAllNotificationsLive().observe(getViewLifecycleOwner(), list -> {

            fullList.clear();
            if (list != null) fullList.addAll(list);

            buildSenderIdList();
            applyFilters();
        });
    }

    private void buildSenderIdList() {
        // extract unique senderIds from notifications
        senderIdList = fullList.stream()
                .map(Notification::getSenderId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        updateSpinner();
    }

    private void updateSpinner() {
        List<String> entries = new ArrayList<>();
        entries.add("All senders");
        entries.addAll(senderIdList);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                entries
        );

        filterSpinner.setAdapter(spinnerAdapter);

        if (selectedSenderId == null) {
            filterSpinner.setSelection(0); // "All senders"
        } else {
            int pos = entries.indexOf(selectedSenderId);
            if (pos >= 0) filterSpinner.setSelection(pos);
        }
    }

    private void applyFilters() {
        filteredList.clear();

        String search = searchView.getQuery() == null ?
                "" :
                searchView.getQuery().toString().trim().toLowerCase();

        for (Notification n : fullList) {

            boolean matchesSearch =
                    search.isEmpty() ||
                            (n.getMessage() != null &&
                                    n.getMessage().toLowerCase().contains(search));

            boolean matchesSender =
                    (selectedSenderId == null) ||   // show all senders
                            (n.getSenderId() != null && n.getSenderId().equals(selectedSenderId));

            if (matchesSearch && matchesSender) {
                filteredList.add(n);
            }
        }

        adapter.setNotifications(new ArrayList<>(filteredList));
    }
}
