/**
 * Fragment that allows an organizer to browse all events in the system.
 *
 * Features:
 * - Displays events in a RecyclerView using EventsAdapter
 * - Observes the live event list from FirebaseViewModel
 * - Reacts to changes in Firestore in real time
 *
 * Notes:
 * - Organizer can later add event-click behavior inside the adapter callback
 * - Layout uses organization_browse_fragment.xml
 */
package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Organizer_BrowseFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private OrganizerViewModel organizerVM;
    private EventsAdapter adapter;

    /**
     * Inflates the organizer browse layout, initializes ViewModels,
     * sets up RecyclerView, and observes the event list from Firebase.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.organization_browse_fragment, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);

        Button homeButton = v.findViewById(R.id.org_browse_home);
        homeButton.setOnClickListener(view -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_organizerBrowseFragment_to_organizerHomeFragment);
        });
        RecyclerView rv = v.findViewById(R.id.org_browse_recycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EventsAdapter(event -> {

        }, firebaseVM);

        rv.setAdapter(adapter);

        firebaseVM.getEventsLive()
                .observe(getViewLifecycleOwner(), events -> adapter.setData(events));

        return v;
    }
}
