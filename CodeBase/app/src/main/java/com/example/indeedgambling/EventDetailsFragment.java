package com.example.indeedgambling;

import android.app.Activity;
import android.Manifest;
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

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.google.firebase.firestore.DocumentSnapshot;

public class EventDetailsFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private EntrantViewModel entrantVM;

    private Event event;
    private String entrantId;
    private String entrantRelation;

    private TextView name, desc, waitlistStatus, total;
    private Button yesBtn, noBtn, backBtn, tryAgainBtn;
    private ImageView posterView;          // <--- NEW

    // set when QR is used
    private String scannedEventId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_event_details, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM  = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        entrantId = entrantVM.returnID();

        // QR argument
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
        posterView     = v.findViewById(R.id.event_poster);   // <--- NEW

        backBtn.setOnClickListener(v1 -> requireActivity().onBackPressed());

        // CASE 1 — from QR scan
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

        // CASE 2 — normal navigation
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            loadEventDetails(v);
            applyEntrantButtonLogic(v);
        }

        return v;
    }

    // =====================================================================
    private void applyEntrantButtonLogic(View v) {

        if (event == null) return;

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

    // QR MODE — disables everything
    private void disableAllEntrantActions() {
        yesBtn.setVisibility(View.GONE);
        noBtn.setVisibility(View.GONE);
        tryAgainBtn.setVisibility(View.GONE);
        waitlistStatus.setVisibility(View.GONE);

        yesBtn.setEnabled(false);
        noBtn.setEnabled(false);
        tryAgainBtn.setEnabled(false);
    }

    // =====================================================================
    private void loadEventDetails(View v) {
        if (event == null) return;

        name.setText(event.getEventName());
        desc.setText(event.getDescription());

        // ---------- NEW: load poster ----------
        if (posterView != null) {
            // grey placeholder first
            posterView.setImageBitmap(null);
            posterView.setBackgroundColor(0xFFEEEEEE);
        }

        String imageId = event.getImageUrl();
        if (!TextUtils.isEmpty(imageId) && posterView != null) {
            firebaseVM.getDb().collection("images")
                    .document(imageId)
                    .get()
                    .addOnSuccessListener((DocumentSnapshot doc) -> {
                        if (doc != null && doc.exists()) {
                            String base64 = doc.getString("url");
                            if (!TextUtils.isEmpty(base64)) {
                                try {
                                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                    posterView.setBackgroundColor(0x00000000);
                                    posterView.setImageBitmap(bmp);
                                } catch (Exception e) {
                                    Log.e("EventDetails", "Poster decode failed", e);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("EventDetails", "Poster load failed", e)
                    );
        }
        // ---------- END poster logic ----------

        TextView loc       = v.findViewById(R.id.event_location);
        TextView dates     = v.findViewById(R.id.event_dates);
        TextView regPeriod = v.findViewById(R.id.event_registration);
        TextView status    = v.findViewById(R.id.event_status);
        TextView category  = v.findViewById(R.id.event_category);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

        if (event.getLocation() != null)
            loc.setText("Location: " + event.getLocation());

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

    // =====================================================================
    private void clickedJoinWaitlist(View v) {

        if (entrantId == null) {
            Toast.makeText(getContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.getWaitingList() != null &&
                event.getWaitingList().contains(entrantId)) {
            Toast.makeText(getContext(), "You already joined this waitlist!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.tryaddtoWaitingList(entrantId)) {
            Toast.makeText(getContext(), "Waitlist is FULL!", Toast.LENGTH_SHORT).show();
            return;
        }

        entrantVM.addEventToEntrant(event.getEventId());
        event.getWaitingList().add(entrantId);

        firebaseVM.joinWaitingList(event.getEventId(), entrantId, () -> {},
                e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        firebaseVM.upsertEntrant(entrantVM.getCurrentEntrant(), () -> {},
                err -> Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show());

        Toast.makeText(getContext(), "Joined waitlist!", Toast.LENGTH_SHORT).show();
        updateTotal(v);
    }

    private void clickedLeaveWaitlist(View v) {

        if (entrantId == null) {
            Toast.makeText(getContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.getWaitingList() == null ||
                !event.getWaitingList().contains(entrantId)) {
            Toast.makeText(getContext(), "You’re not on the waitlist!", Toast.LENGTH_SHORT).show();
            return;
        }

        event.getWaitingList().remove(entrantId);
        entrantVM.removeEventFromEntrant(event.getEventId());

        firebaseVM.leaveWaitingList(event.getEventId(), entrantId, () -> {},
                e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        firebaseVM.upsertEntrant(entrantVM.getCurrentEntrant(), () -> {},
                err -> Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show());

        Toast.makeText(getContext(), "Removed from waitlist.", Toast.LENGTH_SHORT).show();
        updateTotal(v);
    }

    private void clickedAcceptInvite(View v) {
        firebaseVM.signUpForEvent(event.getEventId(), entrantId,
                () -> Toast.makeText(getContext(), "Signed up successfully!", Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clickedRejectInvite(View v) {
        Toast.makeText(getContext(), "Rejected invite.", Toast.LENGTH_SHORT).show();
    }

    private void clickedTryAgain(View v) {
        Toast.makeText(getContext(), "Trying again...", Toast.LENGTH_SHORT).show();
    }

    // =====================================================================
    private void updateTotal(View v) {
        if (event.getWaitingList() != null) {
            int totalEntrant = event.getWaitingList().size();
            total.setText("Total: " + totalEntrant);
        }
    }

    private void updateWaitlistStatus() {

        if (event == null) return;

        if (entrantId != null &&
                event.getWaitingList() != null &&
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


    // Xan code for location, was giving erros so commented out for now

//    /** Updates the Entrant's location if permission is granted.
//     *  Pushes directly to server.
//     */
//    private void updateUserLocation(){
//        Log.d("Tag","In Update Location");
//        if (ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            //Ask for permission if they have not blocked it.
//            ActivityCompat.requestPermissions(requireActivity(), new String[] {"android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"},101);
//
//
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Log.d("Tag","Has Permission");
//        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
//            //Get Entrant
//            Entrant entrant = entrantVM.getCurrentEntrant();
//            if (entrant == null){
//                Log.d("NULL ERROR", "updateUserLocation: Entrant is NULL!!");
//                return;
//            }
//            entrant.setLocation(location);
//            firebaseVM.upsertEntrant(entrant,()->{Log.d("Updated location", "Updated Location");},e -> {
//                Log.d("Firebase Location Issue","EventDetails".concat(e.toString()));
//            });
//        }).addOnFailureListener(e -> {
//            Log.d("Firebase Location Issue","EventDetails".concat(e.toString()));
//        });
//
//    }
//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode,
//            @NonNull String[] permissions,
//            @NonNull int[] grantResults
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == 101) {
//
//            // If at least 1 permission is granted → continue
//            if (grantResults.length > 0 &&
//                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                Log.d("Tag", "User granted 1-time permission");
//                updateUserLocation();   // <-- Continue normally
//            }
//            else {
//                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

}

