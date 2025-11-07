package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Displays detailed information about a selected event and manages entrant interactions.
 *
 * This fragment connects:
 * - Event (event object passed via arguments)
 * - Entrant (retrieved from EntrantViewModel)
 * - FirebaseViewModel (handles Firestore operations)
 *
 * Features:
 * - Shows event details: name, description, location, category, status, and dates.
 * - Lets entrants join or leave the event waiting list.
 * - Allows entrants to sign up for an event when eligible.
 * - Updates UI to show whether the entrant is already on the waiting list.
 *
 * Fields:
 * - firebaseVM: handles Firestore communication for joining/leaving/signing up.
 * - entrantVM: provides the currently logged-in entrant’s profile ID.
 * - event: the Event object currently displayed.
 * - entrantId: ID of the entrant using this fragment.
 * - waitlistStatus, joinBtn, leaveBtn, signUpBtn, backBtn: UI elements.
 *
 * User Flow:
 * 1. Fragment loads event details via arguments.
 * 2. EntrantViewModel provides logged-in entrant profile.
 * 3. Entrant can:
 *    - Join the waiting list (adds entrant ID in Firestore and updates UI).
 *    - Leave the waiting list (removes entrant ID and updates UI).
 *    - Sign up for event directly.
 * 4. Back button navigates to the previous fragment.
 *
 * Used via navigation from event browsing or search fragments.
 * Data updates are reflected live through FirebaseViewModel operations.
 */
public class EventDetailsFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private EntrantViewModel entrantVM;
    private Event event;
    private String entrantId;

    private TextView name, desc, waitlistStatus;
    private Button joinBtn, leaveBtn, signUpBtn, backBtn;

    /**
     * Initializes and displays the event details screen.
     * Retrieves entrant information from EntrantViewModel
     * and binds all event-related actions to Firestore operations.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_event_details, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        Entrant currentEntrant = entrantVM.getCurrentEntrant();
        if (currentEntrant != null) {
            entrantId = currentEntrant.getProfileId();
        } else {
            entrantId = "TEST_USER";
            Toast.makeText(getContext(), "No entrant loaded — using TEST_USER.", Toast.LENGTH_SHORT).show();
        }

        name = v.findViewById(R.id.event_name);
        desc = v.findViewById(R.id.event_description);
        waitlistStatus = v.findViewById(R.id.waitlist_status);
        joinBtn = v.findViewById(R.id.join_waitlist_button);
        leaveBtn = v.findViewById(R.id.leave_waitlist_button);
        signUpBtn = v.findViewById(R.id.signup_button);
        backBtn = v.findViewById(R.id.back_button);

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            loadEventDetails(v);
        }

        if (event != null) {
            name.setText(event.getEventName());
            desc.setText(event.getDescription());
            updateWaitlistStatus();
        }

        backBtn.setOnClickListener(v1 -> requireActivity().onBackPressed());

        joinBtn.setOnClickListener(v1 -> {
            if (entrantId == null) {
                Toast.makeText(getContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (event.getWaitingList() != null && event.getWaitingList().contains(entrantId)) {
                Toast.makeText(getContext(), "You already joined this waitlist!", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseVM.joinWaitingList(event.getEventId(), entrantId,
                    () -> {
                        event.getWaitingList().add(entrantId);
                        updateWaitlistStatus();
                        Toast.makeText(getContext(), "Joined waitlist!", Toast.LENGTH_SHORT).show();
                    },
                    e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        leaveBtn.setOnClickListener(v1 -> {
            if (entrantId == null) {
                Toast.makeText(getContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (event.getWaitingList() == null || !event.getWaitingList().contains(entrantId)) {
                Toast.makeText(getContext(), "You’re not on the waitlist!", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseVM.leaveWaitingList(event.getEventId(), entrantId,
                    () -> {
                        event.getWaitingList().remove(entrantId);
                        updateWaitlistStatus();
                        Toast.makeText(getContext(), "Removed from waitlist.", Toast.LENGTH_SHORT).show();
                    },
                    e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        signUpBtn.setOnClickListener(v1 -> {
            if (entrantId == null) {
                Toast.makeText(getContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseVM.signUpForEvent(event.getEventId(), entrantId,
                    () -> Toast.makeText(getContext(), "Signed up successfully!", Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        return v;
    }

    /**
     * Loads and formats all event details for display.
     *
     */
    private void loadEventDetails(View v) {
        if (event == null) return;

        name.setText(event.getEventName());
        desc.setText(event.getDescription());

        TextView loc = v.findViewById(R.id.event_location);
        TextView dates = v.findViewById(R.id.event_dates);
        TextView regPeriod = v.findViewById(R.id.event_registration);
        TextView status = v.findViewById(R.id.event_status);
        TextView category = v.findViewById(R.id.event_category);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

        if (event.getLocation() != null)
            loc.setText("Location: " + event.getLocation());

        if (event.getCategory() != null)
            category.setText("Category: " + event.getCategory());

        if (event.getEventStart() != null && event.getEventEnd() != null)
            dates.setText("Event Dates: " + sdf.format(event.getEventStart()) + " – " + sdf.format(event.getEventEnd()));

        if (event.getRegistrationStart() != null && event.getRegistrationEnd() != null)
            regPeriod.setText("Registration: " + sdf.format(event.getRegistrationStart()) + " – " + sdf.format(event.getRegistrationEnd()));

        status.setText("Status: " + event.getStatus());

        updateWaitlistStatus();
    }

    /**
     * Updates the visual state of the waiting list section and buttons
     * depending on whether the current entrant is already listed.
     */
    private void updateWaitlistStatus() {
        if (event == null) return;

        if (entrantId != null && event.getWaitingList() != null && event.getWaitingList().contains(entrantId)) {
            waitlistStatus.setVisibility(View.VISIBLE);
            waitlistStatus.setText("✅ You are on the waiting list!");
            joinBtn.setEnabled(false);
            joinBtn.setText("Already Joined");
        } else {
            waitlistStatus.setVisibility(View.GONE);
            joinBtn.setEnabled(true);
            joinBtn.setText("Join Waiting List");
        }
    }
}
