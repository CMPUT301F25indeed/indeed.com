package com.example.indeedgambling;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class StartUpFragment extends Fragment {

    public StartUpFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_up_fragment, container, false);

        Button loginBtn = view.findViewById(R.id.buttonLogin);
        Button signupBtn = view.findViewById(R.id.goToSignup);

        // go to login screen
        loginBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_startUp_to_login));

        // go to sign up screen
        signupBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_startUp_to_signup));

        return view;
    }
}
