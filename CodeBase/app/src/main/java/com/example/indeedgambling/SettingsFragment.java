package com.example.indeedgambling;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

public class SettingsFragment extends Fragment {
    private FirebaseViewModel fvm;
    public String profileID,role;

    private EntrantViewModel entrantVM;
    private OrganizerViewModel organizerVM;
    private AdminViewModel adminVM;

    private Uri selectedPfpUri = null;

    private ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPfpUri = uri;
                    uploadProfileImage(uri);
                }
            });

    public SettingsFragment() {}

    private void uploadProfileImage(Uri uri) {
        fvm.uploadProfilePicture(profileID, uri,
                downloadUrl -> {
                    ImageView profileImage = requireView().findViewById(R.id.pfp_image);

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        Button backButton = view.findViewById(R.id.back_button);

        TextView roleText = view.findViewById(R.id.role_text_profile);

        ImageView profileImage = view.findViewById(R.id.pfp_image);
        Button editPfpButton = view.findViewById(R.id.pfp_edit_button);

        EditText nameEdit = view.findViewById(R.id.settings_name);
        EditText emailEdit = view.findViewById(R.id.settings_email);
        EditText phoneEdit = view.findViewById(R.id.settings_phone);

        Switch lightModeSwitch = view.findViewById(R.id.settings_lightmode_switch);
        Switch notificationsSwitch = view.findViewById(R.id.settings_notif_switch);

        Button deleteProfileButton = view.findViewById(R.id.delete_profile_button);
        Button saveButton = view.findViewById(R.id.save_settings_button);

        // load profile data
        fvm.getProfileById(profileID, profile -> {
            role = profile.getRole();
            String name = profile.getPersonName();
            String email = profile.getEmail();
            String phone = profile.getPhone();

            roleText.setText(role);

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

        return view;
    }

    private void updateCurrentModel(View view, )
}
