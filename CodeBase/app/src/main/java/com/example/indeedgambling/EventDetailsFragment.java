package com.example.indeedgambling;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventDetailsFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private EntrantViewModel entrantVM;

    private Event event;
    private String entrantId;
    private String entrantRelation;

    private ImageView posterView;
    private TextView name, desc, waitlistStatus, total;
    private Button yesBtn, noBtn, backBtn, tryAgainBtn;

    private FusedLocationProviderClient fusedLocationClient;
    private String scannedEventId = null;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_event_details, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM  = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        entrantId = entrantVM.returnID();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        updateUserLocation();

        if (getArguments() != null) {
            scannedEventId = getArguments().getString("eventId");
        }

        name           = v.findViewById(R.id.event_name);
        desc           = v.findViewById(R.id.event_description);
        waitlistStatus = v.findViewById(R.id.waitlist_status);
        backBtn        = v.findViewById(R.id.back_button);
        total          = v.findViewById(R.id.event_total_entrant);
        yesBtn         = v.findViewById(R.id.yes_button);
        noBtn          = v.findViewById(R.id.no_button);
        tryAgainBtn    = v.findViewById(R.id.try_again_button);
        posterView     = v.findViewById(R.id.event_poster);

        backBtn.setOnClickListener(v1 -> requireActivity().onBackPressed());

        if (scannedEventId != null && !scannedEventId.isEmpty()) {
            firebaseVM.getEventById(
                    scannedEventId,
                    loadedEvent -> {
                        this.event = loadedEvent;
                        loadEventDetails(v);
                        disableAllEntrantActions();
                    },
                    error -> Toast.makeText(getContext(), "Failed to load event", Toast.LENGTH_SHORT).show()
            );
            return v;
        }

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            loadEventDetails(v);
            applyEntrantButtonLogic(v);
        }

        return v;
    }

    private void applyEntrantButtonLogic(View v) {

        entrantRelation = event.whichList(entrantId);

        if (entrantRelation.equals("none") || entrantRelation.equals("waitlist")) {
            yesBtn.setText("Join\n Waitlist");
            noBtn.setText("Leave\n Waitlist");
            yesBtn.setVisibility(View.VISIBLE);
            noBtn.setVisibility(View.VISIBLE);
            tryAgainBtn.setVisibility(View.GONE);

            yesBtn.setOnClickListener(v1 -> {
                clickedJoinWaitlist(v);
                updateWaitlistStatus();
            });

            noBtn.setOnClickListener(v1 -> {
                clickedLeaveWaitlist(v);
                updateWaitlistStatus();
            });
        }

        if (entrantRelation.equals("invited")) {
            yesBtn.setText("Accept\n Invite");
            noBtn.setText("Reject\n Invite");
            yesBtn.setVisibility(View.VISIBLE);
            noBtn.setVisibility(View.VISIBLE);
            tryAgainBtn.setVisibility(View.GONE);

            yesBtn.setOnClickListener(v1 -> clickedAcceptInvite(v));
            noBtn.setOnClickListener(v1 -> clickedRejectInvite(v));
        }

        if (entrantRelation.equals("accepted")) {
            yesBtn.setVisibility(View.GONE);
            noBtn.setVisibility(View.GONE);
            tryAgainBtn.setVisibility(View.VISIBLE);
            tryAgainBtn.setText("Already Accepted");
            tryAgainBtn.setEnabled(false);
        }

        if (entrantRelation.equals("cancelled")) {
            tryAgainBtn.setText("Try Again?");
            yesBtn.setVisibility(View.GONE);
            noBtn.setVisibility(View.GONE);
            tryAgainBtn.setVisibility(View.VISIBLE);
            tryAgainBtn.setOnClickListener(v1 -> clickedTryAgain(v));
        }
    }

    private void disableAllEntrantActions() {
        yesBtn.setVisibility(View.GONE);
        noBtn.setVisibility(View.GONE);
        tryAgainBtn.setVisibility(View.GONE);
        waitlistStatus.setVisibility(View.GONE);

        yesBtn.setEnabled(false);
        noBtn.setEnabled(false);
        tryAgainBtn.setEnabled(false);
    }

    private void loadEventDetails(View v) {

        name.setText(event.getEventName());
        desc.setText(event.getDescription());

        // ---- Load Poster ----
        if (posterView != null) {
            posterView.setImageBitmap(null);
            posterView.setBackgroundColor(0xFFEEEEEE);
        }

        String imageId = event.getImageUrl();
        if (!TextUtils.isEmpty(imageId) && posterView != null) {
            firebaseVM.getDb().collection("images")
                    .document(imageId)
                    .get()
                    .addOnSuccessListener((DocumentSnapshot doc) -> {
                        if (doc.exists()) {
                            String base64 = doc.getString("url");
                            try {
                                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                posterView.setBackgroundColor(0x00000000);
                                posterView.setImageBitmap(bmp);
                            } catch (Exception e) {}
                        }
                    });
        }

        TextView loc       = v.findViewById(R.id.event_location);
        TextView dates     = v.findViewById(R.id.event_dates);
        TextView regPeriod = v.findViewById(R.id.event_registration);
        TextView status    = v.findViewById(R.id.event_status);
        TextView category  = v.findViewById(R.id.event_category);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

        if (event.getLocation() != null)
            loc.setText("Location: " + event.getLocationString());

        if (event.getCategory() != null)
            category.setText("Category: " + event.getCategory());

        if (event.getEventStart() != null && event.getEventEnd() != null)
            dates.setText("Event Dates: " +
                    sdf.format(event.getEventStart()) + " – " +
                    sdf.format(event.getEventEnd()));

        if (event.getRegistrationStart() != null && event.getRegistrationEnd() != null)
            regPeriod.setText("Registration: " +
                    sdf.format(event.getRegistrationStart()) + " – " +
                    sdf.format(event.getRegistrationEnd()));

        status.setText("Status: " + event.getStatus());

        updateTotal(v);
    }

    private void clickedJoinWaitlist(View v) {
        if (event.getWaitingList() != null &&
                event.getWaitingList().contains(entrantId)) {
            Toast.makeText(getContext(), "You already joined this waitlist!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!event.tryaddtoWaitingList(entrantId)) {
            Toast.makeText(getContext(), "Waitlist is FULL!", Toast.LENGTH_SHORT).show();
            return;
        }

        entrantVM.addEventToEntrant(event.getEventId());
        firebaseVM.joinWaitingList(event.getEventId(), entrantId, () -> {}, e ->
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );

        firebaseVM.upsertEntrant(entrantVM.getCurrentEntrant(), () -> {}, err ->
                Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
        );

        Toast.makeText(getContext(), "Joined waitlist!", Toast.LENGTH_SHORT).show();
        updateTotal(v);
    }

    private void clickedLeaveWaitlist(View v) {
        if (event.getWaitingList() == null ||
                !event.getWaitingList().contains(entrantId)) {
            Toast.makeText(getContext(), "You're not on the waitlist!", Toast.LENGTH_SHORT).show();
            return;
        }

        event.getWaitingList().remove(entrantId);
        entrantVM.removeEventFromEntrant(event.getEventId());

        firebaseVM.leaveWaitingList(event.getEventId(), entrantId, () -> {}, e ->
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );

        firebaseVM.upsertEntrant(entrantVM.getCurrentEntrant(), () -> {}, err ->
                Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
        );

        Toast.makeText(getContext(), "Removed from waitlist.", Toast.LENGTH_SHORT).show();
        updateTotal(v);
    }

    private void clickedAcceptInvite(View v) {
        firebaseVM.signUpForEvent(event.getEventId(), entrantId,
                () -> Toast.makeText(getContext(), "Signed up!", Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clickedRejectInvite(View v) {
        Toast.makeText(getContext(), "Rejected invite.", Toast.LENGTH_SHORT).show();
    }

    private void clickedTryAgain(View v) {
        Toast.makeText(getContext(), "Trying again...", Toast.LENGTH_SHORT).show();
    }

    private void updateTotal(View v) {
        if (event.getWaitingList() != null) {
            total.setText("Total: " + event.getWaitingList().size());
        }
    }

    private void updateWaitlistStatus() {
        if (event.getWaitingList() != null &&
                event.getWaitingList().contains(entrantId)) {

            waitlistStatus.setVisibility(View.VISIBLE);
            waitlistStatus.setText("✅ You are on the waiting list!");
            yesBtn.setEnabled(false);
            yesBtn.setText("Joined\nWaitlist");

        } else {
            waitlistStatus.setVisibility(View.GONE);
            yesBtn.setEnabled(true);
            yesBtn.setText("Join\n Waitlist");
        }
    }

    private void updateUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{
                            "android.permission.ACCESS_COARSE_LOCATION",
                            "android.permission.ACCESS_FINE_LOCATION"
                    },
                    101
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    Entrant entrant = entrantVM.getCurrentEntrant();
                    if (entrant == null) return;

                    entrant.setLocation(location);
                    firebaseVM.upsertEntrant(
                            entrant,
                            () -> {},
                            e -> Log.d("Firebase Location Issue", "EventDetails" + e)
                    );
                })
                .addOnFailureListener(e ->
                        Log.d("Firebase Location Issue", "EventDetails" + e)
                );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateUserLocation();
        }
    }
}
