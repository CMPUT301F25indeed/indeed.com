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
import androidx.navigation.fragment.NavHostFragment;

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
    private String entrantId,entrantRelation;

    private TextView name, desc, waitlistStatus, total;
    private Button yesBtn, noBtn, backBtn,tryAgainBtn;

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


        entrantId = entrantVM.returnID();

        name = v.findViewById(R.id.event_name);
        desc = v.findViewById(R.id.event_description);
        waitlistStatus = v.findViewById(R.id.waitlist_status);
        backBtn = v.findViewById(R.id.back_button);

        total = v.findViewById(R.id.event_total_entrant);
        yesBtn = v.findViewById(R.id.yes_button);
        noBtn = v.findViewById(R.id.no_button);
        tryAgainBtn =  v.findViewById(R.id.try_again_button);


        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            loadEventDetails(v);
        }

        if (event != null) {
            name.setText(event.getEventName());
            desc.setText(event.getDescription());
            entrantRelation = event.whichList(entrantId);

        }

        backBtn.setOnClickListener(v1 -> requireActivity().onBackPressed());

        // Cases:
        //      waitlist
        //      none
        //      accepted --> Entrant accepted their invite
        //      cancelled --> Entrant rejects invite/Gets to try again
        //      invited --> Entrant get to accept to rejects their invite

        // on waitlist and not on waitlist case

        if (entrantRelation.equals("none")||entrantRelation.equals("waitlist")){


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


        if (entrantRelation.equals("invited")){
            // Backup. If it is somehow still in the waitlist list remove it for an entrant
            // NOTE: if lottery process/selection works it needs to do this to avoid errors
            // contact Tj for more info
            // Back Up - Start
            entrantVM.inviteEntrantRemoveWaitlist(event.getEventId());
            firebaseVM.upsertEntrant(entrantVM.getCurrentEntrant(),() -> {},
                    err -> Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
            );
            // Back Up - END


            yesBtn.setText("Accept\n Invite");
            noBtn.setText("Reject\n Invite");
            yesBtn.setVisibility(View.VISIBLE);
            noBtn.setVisibility(View.VISIBLE);
            tryAgainBtn.setVisibility(View.GONE);

            yesBtn.setOnClickListener(v1 -> {
                clickedAcceptInvite(v);
                // create the updatewaitliststatus function for update invite status
            });

            noBtn.setOnClickListener(v1 -> {
                clickedRejectInvite(v);
                // create the updatewaitliststatus function for update invite status
            });



        }

        if (entrantRelation.equals("cancelled")){


            tryAgainBtn.setText("Try Again?");
            yesBtn.setVisibility(View.GONE);
            noBtn.setVisibility(View.GONE);
            tryAgainBtn.setVisibility(View.VISIBLE);

            tryAgainBtn.setOnClickListener(v1 -> {
                clickedTryAgain(v);
            });



        }

        return v;
    }


    private void clickedJoinWaitlist(View v){
        if (entrantId == null) {
            Toast.makeText(getContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.getWaitingList() != null && event.getWaitingList().contains(entrantId)) {
            Toast.makeText(getContext(), "You already joined this waitlist!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.tryaddtoWaitingList(entrantId)){
            Toast.makeText(getContext(), "Waitlist is FULL!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        entrantVM.addEventToEntrant(event.getEventId());
        event.getWaitingList().add(entrantId);


        firebaseVM.joinWaitingList(event.getEventId(), entrantId, () -> {},
                e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        firebaseVM.upsertEntrant(entrantVM.getCurrentEntrant(),() -> {},
                err -> Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
        );

        Toast.makeText(getContext(), "Joined waitlist!", Toast.LENGTH_SHORT).show();
        updateTotal(v);
    }

    private void clickedLeaveWaitlist(View v){
            if (entrantId == null) {
                Toast.makeText(getContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (event.getWaitingList() == null || !event.getWaitingList().contains(entrantId)) {
                Toast.makeText(getContext(), "You’re not on the waitlist!", Toast.LENGTH_SHORT).show();
                return;
            }


        event.getWaitingList().remove(entrantId);
        entrantVM.removeEventFromEntrant(event.getEventId()); // needs to update database as well

        firebaseVM.leaveWaitingList(event.getEventId(), entrantId, () -> {},
                    e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());


        firebaseVM.upsertEntrant(entrantVM.getCurrentEntrant(),() -> {},
                err -> Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show());

        Toast.makeText(getContext(), "Removed from waitlist.", Toast.LENGTH_SHORT).show();
        updateTotal(v);
    }

    private void clickedAcceptInvite(View v){
        if (entrantId == null) {
            Toast.makeText(getContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseVM.signUpForEvent(event.getEventId(), entrantId,
                () -> Toast.makeText(getContext(), "Signed up successfully!", Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

}
    private void clickedRejectInvite(View v){
        String tj = "tj";

    }

    private void clickedTryAgain(View v){
        String tj = "tj";

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

        updateTotal(v);


        //updateWaitlistStatus();
    }

    private void updateTotal(View v){
        if (event.getWaitingList() != null){
            int totalEntrant = event.getWaitingList().size();
            total.setText("Total: " + totalEntrant);

        }
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
            yesBtn.setEnabled(false);
            yesBtn.setText("Joined\nWaitlist");

        } else {
            waitlistStatus.setVisibility(View.GONE);
            yesBtn.setEnabled(true);
            yesBtn.setText("Join Waitlist");
        }
    }

}
