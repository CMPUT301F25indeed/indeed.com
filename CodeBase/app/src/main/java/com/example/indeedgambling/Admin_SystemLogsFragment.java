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

/**
 * Admin-only screen for viewing, searching, filtering, and deleting system-wide notifications.
 * Shows all {@link Notification} objects stored in Firestore with:
 * - Live search by message content
 * - Filter by sender: All / individual user emails / System
 * - Tap item → detailed popup with event, sender, timestamp, and type
 * - Delete button → confirmation dialog + permanent removal
 * The sender filter dynamically updates when new notifications are loaded.
 */
public class Admin_SystemLogsFragment extends Fragment {

    private FirebaseViewModel fvm;
    private NotificationLoggedAdapter adapter;

    private final List<Notification> fullList = new ArrayList<>();
    private final List<Notification> filteredList = new ArrayList<>();

    private Spinner filterSpinner;
    private SearchView searchView;

    private List<String> senderEmailList = new ArrayList<>();
    private String selectedEmailOrSystem = null; // null = All senders


    /**
     * Inflates the fragment layout.
     */
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

        // ---- NEW ---- set up Remove + Item click ----
        adapter.setOnRemoveClickListener(notification -> showDeleteConfirmation(notification));
        adapter.setOnItemClickListener(notification -> showNotificationDetails(notification));

        observeNotifications();
        setupSearch();
        setupSpinner();
    }

    /**
     * Shows a confirmation dialog before permanently deleting a notification from Firestore.
     * @param notification the notification to be deleted
     */
    private void showDeleteConfirmation(Notification notification) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);

        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        Button cancelBtn = dialogView.findViewById(R.id.dialog_cancel);
        Button yesBtn = dialogView.findViewById(R.id.dialog_yes);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        yesBtn.setOnClickListener(v -> {
            fvm.deleteNotificationById(notification.getId());
            dialog.dismiss();
        });

        dialog.show();
    }


    /**
     * Displays a read-only popup with full details of a notification.
     * @param notification the notification to display
     */
    private void showNotificationDetails(Notification notification) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_notification_popup, null);

        TextView eventView   = dialogView.findViewById(R.id.dialog_noti_event);
        TextView senderView  = dialogView.findViewById(R.id.dialog_noti_sender);
        TextView messageView = dialogView.findViewById(R.id.dialog_noti_message);
        TextView timeView    = dialogView.findViewById(R.id.dialog_noti_time);
        TextView typeView    = dialogView.findViewById(R.id.dialog_noti_type);
        Button okButton      = dialogView.findViewById(R.id.dialog_noti_ok);

        eventView.setText(notification.getEventName() != null ? notification.getEventName() : "N/A");
        senderView.setText(notification.getSenderEmail() != null ? notification.getSenderEmail() : "system");
        messageView.setText(notification.getMessage() != null ? notification.getMessage() : "");
        timeView.setText(formatTimestamp(notification.getTimestamp()));
        typeView.setText(notification.getType() != null ? notification.getType() : "N/A");

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        okButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    /**
     * Formats a notification timestamp into a human-readable string.
     * @param date the timestamp to format, may be null
     * @return formatted date string or empty string if date is null
     */
    private String formatTimestamp(Date date) {
        if (date == null) return "";

        long diff = new Date().getTime() - date.getTime();
        long mins = diff / (1000 * 60);
        long hours = mins / 60;
        long days = hours / 24;


        return new java.text.SimpleDateFormat("MMM d, yyyy • h:mm a", java.util.Locale.getDefault()).format(date);
    }


    /**
     * Configures the sender filter spinner and handles selection changes.
     * Spinner options are dynamically rebuilt whenever the notification list changes.
     */
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

    /**
     * Enables live search filtering by message content.
     */
    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return true; }
            @Override public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });
    }

    /**
     * Starts observing the full list of notifications via LiveData.
     * Triggers filter and spinner rebuild on every update.
     */
    private void observeNotifications() {
        fvm.getAllNotificationsLive().observe(getViewLifecycleOwner(), list -> {

            fullList.clear();
            if (list != null) fullList.addAll(list);

            buildSenderEmailList();
            applyFilters();
        });
    }

    /**
     * Extracts unique sender emails from the current notification list.
     * Used to populate the sender filter spinner.
     */
    private void buildSenderEmailList() {
        // extract unique senderEmails from notifications
        senderEmailList = fullList.stream()
                .map(Notification::getSenderEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        updateSpinner();
    }

    /**
     * Rebuilds the spinner dropdown with:
     * - "All senders"
     * - Unique user emails
     * - "System"
     * Preserves current selection when possible.
     */
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

    /**
     * Applies both the search query and sender filter to {@link #fullList}
     * and updates the adapter with the filtered result.
     */
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
