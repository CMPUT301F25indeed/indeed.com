package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Organizer_HomeFragment extends Fragment {

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.organization_home_fragment, container, false);

        //Logout button setup
        Button LogoutButton = view.findViewById(R.id.Organizer_HomeLogoutButton);
        LogoutButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_organizerHome_to_startup));

        // Setting up other navigation


        return view;
    }
}
