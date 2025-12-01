package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class Entrant_HomeFragment extends Fragment {

    private EntrantViewModel entrantVM;
    private FirebaseViewModel fvm;

    public Entrant_HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.entrant_home_fragment, container, false);

        ListView options = view.findViewById(R.id.entrant_home_buttons);
        Button LogoutButton = view.findViewById(R.id.entrant_logout_button_home);
        TextView greeting = view.findViewById(R.id.entrant_greeting_home);

        String[] entries = {"Browse", "History", "Profile", "Guidelines", "Notifications", "Scan QR Code"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_entrant_home_option,
                R.id.entrant_option_text,
                entries
        );

        options.setAdapter(adapter);

        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
        fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        // -------------------------
        // NEW: START GLOBAL LISTENER
        // -------------------------
        entrantVM.startNotificationListener(fvm);

        Profile p = entrantVM.getCurrentEntrant();
        greeting.setText("Hi " + p.getPersonName());

        if (p.getProfileId() != null) {
            String entrantId = p.getProfileId();

            // popup of latest notification
            fvm.fetchLatestNotification(entrantId,
                    notification -> {
                        if (notification != null)
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Notification")
                                    .setMessage(notification.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                    },
                    error -> Log.e("Entrant_HomeFragment", "Latest notification error", error));
        }

        options.setOnItemClickListener((parent, itemView, position, id) -> {
            switch (position) {
                case 0:
                    NavHostFragment.findNavController(this).navigate(R.id.entrant_BrowseFragment);
                    break;
                case 1:
                    NavHostFragment.findNavController(this).navigate(R.id.entrant_HistoryFragment);
                    break;
                case 2:
                    Bundle args = new Bundle();
                    args.putString("profileID", p.getProfileId());
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.settingsFragment, args);
                    break;
                case 3:
                    NavHostFragment.findNavController(this).navigate(R.id.entrant_GuidelinesFragment);
                    break;
                case 4:
                    NavHostFragment.findNavController(this).navigate(R.id.entrant_NotificationsFragment);
                    break;
                case 5:
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_entrantHomeFragment_to_scanQRFragment);
                    break;
            }
        });

        LogoutButton.setOnClickListener(v -> {
            fvm.updateProfile(
                    p.getProfileId(),
                    java.util.Collections.singletonMap("deviceId", null),
                    () -> {
                        entrantVM.setEntrant(null);
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_entrantHomeFragment_to_startUpFragment);
                    },
                    err -> {
                        entrantVM.setEntrant(null);
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_entrantHomeFragment_to_startUpFragment);
                    }
            );
        });

        return view;
    }
}
