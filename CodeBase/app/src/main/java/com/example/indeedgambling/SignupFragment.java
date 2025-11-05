package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Signs up new user as entrant or organizer in a fragment.
 * Allows users to create either an Entrant or Organizer account
 * by providing their name, email, phone number, password, and role.
 * Handles input validation, hashing, and database insertion through FirebaseViewModel.
 */
public class SignupFragment extends Fragment {

    // used for firebase operations
    private FirebaseViewModel firebaseVM;

    // used for storing entrant info
    private EntrantViewModel entrantVM;

    // used for storing organizer info
    private OrganizerViewModel organizerVM;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signup_fragment, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        EditText name = v.findViewById(R.id.signUpName);
        EditText email = v.findViewById(R.id.signUpEmail);
        EditText phone = v.findViewById(R.id.signUpPhone);
        EditText pass = v.findViewById(R.id.signUpPassword);
        Spinner role = v.findViewById(R.id.signUpRole);
        Button signup = v.findViewById(R.id.signUpBtn);
        Button back = v.findViewById(R.id.signUpBackBtn);

        // clicked when user is ready with all required information
        signup.setOnClickListener(view -> {
            String n = name.getText().toString().trim();
            String e = email.getText().toString().trim();
            String ph = phone.getText().toString().trim();
            String p = pass.getText().toString().trim();
            String r = role.getSelectedItem().toString();

            // no empty name
            if (n.isEmpty()) {
                name.setError("Name required");
                name.requestFocus();
                return;
            }

            // no empty password
            if (p.isEmpty()) {
                pass.setError("Password required");
                pass.requestFocus();
                return;
            }

            // no empty email or non-valid email
            if (e.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                email.setError("Valid email required");
                email.requestFocus();
                return;
            }

            // empty phone number is allowed, but no non-valid phone number
            if (!ph.isEmpty()) {
                if (!Patterns.PHONE.matcher(ph).matches() || ph.length() < 10) {
                    phone.setError("Valid phone number required");
                    phone.requestFocus();
                    return;
                }
            }

            // no dups via no dups emails
            firebaseVM.checkEmailExists(e, exists -> {
                if (exists) {
                    email.setError("Email already in use");
                    email.requestFocus();
                } else {
                    String profileId = HashUtil.generateId(e, p);
                    String passwordHash = HashUtil.sha256(p);

                    Profile prof = new Profile(profileId, n, e, ph, r, passwordHash);

                    if (r.equalsIgnoreCase("Entrant")) {
                        Entrant entrant = new Entrant(profileId, n, e, ph, passwordHash);
                        firebaseVM.upsertEntrant(
                                entrant,
                                () -> {
                                    entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);
                                    entrantVM.setEntrant(entrant);
                                    Toast.makeText(getContext(), "Account created", Toast.LENGTH_SHORT).show();
                                    NavHostFragment.findNavController(this)
                                            .navigate(R.id.action_signupFragment_to_entrantHomeFragment);
                                },
                                err -> Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    } else {
                        firebaseVM.upsertProfile(
                                prof,
                                () -> {
                                    Toast.makeText(getContext(), "Account created", Toast.LENGTH_SHORT).show();
                                    if (r.equalsIgnoreCase("Organizer")) {
                                        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
                                        organizerVM.setOrganizer(prof);
                                        NavHostFragment.findNavController(this)
                                                .navigate(R.id.action_signupFragment_to_organizerHomeFragment);
                                    }
                                },
                                err -> Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }, err -> Toast.makeText(getContext(), "Error checking email: " + err.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // back button
        back.setOnClickListener(v2 ->
                NavHostFragment.findNavController(this).navigate(R.id.startUpFragment)
        );

        return v;
    }
}
