package com.example.indeedgambling;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.lifecycle.ViewModelProvider;

public class SignupFragment extends Fragment {

    private FirebaseViewModel firebaseVM;

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

            if (n.isEmpty() || e.isEmpty() || ph.isEmpty() || p.isEmpty()) {
                Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String profileId = HashUtil.generateId(e, p); // email + password hash
            String passwordHash = HashUtil.sha256(p);

            Profile prof = new Profile(profileId, n, e, ph, r, passwordHash);

            firebaseVM.upsertProfile(prof,
                    () -> {
                        Toast.makeText(getContext(), "Account created", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this).navigate(R.id.startUpFragment);
                    },
                    err -> Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        back.setOnClickListener(v2 ->
                NavHostFragment.findNavController(this).navigate(R.id.startUpFragment));

        return v;
    }
}
