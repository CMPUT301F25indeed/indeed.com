package com.example.indeedgambling;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.api.ResourceDescriptor;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Organizer_UpcomingFragment extends Fragment {

    private FireBaseViewModel Data;

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.organization_upcomingevents_fragment, container, false);
        Data = new ViewModelProvider(requireActivity()).get(FireBaseViewModel.class);

        //HomeButton Function
        Button Home = view.findViewById(R.id.Organizer_Upcoming_HomeButton);
        Home.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_organizerUpcomingFragment_to_organizerHomeFragment);
        });


        //Displaying Organizer's events
        ListView EventList = view.findViewById(R.id.Organizer_UpcomingEventList);

        ArrayList<Event> DisplayEvents = Data.getEvents();

        ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, DisplayEvents);
        EventList.setAdapter(adapter);

        // Show popup when clicking an event in the list
        EventList.setOnItemClickListener((parent, itemView, position, id) -> {
            Event clickedEvent = (Event) parent.getItemAtPosition(position);
            showEventPopup(clickedEvent);
        });


        //+New Event functionality.
        Button NewEvent = view.findViewById(R.id.Organizer_Upcoming_NewEventButton);
        NewEvent.setOnClickListener(v -> { showNewEventPopup();});

        return view;

    }


    private void showNewEventPopup(){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.make_event, null);

        EditText NameInput = popupView.findViewById(R.id.NewEventPopup_NameDialog);

        View RegistrationOpen = popupView.findViewById(R.id.RegistrationOpen);
        View RegistrationClose = popupView.findViewById(R.id.RegistrationClose);
        View EventOpen = popupView.findViewById(R.id.EventOpen);
        View EventClose = popupView.findViewById(R.id.EventClose);

        //Registration DateTime
        DatePicker RegStartDateInput = RegistrationOpen.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker RegStartTimeInput = RegistrationOpen.findViewById(R.id.DateTimePicker_TimeDialog);
        DatePicker RegEndDateInput = RegistrationClose.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker RegEndTimeInput = RegistrationClose.findViewById(R.id.DateTimePicker_TimeDialog);

        //Event DateTime
        DatePicker EventStartDateInput = EventOpen.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker EventStartTimeInput = EventOpen.findViewById(R.id.DateTimePicker_TimeDialog);
        DatePicker EventEndDateInput = EventClose.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker EventEndTimeInput = EventClose.findViewById(R.id.DateTimePicker_TimeDialog);
        EditText MaxEntrantsInput = popupView.findViewById(R.id.NewEventPopup_MaxEntrantsDialog);

        new AlertDialog.Builder(requireContext())
                .setTitle("New Event")
                .setView(popupView)
                .setPositiveButton("Confirm",((dialog, which) -> {
                    String EventName = NameInput.getText().toString().trim();
                    //Using GregorianCalander to get date class, since Date from values is depreciated
                    Date RegStartDate = new GregorianCalendar(RegStartDateInput.getYear(),RegStartDateInput.getMonth(),RegStartDateInput.getDayOfMonth(),RegStartTimeInput.getHour(),RegStartTimeInput.getMinute()).getTime();
                    Date RegEndDate = new GregorianCalendar(RegEndDateInput.getYear(),RegEndDateInput.getMonth(),RegEndDateInput.getDayOfMonth(),RegEndTimeInput.getHour(),RegEndTimeInput.getMinute()).getTime();

                    Date EventStartDate = new GregorianCalendar(EventStartDateInput.getYear(),EventStartDateInput.getMonth(), EventStartDateInput.getDayOfMonth(), EventStartTimeInput.getHour(), EventStartTimeInput.getMinute()).getTime();
                    Date EventEndDate  = new GregorianCalendar(EventEndDateInput.getYear(),EventEndDateInput.getMonth(), EventEndDateInput.getDayOfMonth(), EventEndTimeInput.getHour(), EventEndTimeInput.getMinute()).getTime();


                    String MaxEntInp = MaxEntrantsInput.getText().toString().trim();

                    Log.d("DEBUG", "showNewEventPopup: " + EventName);
                    Log.d("DEBUG", "showNewEventPopup: " + RegStartDate);
                    Log.d("DEBUG", "showNewEventPopup: " + RegEndDate);
                    Log.d("DEBUG", "showNewEventPopup: " + EventStartDate);
                    Log.d("DEBUG", "showNewEventPopup: " + EventEndDate);

                    //If there is a specificied WaitList Limit
                    if (!MaxEntInp.isEmpty()) {
                        int MaxEntrants = Integer.parseInt(MaxEntrantsInput.getText().toString().trim());
                        Event CreatedEvent = new Event(EventName, RegStartDate, RegEndDate, EventStartDate, EventEndDate, new Organizer("billy","bob"), MaxEntrants);
                        Log.d("PopUp Test", "showNewEventPopup: " + CreatedEvent);
                        Log.d("PopUp Test", "showNewEventPopup Contains" + Data.Contains(CreatedEvent));
                        Data.Add(CreatedEvent);
                    } else {
                        Event CreatedEvent = new Event(EventName,RegStartDate, RegEndDate, EventStartDate, EventEndDate, new Organizer("billy","bob"));
                        Log.d("PopUp Test", "showNewEventPopup: " + CreatedEvent);
                        Log.d("PopUp Test", "showNewEventPopup Contains Result: " + Data.Contains(CreatedEvent));
                        Data.Add(CreatedEvent);
                    }

                    //Update Adapter on main page.
                    //Probably not the most efficient
                    ListView EventList = view.findViewById(R.id.Organizer_UpcomingEventList);
                    List<Event> DisplayEvents = Data.getEvents(); //Placeholder until profile handling added
                    ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, DisplayEvents);
                    EventList.setAdapter(adapter);


                }))
                .setNegativeButton("Cancel", null).show();
    }

    /** Popup to display created Event's information.
     *
     */
    private void showEventPopup(Event event){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.organization_event_popup, null);

        //Setting References
        ImageButton EventPoster = popupView.findViewById(R.id.Organizer_EventPopup_View_Event_Poster);
        ImageView QRCode = popupView.findViewById(R.id.Organizer_EventPopup_QR_Code);
        TextView RegPeriod = popupView.findViewById(R.id.Organizer_EventPopup_RegistrationPeriod);
        TextView RunTime = popupView.findViewById(R.id.Organizer_EventPopup_EventRuntime);
        TextView Location = popupView.findViewById(R.id.Organizer_EventPopup_EventLocation);
        TextView Capacity = popupView.findViewById(R.id.Organizer_EventPopup_Capacity);

        //Setting text to Events data

        //Registration Period
        RegPeriod.setText(event.getRegistrationStart().toString().concat("-").concat(event.getRegistrationEnd().toString()));

        Capacity.setText(Integer.toString(event.getMaxWaitingEntrants()));


        //WaitList Button
        Button WaitListButton = popupView.findViewById(R.id.Organizer_EventPopup_WaitList);
        Log.d("DEBUG","Before Listener");

        //Waitlist Pop-up
        WaitListButton.setOnClickListener(v -> {
            View listView = inflater.inflate(R.layout.listview_popup, null);
            ArrayAdapter<Entrant> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, event.getWaitingEntrants());

            new AlertDialog.Builder(requireContext())
                    .setTitle("Waitlist")
                    .setView(listView)
                    .setAdapter(adapter, null)
                    .setNegativeButton("Close", null)
                    .setPositiveButton("Export to CSV", ((dialog, which) -> {})).show();
        });


        new AlertDialog.Builder(requireContext()).setTitle(event.getEventName())
                .setView(popupView)
                .setNegativeButton("Close", ((dialog, which) -> {}))
                .show();

    }
}
