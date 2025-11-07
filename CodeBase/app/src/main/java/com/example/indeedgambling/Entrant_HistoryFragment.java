package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Entrant_HistoryFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private EntrantViewModel entrantVM;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListView listView;
    private HistoryAdapter adapter;
    private List<Event> historyEvents = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_history_fragment, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Button home = view.findViewById(R.id.entrant_home_button_history);
        listView = view.findViewById(R.id.entrant_activity_history);

        home.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrantHistoryFragment_to_entrantHomeFragment));

        loadEntrantHistory(); // Load all event history on open
        return view;
    }

    private void loadEntrantHistory() {
        String entrantId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (entrantId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyEvents.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);

                        // Make null-safe checks
                        if ((event.getWaitingList() != null && event.getWaitingList().contains(entrantId)) ||
                                (event.getInvitedList() != null && event.getInvitedList().contains(entrantId)) ||
                                (event.getAcceptedEntrants() != null && event.getAcceptedEntrants().contains(entrantId)) ||
                                (event.getCancelledEntrants() != null && event.getCancelledEntrants().contains(entrantId))) {
                            historyEvents.add(event);
                        }
                    }

                    Log.d("HistoryFragment", "Loaded " + historyEvents.size() + " events for entrant");

                    adapter = new HistoryAdapter(requireContext(), historyEvents);
                    listView.setAdapter(adapter);

                    if (historyEvents.isEmpty()) {
                        Toast.makeText(getContext(), "No event history found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HistoryFragment", "Failed to load events", e);
                    Toast.makeText(getContext(), "Error loading history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
