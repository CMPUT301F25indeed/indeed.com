package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.HashMap;
import java.util.Map;

public class Entrant_ProfileFragment extends Fragment {

    private EntrantViewModel entrantVM;
    private FirebaseViewModel fvm;

    public Entrant_ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.entrant_profile_fragment, container, false);

        // Initialize ViewModels
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        // UI Elements
        Button homeButton = view.findViewById(R.id.entrant_home_profile);
        Button saveButton = view.findViewById(R.id.entrant_save_profile);
        Button deleteButton = view.findViewById(R.id.entrant_delete_profile);
        Switch notiSwitch = view.findViewById(R.id.entrant_notification_switch_profile);

        EditText nameEdit = view.findViewById(R.id.entrant_name_profile);
        EditText emailEdit = view.findViewById(R.id.entrant_email_profile);
        EditText phoneEdit = view.findViewById(R.id.entrant_phone_profile);

        // Pre-fill EditTexts from ViewModel
        Profile p = entrantVM.getCurrentEntrant();
        if (p != null) {
            nameEdit.setText(p.getPersonName());
            emailEdit.setText(p.getEmail());
            phoneEdit.setText(p.getPhone());

            // Set switch state based on the entrant’s current notification preference
            boolean notificationsEnabled = Boolean.TRUE.equals(p.isNotificationsEnabled());
            notiSwitch.setChecked(notificationsEnabled);
        }

        String profileId = p != null ? p.getProfileId() : null;

        // Handle switch toggle
        notiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (p != null) {
                p.setNotificationsEnabled(isChecked);
                entrantVM.setEntrant(p);

                // Update switch color immediately
                updateSwitchColor(notiSwitch, isChecked);

                // Optionally, update Firestore right away
                if (profileId != null) {
                    Map<String, Object> update = new HashMap<>();
                    update.put("notificationsEnabled", isChecked);
                    fvm.updateProfile(profileId, update,
                            () -> Toast.makeText(getContext(),
                                    "Notifications " + (isChecked ? "enabled" : "disabled"),
                                    Toast.LENGTH_SHORT).show(),
                            error -> Toast.makeText(getContext(),
                                    "Update failed: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                }
            }
        });

        // Save button logic
        saveButton.setOnClickListener(v -> {
            if (p == null) return;

            String newName = nameEdit.getText().toString().trim();
            String newEmail = emailEdit.getText().toString().trim();
            String newPhone = phoneEdit.getText().toString().trim();

            // Validation
            if (newName.isEmpty()) {
                nameEdit.setError("Name required");
                nameEdit.requestFocus();
                return;
            }

            if (newEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                emailEdit.setError("Valid email required");
                emailEdit.requestFocus();
                return;
            }

            if (!newPhone.isEmpty() && (!Patterns.PHONE.matcher(newPhone).matches() || newPhone.length() < 10)) {
                phoneEdit.setError("Valid phone number required");
                phoneEdit.requestFocus();
                return;
            }

            // Only update changed fields
            Map<String, Object> updates = new HashMap<>();
            if (!newName.equals(p.getPersonName())) updates.put("personName", newName);
            if (!newEmail.equals(p.getEmail())) updates.put("email", newEmail);
            if (!newPhone.equals(p.getPhone())) updates.put("phone", newPhone);

            if (!updates.isEmpty() && profileId != null) {
                fvm.updateProfile(profileId, updates,
                        () -> {
                            // Success
                            p.setPersonName(newName);
                            p.setEmail(newEmail);
                            p.setPhone(newPhone);
                            entrantVM.setEntrant(p);
                            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                        },
                        error -> Toast.makeText(getContext(), "Update failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } else {
                Toast.makeText(getContext(), "No changes to save", Toast.LENGTH_SHORT).show();
            }
        });

        // Home button
        homeButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrant_ProfileFragment_to_entrantHomeFragment)
        );

        // Delete button
        deleteButton.setOnClickListener(b ->
                Toast.makeText(getContext(), "Delete TBD", Toast.LENGTH_SHORT).show()
        );

        // Make sure switch color matches initial state
        updateSwitchColor(notiSwitch, notiSwitch.isChecked());

        return view;
    }

    /**
     * Updates the switch tint dynamically (green when ON, grey when OFF)
     */
    private void updateSwitchColor(Switch sw, boolean isOn) {
        int color = getResources().getColor(
                isOn ? android.R.color.holo_green_light : android.R.color.darker_gray,
                requireContext().getTheme()
        );
        sw.getThumbDrawable().setTint(color);
        sw.getTrackDrawable().setTint(color);
    }
}
