/**
 * Displays detailed profile information for any user (Entrant, Organizer, Admin).
 *
 * This screen shows:
 * - Profile image, name, email, phone, role, and profile ID
 * - A list of related events:
 *      • For organizers → events they created
 *      • For entrants  → events they interacted with (waitlist/invited/accepted)
 *      • For admins    → this section is hidden
 *
 * Features:
 * - Loads profile data from Firestore using FirebaseViewModel
 * - Decodes and displays Base64 profile images
 * - Dynamically toggles visibility of "related events" section
 * - Navigates to event details when a related event is selected
 */
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

    /**
     * Retrieves profileID passed through arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            profileID = getArguments().getString("profileID");
        }
    }

    /**
     * Inflates the layout, initializes UI components, loads profile data,
     * and configures the related-events section.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.profile_details_fragment, container, false);

        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        profileImage = view.findViewById(R.id.profile_detail_image);
        nameText     = view.findViewById(R.id.profile_detail_name);
        emailText    = view.findViewById(R.id.profile_detail_email);
        phoneText    = view.findViewById(R.id.profile_detail_phone);
        roleText     = view.findViewById(R.id.profile_detail_role);
        idText       = view.findViewById(R.id.profile_detail_id);
        LinearLayout relatedContainer = view.findViewById(R.id.related_events_container);

        Button backBtn = view.findViewById(R.id.profile_detail_back);

        /**
         * Navigates to the previous screen.
         */
        backBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        /**
         * Loads the profile and populates UI fields.
         */
        fvm.getProfileById(profileID, profile -> {

                    idText.setText(profile.getProfileId());
                    nameText.setText(profile.getPersonName());
                    emailText.setText(profile.getEmail());

                    String phone = profile.getPhone();
                    phoneText.setText(phone != null && !phone.isEmpty() ? phone : "N/A");

                    roleText.setText(profile.getRole());

                    loadProfileImage(profile.getProfileImageUrl());

                    setupRelatedEventsSection(view, relatedContainer, profile);

                }, error ->
                        Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    /**
     * Loads and decodes the user's profile image from Firestore.
     */
    private void loadProfileImage(String imageId) {
        if (imageId == null || imageId.isEmpty()) {
            profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
            return;
        }

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
                .addOnFailureListener(e ->
                        profileImage.setImageResource(android.R.drawable.ic_menu_report_image)
                );
    }

    /**
     * Builds the related-events list and configures visibility rules
     * depending on the user's role.
     */
    private void setupRelatedEventsSection(
            View view,
            LinearLayout container,
            Profile profile
    ) {
        RecyclerView rv = view.findViewById(R.id.profile_related_events_list);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        relatedAdapter = new RelatedEventAdapter();
        rv.setAdapter(relatedAdapter);

        boolean isOrganizer = profile.getRole() != null &&
                profile.getRole().equalsIgnoreCase("organizer");

        relatedAdapter.setUser(profileID, isOrganizer);

        if (profile.getRole() != null &&
                profile.getRole().equalsIgnoreCase("admin")) {
            container.setVisibility(View.GONE);
            return;
        }

        fvm.getEventsLive().observe(getViewLifecycleOwner(), events -> {

            if (events == null) {
                container.setVisibility(View.GONE);
                return;
            }

            relatedAdapter.setEvents(events);

            if (relatedAdapter.getItemCount() == 0) {
                container.setVisibility(View.GONE);
            } else {
                container.setVisibility(View.VISIBLE);
            }
        });

        relatedAdapter.setOnItemClickListener((event, relation) -> {
            Bundle args = new Bundle();
            args.putSerializable("event", event);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminEventDetailsFragment, args);
        });
    }
}
