/**
 * Fragment that allows users to modify personal account settings.
 *
 * This screen lets Entrants, Organizers, and Admins edit profile details such
 * as name, email, phone number, notification preferences, and profile image.
 *
 * Features:
 * - Loads profile information from Firestore based on profileID
 * - Allows profile picture upload (stored as Base64 in /images collection)
 * - Restricts delete and notification settings to Entrants only
 * - Validates email and phone input before applying updates
 * - Updates ViewModels so all UI layers reflect changes immediately
 *
 * Behavior Notes:
 * - Entrants can delete their profile; Organizers/Admins cannot
 * - Email uniqueness is checked before saving
 * - Image uploads are compressed and stored as Base64 strings
 */
package com.example.indeedgambling;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class SettingsFragment extends Fragment {

    private FirebaseViewModel fvm;
    public String profileID, role, name, email, phone, imageUrl;

    private EntrantViewModel entrantVM;
    private OrganizerViewModel organizerVM;
    private AdminViewModel adminVM;

    private Uri selectedPfpUri = null;

    private EditText nameEdit, emailEdit, phoneEdit;
    private boolean initialNotifications;
    private Switch notificationsSwitch;
    private ImageView profileImage;

    private ActivityResultLauncher<String> imagePickerLauncher;

    public SettingsFragment() {}

    /**
     * Retrieves navigation arguments and registers the image picker launcher.
     *
     * @param savedInstanceState previous state, if any
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            profileID = getArguments().getString("profileID");
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedPfpUri = uri;
                        if (profileImage != null) {
                            profileImage.setImageURI(uri);
                        }
                    }
                }
        );
    }

    /**
     * Inflates the settings screen and initializes UI components and ViewModels.
     * Also loads all user profile data and applies role-based UI restrictions.
     *
     * @param inflater layout inflater
     * @param container parent container
     * @param savedInstanceState previous saved state
     * @return inflated settings view
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        adminVM = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        Button backButton = view.findViewById(R.id.back_button);
        Button deleteProfileButton = view.findViewById(R.id.delete_profile_button);
        Button saveButton = view.findViewById(R.id.save_settings_button);

        profileImage = view.findViewById(R.id.pfp_image);
        Button editPfpButton = view.findViewById(R.id.pfp_edit_button);

        nameEdit = view.findViewById(R.id.settings_name);
        emailEdit = view.findViewById(R.id.settings_email);
        phoneEdit = view.findViewById(R.id.settings_phone);

        notificationsSwitch = view.findViewById(R.id.settings_notif_switch);

        LinearLayout notifLayout = view.findViewById(R.id.noti_layout);

        /**
         * Loads profile information from Firestore, initializes UI fields,
         * and applies restrictions depending on user role.
         */
        fvm.getProfileById(profileID, profile -> {
            role = profile.getRole().trim().toLowerCase();
            name = profile.getPersonName();
            email = profile.getEmail();
            phone = profile.getPhone();

            nameEdit.setText(name);
            emailEdit.setText(email);
            phoneEdit.setText(phone);

            boolean isEntrant = role.equals("entrant");
            deleteProfileButton.setVisibility(isEntrant ? View.VISIBLE : View.GONE);
            notifLayout.setVisibility(isEntrant ? View.VISIBLE : View.GONE);

            initialNotifications = Boolean.TRUE.equals(profile.isNotificationsEnabled());
            notificationsSwitch.setChecked(initialNotifications);
            updateSwitchColor(notificationsSwitch, initialNotifications);

            imageUrl = profile.getProfileImageUrl();
            loadProfileImage(imageUrl);

        }, error -> error.printStackTrace());

        /**
         * Launches image picker for profile picture selection.
         */
        editPfpButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        /**
         * Updates switch color each time the user toggles notifications.
         */
        notificationsSwitch.setOnCheckedChangeListener(
                (btn, isChecked) -> updateSwitchColor(notificationsSwitch, isChecked)
        );

        /**
         * Deletes entrant profile only after confirmation.
         */
        deleteProfileButton.setOnClickListener(v -> {
            if (!role.equals("entrant")) return;

            fvm.getProfileById(profileID, profile -> {
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            fvm.deleteProfileAndCleanOpenEvents(
                                    profile,
                                    () -> {
                                        entrantVM.setEntrant(null);
                                        Toast.makeText(getContext(), "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                                        NavHostFragment.findNavController(this)
                                                .navigate(R.id.action_settingsFragment_to_startUpFragment);
                                    },
                                    err -> Toast.makeText(
                                            getContext(),
                                            "Failed to delete: " + err.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }, err -> {});
        });

        /**
         * Saves updated fields including name, email, phone, notifications,
         * and profile image if one was selected.
         */
        saveButton.setOnClickListener(v -> saveProfileChanges());

        /**
         * Navigates back to previous screen.
         */
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        return view;
    }

    /**
     * Loads a profile image from Firestore using a document ID
     * and applies Base64 decoding.
     *
     * @param imageId Firestore image document ID
     */
    private void loadProfileImage(String imageId) {
        if (imageId == null || imageId.isEmpty()) {
            profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
            return;
        }

        fvm.getDb().collection("images")
                .document(imageId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String base64 = doc.getString("url");
                        if (base64 != null && !base64.isEmpty()) {
                            try {
                                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                profileImage.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        profileImage.setImageResource(android.R.drawable.ic_menu_report_image)
                );
    }

    /**
     * Applies color tinting to the notification toggle switch.
     *
     * @param sw switch component
     * @param isOn current toggle state
     */
    private void updateSwitchColor(Switch sw, boolean isOn) {
        int color = getResources().getColor(
                isOn ? android.R.color.holo_green_light : android.R.color.darker_gray,
                requireContext().getTheme()
        );
        sw.getThumbDrawable().setTint(color);
        sw.getTrackDrawable().setTint(color);
    }

    /**
     * Validates user input and performs checks before saving changes.
     * If needed, uploads the selected profile picture as Base64.
     */
    private void saveProfileChanges() {
        String newName = nameEdit.getText().toString().trim();
        String newEmail = emailEdit.getText().toString().trim();
        String newPhone = phoneEdit.getText().toString().trim();
        boolean newNotifications = notificationsSwitch.isChecked();

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

        if (!newPhone.isEmpty() &&
                (!Patterns.PHONE.matcher(newPhone).matches() || newPhone.length() < 10)) {
            phoneEdit.setError("Valid phone number required");
            phoneEdit.requestFocus();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        if (!name.equals(newName)) updates.put("personName", newName);
        if (!email.equals(newEmail)) updates.put("email", newEmail);
        if (!phone.equals(newPhone)) updates.put("phone", newPhone);
        if (newNotifications != initialNotifications) updates.put("notificationsEnabled", newNotifications);

        fvm.checkEmailExists(newEmail, exists -> {
            if (exists && !newEmail.equals(email)) {
                emailEdit.setError("Email already in use");
                emailEdit.requestFocus();
                return;
            }

            if (selectedPfpUri != null) {
                uploadProfileImageAndSave(updates);
            } else {
                if (updates.isEmpty()) {
                    Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show();
                    return;
                }
                commitSave(updates);
            }

        }, err -> Toast.makeText(
                getContext(),
                "Error checking email: " + err.getMessage(),
                Toast.LENGTH_SHORT
        ).show());
    }

    /**
     * Converts the chosen profile picture to Base64,
     * uploads it to Firestore, and attaches the new image ID to updates.
     *
     * @param updates pending profile updates
     */
    private void uploadProfileImageAndSave(Map<String, Object> updates) {
        try {
            InputStream inputStream = requireContext()
                    .getContentResolver()
                    .openInputStream(selectedPfpUri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                Toast.makeText(requireContext(), "Image load failed", Toast.LENGTH_SHORT).show();
                return;
            }

            if (bitmap.getByteCount() > 2_000_000) {
                bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true);
                Toast.makeText(requireContext(), "Image compressed for upload.", Toast.LENGTH_SHORT).show();
            } else if (bitmap.getByteCount() > 800_000) {
                bitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            String base64String = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            Map<String, Object> imageData = new HashMap<>();
            imageData.put("eventId", null);
            imageData.put("uploaderId", profileID);
            imageData.put("url", base64String);
            imageData.put("uploadedAt", new Date());
            imageData.put("approved", true);

            fvm.getDb().collection("images")
                    .add(imageData)
                    .addOnSuccessListener(docRef -> {
                        updates.put("profileImageUrl", docRef.getId());
                        commitSave(updates);
                    })
                    .addOnFailureListener(ex ->
                            Toast.makeText(requireContext(),
                                    "Failed to upload image",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );

        } catch (Exception ex) {
            Toast.makeText(requireContext(),
                    "Image convert failed",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Writes all profile changes to Firestore and updates all ViewModels
     * so the new profile data is reflected across the app.
     *
     * @param updates key-value map of updated fields
     */
    private void commitSave(Map<String, Object> updates) {

        fvm.updateProfile(
                profileID,
                updates,
                () -> Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(requireContext(), "Update failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

<<<<<<< HEAD
        // Update local variables so UI knows the new values
        for (String key : updates.keySet()) {

            if (key.equals("personName")) {
                name = (String) updates.get(key);
            }

            if (key.equals("email")) {
                email = (String) updates.get(key);
            }

            if (key.equals("phone")) {
                phone = (String) updates.get(key);
            }

            if (key.equals("notificationsEnabled")) {
                initialNotifications = (Boolean) updates.get(key);
            }

            if (key.equals("profileImageUrl")) {
                imageUrl = (String) updates.get(key);
            }
        }
    }


=======
        if (role.equals("entrant")) entrantVM.updateSettings(updates);
        if (role.equals("organizer")) organizerVM.updateSettings(updates);
        if (role.equals("admin")) adminVM.updateSettings(updates);

        for (String key : updates.keySet()) {
            if (key.equals("personName")) name = (String) updates.get(key);
            if (key.equals("email")) email = (String) updates.get(key);
            if (key.equals("phone")) phone = (String) updates.get(key);
            if (key.equals("notificationsEnabled")) initialNotifications = (Boolean) updates.get(key);
            if (key.equals("profileImageUrl")) imageUrl = (String) updates.get(key);
        }
    }
>>>>>>> 9a7d4e38f516309921ef145183825ae29f915917
}
