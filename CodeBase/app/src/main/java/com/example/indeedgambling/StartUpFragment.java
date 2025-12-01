/**
 * Fragment responsible for handling the initial launch flow of the application.
 *
 * Primary Purpose:
 * - Attempts automatic login using a stored deviceId for Entrant profiles.
 *
 * Behavior Summary:
 * - Retrieves the deviceId from Android system settings.
 * - Queries Firestore for a profile with a matching deviceId.
 * - If the profile is an Entrant, a session is restored and the user is
 *   navigated directly to the Entrant home screen.
 * - Organizer and Admin profiles are never auto-logged in and will always
 *   see the Login and Signup buttons.
 *
 * Notes:
 * - Auto-login applies exclusively to Entrants.
 * - Organizer/Admin roles require manual authentication for security.
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

    /**
     * Inflates the startup screen and begins the auto-login logic.
     * Displays login/signup buttons only if auto-login fails or is not applicable.
     *
     * @param inflater layout inflater
     * @param container view container
     * @param savedInstanceState previous state, if any
     * @return the inflated view
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.start_up_fragment, container, false);

        Button loginBtn = view.findViewById(R.id.buttonLogin);
        Button signupBtn = view.findViewById(R.id.goToSignup);

        FirebaseViewModel firebaseVM =
                new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        EntrantViewModel entrantVM =
                new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        /**
         * Retrieve the unique device identifier.
         * Used only for automatic Entrant login.
         */
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        /**
         * Attempts to match the deviceId to an Entrant profile.
         * If successful, restores Entrant session.
         */
        firebaseVM.findProfileByDeviceId(deviceId, profile -> {

            if (profile != null &&
                    profile.getRole().equalsIgnoreCase("entrant")) {

                Entrant entrant = new Entrant(
                        profile.getProfileId(),
                        profile.getPersonName(),
                        profile.getEmail(),
                        profile.getPhone(),
                        profile.getPasswordHash()
                );

                if (profile.getEventsJoined() != null) {
                    entrant.setAllEvents(profile.getEventsJoined());
                }

                entrantVM.setEntrant(entrant);

                NavHostFragment.findNavController(this)
                        .navigate(R.id.entrantHomeFragment);

                return;
            }

            showLoginButtons(loginBtn, signupBtn);

        }, err -> showLoginButtons(loginBtn, signupBtn));

        return view;
    }

    /**
     * Displays the login and signup buttons and binds navigation actions.
     *
     * @param loginBtn login button
     * @param signupBtn signup button
     */
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
