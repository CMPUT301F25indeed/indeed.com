package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
/**
 * Fragment that allows an organizer to send notifications to event entrants.
 * <p>
 * Supports sending notifications to:
 * <ul>
 *     <li>Waiting list entrants</li>
 *     <li>Selected entrants</li>
 *     <li>Cancelled entrants</li>
 * </ul>
 * Retrieves the currently selected event from {@link OrganizerViewModel}.
 */

public class NotificationSenderFragment extends Fragment {
    /**
     * ViewModel for organizer state and event data.
     */

    private OrganizerViewModel viewModel;
    private EditText editEventId, editNotificationTitle, editNotificationMessage;
    private RadioGroup radioRecipientGroup;
    private Button buttonSend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_sender_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        initializeViews(view);
        setupSendButton();
        fetchEventId();
    }
    /**
     * Initializes all view references.
     *
     * @param view Root view of the fragment.
     */


    private void initializeViews(View view) {
        editEventId = view.findViewById(R.id.edit_event_id);
        editNotificationTitle = view.findViewById(R.id.edit_notification_title);
        editNotificationMessage = view.findViewById(R.id.edit_notification_message);
        radioRecipientGroup = view.findViewById(R.id.radio_recipient_group);
        buttonSend = view.findViewById(R.id.button_send_notification);
    }
    private void fetchEventId(){
        Event currentEvent = viewModel.getSelectedEvent().getValue();
        if (currentEvent != null && currentEvent.getEventId() != null) {
            editEventId.setText(currentEvent.getEventId());
            editEventId.setEnabled(false);
        }
    }

    private void setupSendButton() {
        buttonSend.setOnClickListener(v -> sendNotification());
    }

    /**
     * Sends a notification to the selected group of entrants.
     * Validates input and determines recipient group.
     */
    private void sendNotification() {
        // Get all input values
        String eventId = editEventId.getText().toString().trim();
        String title = editNotificationTitle.getText().toString().trim();
        String message = editNotificationMessage.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a notification title.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter notification content.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check which is selected
        int selectedId = radioRecipientGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(requireContext(), " Please select who to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        // build full message with title
        String fullMessage = title + ": " + message;

        Toast.makeText(requireContext(), " Sending notification...", Toast.LENGTH_SHORT).show();

        if (selectedId == R.id.radio_waiting_list) {
            sendToWaitingList(eventId, fullMessage);
        } else if (selectedId == R.id.radio_selected_entrants) {
            sendToSelectedEntrants(eventId, fullMessage);
        } else if (selectedId == R.id.radio_cancelled_entrants) {
            sendToCancelledEntrants(eventId, fullMessage);
        }
    }

    private void sendToWaitingList(String eventId, String message) {
        Event currentEvent = viewModel.getSelectedEvent().getValue();
        Log.d("DEBUG", "Waiting list check - Event: " + (currentEvent != null));
        Log.d("DEBUG", "Waiting list size: " + (currentEvent != null ? currentEvent.getWaitingList().size() : "null"));


        if (currentEvent != null && currentEvent.getWaitingList() != null
                && !currentEvent.getWaitingList().isEmpty()){
            //Log.d("DEBUG", "Sending to Firebase - Event ID: " + eventId);
            viewModel.notifyWaitingList(eventId, message,
                (Void aVoid) -> {
                    //Log.d("DEBUG", "Firebase SUCCESS - notification sent");
                    Toast.makeText(requireContext(), "Sent to waiting list!", Toast.LENGTH_SHORT).show();
                    requireActivity().getOnBackPressedDispatcher();
                },
                e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );} else{
            Toast.makeText(requireContext(), "No waiting list entrants.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToSelectedEntrants(String eventId, String message) {
        Event currentEvent = viewModel.getSelectedEvent().getValue();

        if (currentEvent != null && currentEvent.getInvitedList() != null
                && !currentEvent.getInvitedList().isEmpty()) {

            viewModel.notifySelectedEntrants(eventId, message,
                (Void aVoid) -> {
                    Toast.makeText(requireContext(), "Sent to selected entrants!", Toast.LENGTH_SHORT).show();
                    requireActivity().getOnBackPressedDispatcher();
                },
                e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }       else{
            Toast.makeText(requireContext(), "No selected entrants.", Toast.LENGTH_SHORT).show();
        }
    }
    private void sendToCancelledEntrants(String eventId, String message) {
        // For cancelled get the event first to access cancelled entrants
        Event currentEvent = viewModel.getSelectedEvent().getValue();
//        Log.d("DEBUG", "Current event: " + currentEvent);
//        Log.d("DEBUG", "Cancelled entrants: " + (currentEvent != null ? currentEvent.getCancelledEntrants() : "null"));

        if (currentEvent != null && currentEvent.getCancelledEntrants() != null
                && !currentEvent.getCancelledEntrants().isEmpty()) {

            viewModel.notifyCancelledEntrants(eventId, currentEvent.getCancelledEntrants(), message,
                    (Void aVoid) -> {
                        Toast.makeText(requireContext(), "Sent to cancelled entrants!", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    },
                    e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );

        } else {
            // No cancelled entrants - show message and return
            // Log.d("DEBUG", "No cancelled entrants - showing error");
            Toast.makeText(requireContext(), "No cancelled entrants found for this event", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        };
    }
}