package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.*;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Date;

public class StartUpFragment extends Fragment {

    public StartUpFragment() {}

    //private FirebaseViewModel Data;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_up_fragment, container, false);

        Button loginBtn = view.findViewById(R.id.buttonLogin);
        Button signupBtn = view.findViewById(R.id.goToSignup);

        // go to login screen
        loginBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_startUpFragment_to_loginFragment));

        // go to sign up screen
        signupBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_startUpFragment_to_signupFragment));

        // Xan test data
        //Data.Add(new Profile("Tester","Tester"));
        //Data.Add(new Profile("Password","ProfileName"));

        return view;
    }
}
