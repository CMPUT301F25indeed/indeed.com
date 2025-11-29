package com.example.indeedgambling;

import android.app.AlertDialog;
import android.graphics.Color;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        adapter = new EventsAdapter(this, firebaseVM);

        recyclerView.setAdapter(adapter);

        firebaseVM.getEventsLive().observe(getViewLifecycleOwner(), events -> {
            if (events != null) adapter.setData(events);
        });

        Button homeBtn = v.findViewById(R.id.entrant_home_button_browse);
        homeBtn.setOnClickListener(view ->
                NavHostFragment.findNavController(Entrant_BrowseFragment.this)
                        .navigate(R.id.action_entrant_BrowseFragment_to_entrantHomeFragment));

        // 🔹 Added: Filter button click opens dialog
        Button filterBtn = v.findViewById(R.id.entrant_filter_button_browse);
        filterBtn.setOnClickListener(view -> showFilterDialog());

        return v;
    }

    @Override
    public void clicked(Event e) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", e);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_entrant_BrowseFragment_to_eventDetailsFragment, bundle);
    }

    // ----------------------------------------------------
    //  Filter feature for US 01.01.04 (Hardcoded categories)
    // ----------------------------------------------------

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter_events, null);

        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category);
        EditText startDateEdit = dialogView.findViewById(R.id.edit_start_date);
        EditText endDateEdit = dialogView.findViewById(R.id.edit_end_date);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnApply = dialogView.findViewById(R.id.btn_apply);

        // ✅ Hardcoded categories (no duplicates)
        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("Sports");
        categories.add("Music");
        categories.add("Art");
        categories.add("Education");
        categories.add("Technology");
        categories.add("Health");
        categories.add("Fitness");
        categories.add("Dance");
        categories.add("Cooking");
        categories.add("Travel");
        categories.add("Photography");
        categories.add("Film");
        categories.add("Theatre");
        categories.add("Community");
        categories.add("Charity");
        categories.add("Business");
        categories.add("Career");
        categories.add("Science");
        categories.add("Literature");
        categories.add("Games");
        categories.add("Workshop");
        categories.add("Festival");
        categories.add("Outdoor");
        categories.add("Food & Drinks");
        categories.add("Other");

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(categoryAdapter);

        // Custom date+time picker
        startDateEdit.setOnClickListener(v -> showCustomDateTimeDialog(startDateEdit));
        endDateEdit.setOnClickListener(v -> showCustomDateTimeDialog(endDateEdit));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            String selectedCategory = categorySpinner.getSelectedItem().toString();
            String startText = startDateEdit.getText().toString().trim();
            String endText = endDateEdit.getText().toString().trim();

            Date startDate = null, endDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            try {
                if (!startText.isEmpty()) startDate = sdf.parse(startText);
                if (!endText.isEmpty()) endDate = sdf.parse(endText);
            } catch (Exception e) {
                e.printStackTrace();
            }

            firebaseVM.fetchEventsByCategoryAndDate(selectedCategory, startDate, endDate, events -> {
                if (events != null && !events.isEmpty()) {
                    adapter.setData(events);
                } else {
                    adapter.setData(new ArrayList<>());
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }, err -> {
                Toast.makeText(getContext(), "Filter failed: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showCustomDateTimeDialog(EditText target) {
        View pickerView = LayoutInflater.from(getContext()).inflate(R.layout.datetime_picker, null);
        DatePicker datePicker = pickerView.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker timePicker = pickerView.findViewById(R.id.DateTimePicker_TimeDialog);

        datePicker.setMinDate(System.currentTimeMillis() - 1000);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(pickerView)
                .setPositiveButton("OK", (d, which) -> {
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth();
                    int day = datePicker.getDayOfMonth();
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, day, hour, minute);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    target.setText(sdf.format(calendar.getTime()));
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }
}
