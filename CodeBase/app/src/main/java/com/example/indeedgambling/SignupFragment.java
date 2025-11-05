package com.example.indeedgambling;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.lifecycle.ViewModelProvider;
import android.util.Patterns;

public class SignupFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private EntrantViewModel entrantVM;
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

        signup.setOnClickListener(view -> {
            String n = name.getText().toString().trim();
            String e = email.getText().toString().trim();
            String ph = phone.getText().toString().trim();
            String p = pass.getText().toString().trim();
            String r = role.getSelectedItem().toString();


            if (n.isEmpty()) {
                name.setError("Name Required");
                name.requestFocus();
                return;
            }

            if (p.isEmpty()) {
                pass.setError("Password Required");
                pass.requestFocus();
                return;
            }

            if (e.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                email.setError("Valid Email required");
                email.requestFocus();
                return;
            }

            if (!ph.isEmpty()){
                if (!Patterns.PHONE.matcher(ph).matches() || ph.length() < 10) {
                    phone.setError("Valid phone number required");
                    phone.requestFocus();
                    return;
                }
            }


            String profileId = HashUtil.generateId(e, p); // email + password hash
            String passwordHash = HashUtil.sha256(p);

            Profile prof = new Profile(profileId, n, e, ph, r, passwordHash);

            firebaseVM.upsertProfile(prof,
                    () -> {
                        Toast.makeText(getContext(), "Account created", Toast.LENGTH_SHORT).show();
                        if (r.equalsIgnoreCase("Entrant")) {
                            entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

                            entrantVM.setEntrant(prof);
                            NavHostFragment.findNavController(this)
                                    .navigate(R.id.action_signupFragment_to_entrantHomeFragment);

                        } else if (r.equalsIgnoreCase("Organizer")) {
                            organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);

                            organizerVM.setOrganizer(prof);
                            NavHostFragment.findNavController(this)
                                    .navigate(R.id.action_signupFragment_to_organizerHomeFragment);
                        }


                    },
                    err -> Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        back.setOnClickListener(v2 ->
                NavHostFragment.findNavController(this).navigate(R.id.startUpFragment));

        return v;
    }
}
