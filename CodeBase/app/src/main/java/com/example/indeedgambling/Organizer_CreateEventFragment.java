package com.example.indeedgambling;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.text.SimpleDateFormat;
import java.util.*;

public class Organizer_CreateEventFragment extends Fragment {

    private EditText title, desc, location, category, capacity;
    private Button regStartBtn, regEndBtn, startBtn, endBtn, createBtn;
    private FirebaseViewModel firebaseVM;
    private OrganizerViewModel organizerVM;

    private Date regStartDate, regEndDate, eventStartDate, eventEndDate;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public Organizer_CreateEventFragment(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.make_event, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);

        title = v.findViewById(R.id.NewEventPopup_NameDialog);
        desc = v.findViewById(R.id.NewEventPopup_Description);
        location = v.findViewById(R.id.NewEventPopup_Location);
        category = v.findViewById(R.id.NewEventPopup_Category);
        capacity = v.findViewById(R.id.NewEventPopup_MaxEntrantsDialog);

        regStartBtn = v.findViewById(R.id.RegistrationOpen);
        regEndBtn = v.findViewById(R.id.RegistrationClose);
        startBtn = v.findViewById(R.id.EventOpen);
        endBtn = v.findViewById(R.id.EventClose);
        createBtn = v.findViewById(R.id.event_create_btn);

        regStartBtn.setOnClickListener(v1 -> pickDateTime(d -> {
            regStartDate = d;
            regStartBtn.setText(format.format(d));
        }));

        regEndBtn.setOnClickListener(v1 -> pickDateTime(d -> {
            regEndDate = d;
            regEndBtn.setText(format.format(d));
        }));

        startBtn.setOnClickListener(v1 -> pickDateTime(d -> {
            eventStartDate = d;
            startBtn.setText(format.format(d));
        }));

        endBtn.setOnClickListener(v1 -> pickDateTime(d -> {
            eventEndDate = d;
            endBtn.setText(format.format(d));
        }));

        createBtn.setOnClickListener(v1 -> createEvent());

        return v;
    }

    private void pickDateTime(java.util.function.Consumer<Date> onPick) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(getContext(), (v, y, m, d) -> {
            TimePickerDialog tp = new TimePickerDialog(getContext(), (vv, hh, mm) -> {
                Calendar picked = Calendar.getInstance();
                picked.set(y, m, d, hh, mm);
                onPick.accept(picked.getTime());
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
            tp.show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void createEvent() {
        String t = title.getText().toString().trim();
        String des = desc.getText().toString().trim();
        String loc = location.getText().toString().trim();
        String cat = category.getText().toString().trim();
        String capTxt = capacity.getText().toString().trim();

        if (TextUtils.isEmpty(t) || regStartDate == null || regEndDate == null ||
                eventStartDate == null || eventEndDate == null) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int cap = TextUtils.isEmpty(capTxt) ? 0 : Integer.parseInt(capTxt);

        Profile organizer = organizerVM.getOrganizer().getValue();
        if (organizer == null) {
            Toast.makeText(getContext(), "Organizer missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Event e = new Event(
                t, des, organizer.getProfileId(), cat, loc,
                eventStartDate, eventEndDate,
                regStartDate, regEndDate,
                cap
        );

        e.setEventId(UUID.randomUUID().toString());

        firebaseVM.createEvent(e,
                () -> {
                    Toast.makeText(getContext(), "Event created!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.organizerHomeFragment);
                },
                err -> Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
