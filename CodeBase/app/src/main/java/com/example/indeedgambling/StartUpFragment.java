package com.example.indeedgambling;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.DocumentSnapshot;

public class StartUpFragment extends Fragment {
    public StartUpFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_up_fragment, container, false);
        Button loginButton = view.findViewById(R.id.button_login);
        Button signupButton = view.findViewById(R.id.button_signup);

        loginButton.setOnClickListener(v -> showLoginPopup());
        signupButton.setOnClickListener(v -> showSignUpPopup());


        return view;
    }

    private void showLoginPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.login_dialog, null);
        EditText usernameInput = popupView.findViewById(R.id.login_username);
        EditText passwordInput = popupView.findViewById(R.id.login_password);

        new AlertDialog.Builder(requireContext())
                .setTitle("Login")
                .setView(popupView)
                .setPositiveButton("Login", (dialog, which) -> {
                    String username = usernameInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    if (username.isEmpty() || password.isEmpty()) {
                        new AlertDialog.Builder(requireContext())
                                .setMessage("Please enter both username and password.")
                                .setPositiveButton("OK", null).show();

                        return;
                    }

                    FirebaseViewModel fvm = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
                    fvm.loadProfileByLogin(username, password, (DocumentSnapshot doc) -> {
                        if (!doc.exists()) {
                            new AlertDialog.Builder(requireContext())
                                    .setMessage("User not found.")
                                    .setPositiveButton("OK", null).show();
                            return;
                        }
                        String role = doc.getString("role");
                        if ("entrant".equalsIgnoreCase(role)) {
                            EntrantViewModel evm = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
                            Entrant e = doc.toObject(Entrant.class);
                            evm.setEntrant(e);
                            NavHostFragment.findNavController(this).navigate(R.id.action_startUp_to_entrantHome);
                        } else if ("organizer".equalsIgnoreCase(role)) {
                            OrganizerViewModel ovm = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
                            Organizer o = doc.toObject(Organizer.class);
                            ovm.setOrganizer(o);
                            NavHostFragment.findNavController(this).navigate(R.id.action_startUp_to_organizerHome);
                        } else if ("admin".equalsIgnoreCase(role)) {
                            AdminViewModel avm = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
                            Admin a = doc.toObject(Admin.class);
                            avm.setAdmin(a);
                            NavHostFragment.findNavController(this).navigate(R.id.action_startUp_to_adminHome);
                        } else {
                            new AlertDialog.Builder(requireContext())
                                    .setMessage("Unknown role.")
                                    .setPositiveButton("OK", null).show();
                        }
                    }, e -> new AlertDialog.Builder(requireContext())
                            .setMessage("Login error: " + e.getMessage()).setPositiveButton("OK", null).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSignUpPopup() {
        new AlertDialog.Builder(requireContext())
                .setMessage("Sign up will be implemented later.")
                .setPositiveButton("OK", null).show();
    }
}
