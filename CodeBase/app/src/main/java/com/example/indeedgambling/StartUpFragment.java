/**
 * Handles the initial launch behavior of the application.
 *
 * This fragment attempts device-based auto-login for Entrants only.
 * Organizer and Admin users are never auto-logged in for security reasons.
 *
 * Behavior:
 * - Retrieves the deviceId from the Android system.
 * - Checks Firestore for a profile that matches the deviceId.
 * - If the profile belongs to an Entrant, the user is logged in automatically.
 * - Organizer/Admin users always see login/signup options.
 *
 * Notes:
 * - Auto-login applies only to Entrants.
 */
package com.example.indeedgambling;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class StartUpFragment extends Fragment {

    public StartUpFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.start_up_fragment, container, false);

        Button loginBtn = view.findViewById(R.id.buttonLogin);
        Button signupBtn = view.findViewById(R.id.goToSignup);

        FirebaseViewModel firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        EntrantViewModel entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        firebaseVM.findProfileByDeviceId(deviceId, profile -> {

            if (profile != null && profile.getRole().equalsIgnoreCase("entrant")) {

                Entrant entrant = new Entrant(
                        profile.getProfileId(),
                        profile.getPersonName(),
                        profile.getEmail(),
                        profile.getPhone(),
                        profile.getPasswordHash()
                );

                if (profile.getEventsJoined() != null)
                    entrant.setAllEvents(profile.getEventsJoined());

                entrantVM.setEntrant(entrant);

                NavHostFragment.findNavController(this)
                        .navigate(R.id.entrantHomeFragment);

                return;
            }

            showLoginButtons(loginBtn, signupBtn);

        }, err -> showLoginButtons(loginBtn, signupBtn));

        return view;
    }

    private void showLoginButtons(Button loginBtn, Button signupBtn) {

        loginBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_startUpFragment_to_loginFragment)
        );

        signupBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_startUpFragment_to_signupFragment)
        );
    }
}
