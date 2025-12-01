package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.List;

/**
 * Displays the event history for an Entrant.
 *
 * This fragment shows all events in which the user has participated
 * or been invited, based on Firestore data retrieved through
 * FirebaseViewModel. Entrants can review their status in each event
 * and respond to pending invitations.
 *
 * Features:
 * - Loads entrant-specific event history using profileId
 * - Shows invitation status (waiting, invited, accepted, cancelled)
 * - Allows accepting or declining event invitations
 * - Automatically refreshes the history list
 */
public class Entrant_HistoryFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private EntrantViewModel entrantVM;
    private ListView listView;
    private EntrantHistoryAdapter adapter;
    private String currentEntrantId;

    /**
     * Creates the history screen layout, loads the entrant profile,
     * and initializes event history retrieval and list interaction.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.entrant_history_fragment, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        Button home = view.findViewById(R.id.entrant_home_button_history);
        listView = view.findViewById(R.id.entrant_activity_history);

        home.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrant_HistoryFragment_to_entrantHomeFragment)
        );

        Profile entrant = entrantVM.getCurrentEntrant();
        if (entrant != null && entrant.getProfileId() != null) {

            currentEntrantId = entrant.getProfileId();

            firebaseVM.fetchEntrantHistory(
                    currentEntrantId,
                    events -> updateHistory(events, currentEntrantId),
                    e -> Log.e("Entrant_HistoryFragment", "Failed to load history", e)
            );
        }

        listView.setOnItemClickListener((parent, itemView, position, id) -> {

            if (adapter == null || currentEntrantId == null) return;

            Event event = adapter.getItem(position);
            if (event == null) return;

            String listName = event.whichList(currentEntrantId);
            if (!"invited".equals(listName)) return;

            showAcceptDialog(event);
        });

        return view;
    }

    /**
     * Updates the list of history items displayed to the user.
     */
    private void updateHistory(List<Event> events, String entrantId) {
        if (adapter == null) {
            adapter = new EntrantHistoryAdapter(requireContext(), events, entrantId);
            listView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(events);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Opens a dialog allowing the user to accept or decline
     * an event invitation sent by the organizer.
     */
    private void showAcceptDialog(Event event) {

        new AlertDialog.Builder(requireContext())
                .setTitle("Respond to Invitation")
                .setMessage("Do you want to accept or decline the invitation for \"" +
                        event.getEventName() + "\"?")
                .setPositiveButton("Accept", (dialog, which) -> {

                    if (currentEntrantId == null || currentEntrantId.isEmpty()) return;

                    firebaseVM.acceptInvitation(
                            event.getEventId(),
                            currentEntrantId,
                            () -> {
                                Toast.makeText(requireContext(),
                                        "Invitation accepted",
                                        Toast.LENGTH_SHORT).show();

                                firebaseVM.fetchEntrantHistory(
                                        currentEntrantId,
                                        events -> updateHistory(events, currentEntrantId),
                                        e -> Log.e("Entrant_HistoryFragment",
                                                "Failed to refresh history after accept", e)
                                );
                            },
                            e -> {
                                Log.e("Entrant_HistoryFragment",
                                        "Failed to accept invitation", e);
                                Toast.makeText(requireContext(),
                                        "Error accepting invitation",
                                        Toast.LENGTH_SHORT).show();
                            }
                    );
                })
                .setNegativeButton("Decline", (dialog, which) -> {

                    if (currentEntrantId == null || currentEntrantId.isEmpty()) return;

                    firebaseVM.declineInvitation(
                            event.getEventId(),
                            currentEntrantId,
                            () -> {
                                Toast.makeText(requireContext(),
                                        "Invitation declined",
                                        Toast.LENGTH_SHORT).show();

                                firebaseVM.fetchEntrantHistory(
                                        currentEntrantId,
                                        events -> updateHistory(events, currentEntrantId),
                                        e -> Log.e("Entrant_HistoryFragment",
                                                "Failed to refresh history after decline", e)
                                );
                            },
                            e -> {
                                Log.e("Entrant_HistoryFragment",
                                        "Failed to decline invitation", e);
                                Toast.makeText(requireContext(),
                                        "Error declining invitation",
                                        Toast.LENGTH_SHORT).show();
                            }
                    );
                })
                .setNeutralButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
