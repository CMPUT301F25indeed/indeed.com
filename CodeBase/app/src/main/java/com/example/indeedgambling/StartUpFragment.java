package com.example.indeedgambling;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Date;

public class StartUpFragment extends Fragment {
    public StartUpFragment() {}

    private FireBaseViewModel Data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_up_fragment, container, false);

        //Syncing Firebase on startup screen
        Data = new ViewModelProvider(requireActivity()).get(FireBaseViewModel.class);

        Button loginButton = view.findViewById(R.id.button_login);
        Button signupButton = view.findViewById(R.id.button_signup);

        loginButton.setOnClickListener(v -> showLoginPopup());
        signupButton.setOnClickListener(v -> showSignUpPopup());

        Data.Add(new Profile("Tester","Tester"));
        Data.Add(new Profile("Password","ProfileName"));

        return view;
    }
    private void showLoginPopup() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.login_dialog, null);

        EditText usernameInput = popupView.findViewById(R.id.login_username);
        EditText passwordInput = popupView.findViewById(R.id.login_password);

        new AlertDialog.Builder(requireContext())
                .setTitle("Login")
                .setView(popupView)
                .setPositiveButton("Login", (dialog, which) -> {
                    String username = usernameInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();

                    // name and password must be filled
                    if (username.isEmpty() || password.isEmpty()) {
                        new AlertDialog.Builder(requireContext())
                                .setMessage("Please enter both username and password.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    // we are doing if == entrant, organizer, etc for now
                    if (username.equalsIgnoreCase("entrant")) {
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_startUp_to_entrantHome);
                    }
                    else if (username.equalsIgnoreCase("org")) {
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_startUp_to_organizerHome);
                    }

                    //Need to check if data matches any profile, and then which class.


                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSignUpPopup() {

    }

}
