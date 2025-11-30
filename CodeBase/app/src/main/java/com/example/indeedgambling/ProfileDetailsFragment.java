package com.example.indeedgambling;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileDetailsFragment extends Fragment {

    private FirebaseViewModel fvm;
    private String profileID;

    private ImageView profileImage;
    private TextView nameText, emailText, phoneText, roleText, idText;
    private RelatedEventAdapter relatedAdapter;

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
        idText = view.findViewById(R.id.profile_detail_id);
        LinearLayout relatedContainer = view.findViewById(R.id.related_events_container);


        Button backBtn = view.findViewById(R.id.profile_detail_back);

        // Back button
        backBtn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // ----------------------------------------------------------------------
        // Load the profile
        // ----------------------------------------------------------------------
        fvm.getProfileById(profileID, profile -> {

            idText.setText(profile.getProfileId());
            nameText.setText(profile.getPersonName());
            emailText.setText(profile.getEmail());

            String phone = profile.getPhone();
            phoneText.setText(phone != null && !phone.isEmpty() ? phone : "N/A");

            roleText.setText(profile.getRole());

            // Load image if exists
            String imageId = profile.getProfileImageUrl();

            if (imageId == null || imageId.isEmpty()) {
                profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
            } else {
                fvm.getDb()
                        .collection("images")
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
                                } else {
                                    profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
                                }

                            } else {
                                profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
                            }
                        })
                        .addOnFailureListener(e -> {
                            profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
                        });
            }

            // ------------------------------------------------------------------
            // SETUP RELATED EVENTS SECTION — must be inside profile callback
            // ------------------------------------------------------------------
            RecyclerView rv = view.findViewById(R.id.profile_related_events_list);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));

            relatedAdapter = new RelatedEventAdapter();
            rv.setAdapter(relatedAdapter);

            boolean isOrganizer = profile.getRole() != null &&
                    profile.getRole().equalsIgnoreCase("organizer");

            relatedAdapter.setUser(profileID, isOrganizer);

            fvm.getEventsLive().observe(getViewLifecycleOwner(), events -> {
                if (events == null) {
                    relatedContainer.setVisibility(View.GONE);
                    return;
                }

                // Role check — hide for admins
                if (profile.getRole() != null && profile.getRole().equalsIgnoreCase("admin")) {
                    relatedContainer.setVisibility(View.GONE);
                    return;
                }

                // Submit to adapter (it will filter out "None")
                relatedAdapter.setEvents(events);

                // After filtering, check if it's empty
                if (relatedAdapter.getItemCount() == 0) {
                    relatedContainer.setVisibility(View.GONE);
                } else {
                    relatedContainer.setVisibility(View.VISIBLE);
                }
            });


            relatedAdapter.setOnItemClickListener((event, relation) -> {
                Bundle args = new Bundle();
                args.putSerializable("event", event);

                NavHostFragment.findNavController(this)
                        .navigate(R.id.adminEventDetailsFragment, args);
            });

        }, error -> {
            Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
