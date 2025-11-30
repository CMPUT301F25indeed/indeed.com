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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

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

    private boolean initialNotifications, initialLightMode;
    private Switch lightModeSwitch, notificationsSwitch;
    private ImageView profileImage;

    private ActivityResultLauncher<String> imagePickerLauncher;

    public SettingsFragment() {}

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
                        selectedPfpUri = uri; // store for later
                        if (profileImage != null) {
                            // show local preview right away
                            profileImage.setImageURI(uri);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        adminVM = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        Button backButton = view.findViewById(R.id.back_button);

        profileImage = view.findViewById(R.id.pfp_image);
        Button editPfpButton = view.findViewById(R.id.pfp_edit_button);

        nameEdit = view.findViewById(R.id.settings_name);
        emailEdit = view.findViewById(R.id.settings_email);
        phoneEdit = view.findViewById(R.id.settings_phone);

        lightModeSwitch = view.findViewById(R.id.settings_lightmode_switch);
        notificationsSwitch = view.findViewById(R.id.settings_notif_switch);

        Button deleteProfileButton = view.findViewById(R.id.delete_profile_button);
        Button saveButton = view.findViewById(R.id.save_settings_button);

        // load profile data
        fvm.getProfileById(profileID, profile -> {
            role = profile.getRole().trim().toLowerCase();
            name = profile.getPersonName();
            email = profile.getEmail();
            phone = profile.getPhone();

            nameEdit.setText(name);
            emailEdit.setText(email);
            phoneEdit.setText(phone);

            // hide delete + notifications for non-entrant
            if (!role.equals("entrant")) {
                deleteProfileButton.setVisibility(View.GONE);
                LinearLayout myLayout = view.findViewById(R.id.noti_layout);
                myLayout.setVisibility(View.GONE);
            }

            initialNotifications = Boolean.TRUE.equals(profile.isNotificationsEnabled());
            initialLightMode = Boolean.TRUE.equals(profile.isLightModeEnabled());

            notificationsSwitch.setChecked(initialNotifications);
            lightModeSwitch.setChecked(initialLightMode);

            updateSwitchColor(notificationsSwitch, initialNotifications);
            updateSwitchColor(lightModeSwitch, initialLightMode);

            // ----- NEW: load profile image using Base64 from /images -----
            imageUrl = profile.getProfileImageUrl(); // now stores image doc id
            if (imageUrl != null && !imageUrl.isEmpty()) {
                fvm.getDb().collection("images")
                        .document(imageUrl)
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
                                } else {
                                    profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
                                }
                            } else {
                                profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
                            }
                        })
                        .addOnFailureListener(e ->
                                profileImage.setImageResource(android.R.drawable.ic_menu_report_image)
                        );
            }
            // ----- END NEW LOAD LOGIC -----

        }, error -> {
            error.printStackTrace();
        });

        // Edit pfp
        editPfpButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        notificationsSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            updateSwitchColor(notificationsSwitch, isChecked);
        });

        lightModeSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            updateSwitchColor(lightModeSwitch, isChecked);
        });

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
                                    err -> Toast.makeText(getContext(),
                                            "Failed to delete: " + err.getMessage(),
                                            Toast.LENGTH_LONG).show()
                            );
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }, err -> {});
        });

        saveButton.setOnClickListener(v -> saveProfileChanges());

        backButton.setOnClickListener(v -> {
            NavController nav = NavHostFragment.findNavController(this);

            if (role.equals("entrant")) {
                nav.navigate(R.id.action_settingsFragment_to_entrantHomeFragment);
            }

            if (role.equals("organizer")) {
                nav.navigate(R.id.action_settingsFragment_to_organizerHomeFragment);
            }

            if (role.equals("admin")) {
                nav.navigate(R.id.action_settingsFragment_to_adminHomeFragment);
            }
        });

        return view;
    }

    private void updateSwitchColor(Switch sw, boolean isOn) {
        int color = getResources().getColor(
                isOn ? android.R.color.holo_green_light : android.R.color.darker_gray,
                requireContext().getTheme()
        );
        sw.getThumbDrawable().setTint(color);
        sw.getTrackDrawable().setTint(color);
    }

    private void saveProfileChanges() {
        String newName = nameEdit.getText().toString().trim();
        String newEmail = emailEdit.getText().toString().trim();
        String newPhone = phoneEdit.getText().toString().trim();

        boolean newNotifications = notificationsSwitch.isChecked();
        boolean newLightMode = lightModeSwitch.isChecked();

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

        Map<String, Object> updates = new HashMap<>();

        if (!name.equals(newName)) updates.put("personName", newName);
        if (!email.equals(newEmail)) updates.put("email", newEmail);
        if (!phone.equals(newPhone)) updates.put("phone", newPhone);
        if (newNotifications != initialNotifications) updates.put("notificationsEnabled", newNotifications);
        if (newLightMode != initialLightMode) updates.put("lightModeEnabled", newLightMode);

        // Check email first
        fvm.checkEmailExists(newEmail, exists -> {
            if (exists && !newEmail.equals(email)) {
                emailEdit.setError("Email already in use");
                emailEdit.requestFocus();
                return;
            }

            // ----- NEW: upload profile image as Base64 to /images -----
            if (selectedPfpUri != null) {
                try {
                    InputStream inputStream = requireContext()
                            .getContentResolver()
                            .openInputStream(selectedPfpUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    if (bitmap != null) {
                        // same style compression as event posters
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
                        imageData.put("eventId", null);        // profile picture, no event
                        imageData.put("uploaderId", profileID);
                        imageData.put("url", base64String);
                        imageData.put("uploadedAt", new Date());
                        imageData.put("approved", true);

                        fvm.getDb().collection("images")
                                .add(imageData)
                                .addOnSuccessListener(docRef -> {
                                    String imageDocId = docRef.getId();
                                    updates.put("profileImageUrl", imageDocId);
                                    commitSave(updates);
                                })
                                .addOnFailureListener(ex -> {
                                    ex.printStackTrace();
                                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Toast.makeText(requireContext(), "Image load failed", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(requireContext(), "Image convert failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                // No new image, just commit other updates
                if (updates.isEmpty()) {
                    Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show();
                    return;
                }
                commitSave(updates);
            }
            // ----- END NEW UPLOAD LOGIC -----

        }, err -> Toast.makeText(getContext(), "Error checking email: " + err.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void commitSave(Map<String, Object> updates) {

        fvm.updateProfile(profileID, updates,
                () -> Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(requireContext(), "Update failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        if (role.equals("entrant")) {
            entrantVM.updateSettings(updates);
        }

        if (role.equals("organizer")) {
            organizerVM.updateSettings(updates);
        }

        if (role.equals("admin")) {
            adminVM.updateSettings(updates);
        }

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

            if (key.equals("lightModeEnabled")) {
                initialLightMode = (Boolean) updates.get(key);
                //saveAndApplyTheme(initialLightMode);
            }

            if (key.equals("profileImageUrl")) {
                imageUrl = (String) updates.get(key);
            }
        }
    }

    private void saveAndApplyTheme(boolean isLightMode) {
        int themeMode = isLightMode ?
                AppCompatDelegate.MODE_NIGHT_NO :
                AppCompatDelegate.MODE_NIGHT_YES;

        SharedPreferences prefs = requireActivity().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("theme_mode", themeMode).apply();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) {
                    Log.d("ThemeDebug", "Activity not available, theme preference saved for next launch");
                    return;
                }

                AppCompatDelegate.setDefaultNightMode(themeMode);
                Toast.makeText(requireContext(), "Theme updated successfully!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e("ThemeDebug", "Theme application failed: " + e.getMessage());
                Toast.makeText(requireContext(), "Theme saved - changes apply on restart", Toast.LENGTH_SHORT).show();
            }
        }, 1000);
    }

    // (old helper kept, but no longer used directly for the new flow)
    private void useProfileImage(Uri uri) {
        fvm.uploadProfilePicture(profileID, uri,
                downloadUrl -> {

                    profileImage = requireView().findViewById(R.id.pfp_image);

                    Glide.with(requireContext())
                            .load(downloadUrl)
                            //.placeholder(R.drawable.default_pfp)
                            .into(profileImage);

                    fvm.updateProfilePicture(profileID, downloadUrl,
                            () -> {}, err -> err.printStackTrace());
                },
                exception -> {
                    exception.printStackTrace();
                });
    }
}
