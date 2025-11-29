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

    private List<String> senderEmailList = new ArrayList<>();
    private String selectedEmailOrSystem = null; // null = All senders

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
        adapter = new NotificationLoggedAdapter();
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
                    selectedEmailOrSystem = null;
                } else if (chosen.equals("System")) {
                    selectedEmailOrSystem = "SYSTEM"; // special marker
                } else {
                    selectedEmailOrSystem = chosen; // the email string
                }

                applyFilters();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {
                selectedEmailOrSystem = null;
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

            buildSenderEmailList();
            applyFilters();
        });
    }

    private void buildSenderEmailList() {
        // extract unique senderEmails from notifications
        senderEmailList = fullList.stream()
                .map(Notification::getSenderEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        updateSpinner();
    }

    private void updateSpinner() {
        List<String> entries = new ArrayList<>();
        entries.add("All senders");
        entries.addAll(senderEmailList);
        entries.add("System"); // extra tab for system notifications

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                entries
        );
        filterSpinner.setAdapter(spinnerAdapter);

        if (selectedEmailOrSystem == null) {
            filterSpinner.setSelection(0);
        } else if (selectedEmailOrSystem.equals("SYSTEM")) {
            filterSpinner.setSelection(entries.indexOf("System"));
        } else {
            int pos = entries.indexOf(selectedEmailOrSystem);
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

            boolean matchesFilter;

            if (selectedEmailOrSystem == null) {
                matchesFilter = true; // all notifications
            } else if (selectedEmailOrSystem.equals("SYSTEM")) {
                matchesFilter = "system".equalsIgnoreCase(n.getSenderId());
            } else {
                matchesFilter = selectedEmailOrSystem.equals(n.getSenderEmail());
            }

            if (matchesSearch && matchesFilter) {
                filteredList.add(n);
            }
        }

        adapter.setNotifications(new ArrayList<>(filteredList));
    }
}
