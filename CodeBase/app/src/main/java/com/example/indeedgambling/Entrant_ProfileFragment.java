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

/**
 * Entrant profile screen fragment display and logic.
 * Allows the user to view and edit their personal information,
 * toggle notification preferences, save changes, navigate home, and delete profile.
 */
public class Entrant_ProfileFragment extends Fragment {

    /**
     * ViewModel holding the current Entrant data for their fragments.
     */
    private EntrantViewModel entrantVM;

    /**
     * ViewModel for interacting with Firebase.
     */
    private FirebaseViewModel fvm;

    /**
     * Default constructor.
     */
    public Entrant_ProfileFragment() {}


    /**
     * Sets up profile screen for entrants.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view itself.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from
     *                           a previous saved state as given here.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // set up
        View view = inflater.inflate(R.layout.entrant_profile_fragment, container, false);

        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        Button homeButton = view.findViewById(R.id.entrant_home_profile);
        Button saveButton = view.findViewById(R.id.entrant_save_profile);
        Button deleteButton = view.findViewById(R.id.entrant_delete_profile);
        Switch notiSwitch = view.findViewById(R.id.entrant_notification_switch_profile);

        EditText nameEdit = view.findViewById(R.id.entrant_name_profile);
        EditText emailEdit = view.findViewById(R.id.entrant_email_profile);
        EditText phoneEdit = view.findViewById(R.id.entrant_phone_profile);

        Entrant e = entrantVM.getCurrentEntrant();
        if (e != null) {
            nameEdit.setText(e.getPersonName());
            emailEdit.setText(e.getEmail());
            phoneEdit.setText(e.getPhone());

            // set switch state based on current notification preference
            boolean notificationsEnabled = Boolean.TRUE.equals(e.isNotificationsEnabled());
            notiSwitch.setChecked(notificationsEnabled);
        }
        String profileId = e != null ? e.getProfileId() : null;

        // switch toggles
        notiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (e == null) {return;}

            e.setNotificationsEnabled(isChecked);
            entrantVM.setEntrant(e);

            updateSwitchColor(notiSwitch, isChecked);

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
        });

        // save changes to personal data
        saveButton.setOnClickListener(v -> {
            if (e == null) {return;}

            String newName = nameEdit.getText().toString().trim();
            String newEmail = emailEdit.getText().toString().trim();
            String newPhone = phoneEdit.getText().toString().trim();

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

            // only update changed stuff
            Map<String, Object> updates = new HashMap<>();
            if (!newName.equals(e.getPersonName())) updates.put("personName", newName);
            if (!newEmail.equals(e.getEmail())) updates.put("email", newEmail);
            if (!newPhone.equals(e.getPhone())) updates.put("phone", newPhone);

            // check if changed emailed is not the same to check if we are maybe creating a dup
            if (!newEmail.equals(e.getEmail())) {
                fvm.checkEmailExists(newEmail, exists -> {
                    if (exists) {
                        emailEdit.setError("Email already in use");
                        emailEdit.requestFocus();
                    } else {
                        performProfileUpdate(e, profileId, newName, newEmail, newPhone, updates);
                    }
                }, err -> Toast.makeText(getContext(), "Error checking email: " + err.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                performProfileUpdate(e, profileId, newName, newEmail, newPhone, updates);
            }
        });

        // home
        homeButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrantProfileFragment_to_entrantHomeFragment)
        );

        // delete
        deleteButton.setOnClickListener(v -> {
            if (e == null || profileId == null) {
                Toast.makeText(getContext(), "No profile loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        fvm.deleteProfileAndCleanOpenEvents(
                                e, // pass the full entrant object (Profile-compatible)
                                () -> {
                                    entrantVM.setEntrant(null);
                                    Toast.makeText(getContext(), "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                                    NavHostFragment.findNavController(this)
                                            .navigate(R.id.action_entrantProfileFragment_to_startUpFragment);
                                },
                                err -> Toast.makeText(getContext(),
                                        "Failed to delete: " + err.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        );
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });



        // matches colors to noti settings
        updateSwitchColor(notiSwitch, notiSwitch.isChecked());

        return view;
    }

    /**
     * Executes the actual Firestore update after validation and duplicate email check.
     *
     * This method performs the update in Firestore once all input has been validated
     * and the email is confirmed to be unique (if modified). It updates the user's
     * Firestore document with the provided field changes, updates the local ViewModel,
     * and provides user feedback via Toast messages.
     *
     * @param e         The Entrant object representing the current user's data.
     * @param profileId The Firestore document ID associated with this Entrant.
     * @param newName   The new name entered by the user.
     * @param newEmail  The new email entered by the user.
     * @param newPhone  The new phone number entered by the user.
     * @param updates   A map of field names and values containing only modified data to update in Firestore.
     */
    private void performProfileUpdate(Entrant e, String profileId, String newName, String newEmail, String newPhone, Map<String, Object> updates) {
        if (!updates.isEmpty() && profileId != null) {
            fvm.updateProfile(profileId, updates,
                    () -> {
                        e.setPersonName(newName);
                        e.setEmail(newEmail);
                        e.setPhone(newPhone);
                        entrantVM.setEntrant(e);
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    },
                    error -> Toast.makeText(getContext(), "Update failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );
        } else {
            Toast.makeText(getContext(), "No changes to save", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the switch colors (green when ON, grey when OFF)
     *
     * @param sw   The Switch to update
     * @param isOn True if the switch is ON, false if OFF
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
