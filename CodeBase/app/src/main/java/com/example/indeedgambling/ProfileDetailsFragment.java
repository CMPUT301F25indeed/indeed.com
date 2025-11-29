package com.example.indeedgambling;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

public class ProfileDetailsFragment extends Fragment {

    private FirebaseViewModel fvm;
    private String profileID;

    private ImageView profileImage;
    private TextView nameText, emailText, phoneText, roleText;

    public ProfileDetailsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            profileID = getArguments().getString("profileID");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_details_fragment, container, false);

        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        profileImage = view.findViewById(R.id.profile_detail_image);
        nameText = view.findViewById(R.id.profile_detail_name);
        emailText = view.findViewById(R.id.profile_detail_email);
        phoneText = view.findViewById(R.id.profile_detail_phone);
        roleText = view.findViewById(R.id.profile_detail_role);

        Button backBtn = view.findViewById(R.id.profile_detail_back);

        // Load profile
        fvm.getProfileById(profileID, profile -> {

            nameText.setText(profile.getPersonName());
            emailText.setText(profile.getEmail());
            phoneText.setText(profile.getPhone());
            roleText.setText(profile.getRole());

            if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isEmpty()) {
                Glide.with(requireContext()).load(profile.getProfileImageUrl()).into(profileImage);
            }

        }, error -> {
            Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
        });

        backBtn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());



        return view;
    }
}
