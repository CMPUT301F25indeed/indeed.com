package com.example.indeedgambling;

import android.os.Bundle;
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

public class NotificationSenderFragment extends Fragment {

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
    }

    private void initializeViews(View view) {
        editEventId = view.findViewById(R.id.edit_event_id);
        editNotificationTitle = view.findViewById(R.id.edit_notification_title);
        editNotificationMessage = view.findViewById(R.id.edit_notification_message);
        radioRecipientGroup = view.findViewById(R.id.radio_recipient_group);
        buttonSend = view.findViewById(R.id.button_send_notification);
    }

    private void setupSendButton() {
        buttonSend.setOnClickListener(v -> sendNotification());
    }

    private void sendNotification() {
        // Get all input values
        String eventId = editEventId.getText().toString().trim();
        String title = editNotificationTitle.getText().toString().trim();
        String message = editNotificationMessage.getText().toString().trim();

        // Validate inputs
        if (eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter Event ID", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter notification title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter message content", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check which is selected
        int selectedId = radioRecipientGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(requireContext(), " Please select who to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build full message with title
        String fullMessage = title + ": " + message;

        Toast.makeText(requireContext(), " Sending notification...", Toast.LENGTH_SHORT).show();

        // Send based on recipient selection
        if (selectedId == R.id.radio_waiting_list) {
            sendToWaitingList(eventId, fullMessage);
        } else if (selectedId == R.id.radio_selected_entrants) {
            sendToSelectedEntrants(eventId, fullMessage);
        } else if (selectedId == R.id.radio_cancelled_entrants) {
            sendToCancelledEntrants(eventId, fullMessage);
        }
    }

    private void sendToWaitingList(String eventId, String message) {
        viewModel.notifyWaitingList(eventId, message,
                (Void aVoid) -> {
                    Toast.makeText(requireContext(), "Sent to waiting list!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigate(R.id.action_notificationSender_to_orgHome);
                },
                e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void sendToSelectedEntrants(String eventId, String message) {
        viewModel.notifySelectedEntrants(eventId, message,
                (Void aVoid) -> {
                    Toast.makeText(requireContext(), "Sent to selected entrants!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigate(R.id.action_notificationSender_to_orgHome);
                },
                e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
    private void sendToCancelledEntrants(String eventId, String message) {
        // For cancelled get the event first to access cancelled entrants
        viewModel.getEventById(eventId).observe(getViewLifecycleOwner(), event -> {
            if (event != null && event.getCancelledEntrants() != null && !event.getCancelledEntrants().isEmpty()) {
                viewModel.notifyCancelledEntrants(eventId, event.getCancelledEntrants(), message,
                        (Void aVoid) -> {
                            Toast.makeText(requireContext(), "Sent to cancelled entrants!", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(this).navigate(R.id.action_notificationSender_to_orgHome);
                        },
                        e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } else {
                Toast.makeText(requireContext(), "ℹ️ No cancelled entrants found for this event", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigate(R.id.action_notificationSender_to_orgHome);
            }
        });
    }
}