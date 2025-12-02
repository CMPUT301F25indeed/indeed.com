package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Organizer notification control screen.
 *
 * Supports:
 *  - US 02.05.01: Notify invited (lottery win)
 *  - US 02.06.03: See final list of enrolled entrants
 *  - US 02.06.04: Cancel entrants that did not sign up
 *
 * The event is taken from OrganizerViewModel.getSelectedEvent().
 */
public class NotificationSenderFragment extends Fragment {

    private OrganizerViewModel organizerVM;
    private FirebaseViewModel firebaseVM;

    private TextView eventInfo;
    private Button buttonNotifyInvited;
    private Button buttonShowEnrolled;
    private Button buttonCancelNonResponders;
    private Button buttonBack;
    private ListView listEnrolled;

    private ArrayAdapter<String> enrolledAdapter;

    public NotificationSenderFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_sender_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        eventInfo = view.findViewById(R.id.notification_event_info);
        buttonNotifyInvited = view.findViewById(R.id.button_notify_invited);
        buttonShowEnrolled = view.findViewById(R.id.button_show_enrolled);
        buttonCancelNonResponders = view.findViewById(R.id.button_cancel_nonresponders);
        buttonBack = view.findViewById(R.id.button_notifications_back);
        listEnrolled = view.findViewById(R.id.list_enrolled_entrants);

        Event currentEvent = null;
        if (organizerVM.getSelectedEvent() != null) {
            currentEvent = organizerVM.getSelectedEvent().getValue();
        }

        if (currentEvent == null) {
            Toast.makeText(requireContext(),
                    "No event selected. Open from an upcoming event.",
                    Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_notificationSender_to_organizerUpcoming);
            return;
        }

        setupEventInfo(currentEvent);
        setupButtons(currentEvent);
    }

    /**
     * Shows basic information about the selected event.
     */
    private void setupEventInfo(Event event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Event: ").append(event.getEventName());

        String locationString = event.getLocationString();
        if (locationString != null && !locationString.isEmpty()) {
            sb.append("\nLocation: ").append(locationString);
        }

        sb.append("\nID: ").append(event.getEventId());
        eventInfo.setText(sb.toString());
    }

    /**
     * Wires up the three main organizer actions.
     */
    private void setupButtons(Event event) {
        final String eventId = event.getEventId();

        // US 02.05.01: notify all invited entrants they won the lottery.
        buttonNotifyInvited.setOnClickListener(v -> {
            firebaseVM.notifyInvitedEntrantsLotteryWin(
                    eventId,
                    () -> Toast.makeText(requireContext(),
                            "Notified invited entrants.",
                            Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(requireContext(),
                            "Failed to notify invited entrants.",
                            Toast.LENGTH_SHORT).show()
            );
        });

        // US 02.06.03: show final list of enrolled entrants (acceptedEntrants).
        buttonShowEnrolled.setOnClickListener(v -> {
            firebaseVM.getEventAcceptedEntrants(
                    eventId,
                    profiles -> {
                        List<String> names = new ArrayList<>();
                        for (Profile p : profiles) {
                            names.add(p.getPersonName());
                        }
                        if (names.isEmpty()) {
                            names.add("No enrolled entrants yet.");
                        }
                        enrolledAdapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_list_item_1,
                                names
                        );
                        listEnrolled.setAdapter(enrolledAdapter);
                    },
                    e -> Toast.makeText(requireContext(),
                            "Failed to load enrolled entrants.",
                            Toast.LENGTH_SHORT).show()
            );
        });

        // US 02.06.04: cancel invited entrants who never accepted, send loss notifications.
        buttonCancelNonResponders.setOnClickListener(v -> {
            firebaseVM.cancelNonRespondingInvitedEntrants(
                    eventId,
                    () -> Toast.makeText(requireContext(),
                            "Cancelled non-responders and sent loss notifications.",
                            Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(requireContext(),
                            "Failed to cancel non-responders.",
                            Toast.LENGTH_SHORT).show()
            );
        });

        buttonBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_notificationSender_to_organizerUpcoming));
    }
}