package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class EventDetailsFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private Event event;
    private final String entrantId = "TEST_USER";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_event_details, container, false);


        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);


        TextView name = v.findViewById(R.id.event_name);
        TextView desc = v.findViewById(R.id.event_description);
        Button joinBtn = v.findViewById(R.id.join_waitlist_button);
        Button leaveBtn = v.findViewById(R.id.leave_waitlist_button);
        Button signUpBtn = v.findViewById(R.id.signup_button);
        Button backBtn = v.findViewById(R.id.back_button);


        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }


        if (event != null) {
            name.setText(event.getEventName());
            desc.setText(event.getDescription());
        }




        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });



        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseVM.joinWaitingList(event.getEventId(), entrantId,
                        () -> Toast.makeText(getContext(), "Joined waiting list!", Toast.LENGTH_SHORT).show(),
                        e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });


        leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseVM.leaveWaitingList(event.getEventId(), entrantId,
                        () -> Toast.makeText(getContext(), "Left waiting list!", Toast.LENGTH_SHORT).show(),
                        e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseVM.signUpForEvent(event.getEventId(), entrantId,
                        () -> Toast.makeText(getContext(), "Signed up successfully!", Toast.LENGTH_SHORT).show(),
                        e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        return v;
    }
}
