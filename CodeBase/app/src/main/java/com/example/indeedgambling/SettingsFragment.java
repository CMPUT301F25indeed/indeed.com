package com.example.indeedgambling;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.HashMap;
import java.util.Map;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;
import android.util.Patterns;

public class SettingsFragment extends Fragment {
    private FirebaseViewModel fvm;
    public String profileID,role,name,email,phone,imageUrl;

    private EntrantViewModel entrantVM;
    private OrganizerViewModel organizerVM;
    private AdminViewModel adminVM;

    private Uri selectedPfpUri = null;

    private EditText nameEdit,emailEdit, phoneEdit;

    private boolean initialNotifications, initialLightMode;
    private Switch lightModeSwitch,notificationsSwitch;
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
                            profileImage.setImageURI(uri); // show preview
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

        TextView roleText = view.findViewById(R.id.role_text_profile);

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

            roleText.setText(role);

            // Load the profile image
            imageUrl = profile.getProfileImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext())
                        .load(imageUrl)
                        //.placeholder(R.drawable.default_pfp) // optional placeholder
                        .into(profileImage);
            }

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

            String imageUrl = profile.getProfileImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext())
                        .load(imageUrl)
                        //.placeholder(R.drawable.default_pfp)
                        .into(profileImage);
            }

        }, error -> {
            error.printStackTrace();
        });


        // Edit pfp
        editPfpButton.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });



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

        saveButton.setOnClickListener(v -> {
            saveProfileChanges();
        });

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

        if (!name.equals(nameEdit.getText().toString())) updates.put("personName", newName);
        if (!email.equals(emailEdit.getText().toString())) updates.put("email", newEmail);
        if (!phone.equals(phoneEdit.getText().toString())) updates.put("phone", newPhone);
        if (newNotifications != initialNotifications) updates.put("notificationsEnabled", newNotifications);
        if (newLightMode != initialLightMode) updates.put("lightMode", newLightMode);


        // Check email first
        fvm.checkEmailExists(newEmail, exists -> {
            if (exists && !newEmail.equals(email)) {
                emailEdit.setError("Email already in use");
                emailEdit.requestFocus();
                return;
            }


            // If user picked a new profile picture, upload it first
            if (selectedPfpUri != null) {
                fvm.uploadProfilePicture(profileID, selectedPfpUri, downloadUrl -> {
                    updates.put("profileImageUrl", downloadUrl);
                    commitSave(updates); // commit after successful upload
                }, exception -> {
                    exception.printStackTrace();
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
            } else {
                // No new image, just commit other updates
                if (updates.isEmpty()) {
                    Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show();
                    return;
                }
                commitSave(updates);
            }

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
    }


    private void useProfileImage(Uri uri) {
        fvm.uploadProfilePicture(profileID, uri,
                downloadUrl -> {

                    profileImage = requireView().findViewById(R.id.pfp_image);

                    Glide.with(requireContext())
                            .load(downloadUrl)
                            //.placeholder(R.drawable.default_pfp)
                            .into(profileImage);

                    // also update Firestore profile
                    fvm.updateProfilePicture(profileID, downloadUrl,
                            () -> {}, err -> err.printStackTrace());
                },
                exception -> {
                    exception.printStackTrace();
                });
    }
}
