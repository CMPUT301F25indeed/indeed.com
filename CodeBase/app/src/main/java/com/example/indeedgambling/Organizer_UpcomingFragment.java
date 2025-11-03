package com.example.indeedgambling;

import android.app.AlertDialog;
import android.os.Bundle;
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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Organizer_UpcomingFragment extends Fragment {

    private FirebaseViewModel Data;
    private OrganizerViewModel organizerVM;

    private String orgID;
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.organization_upcomingevents_fragment, container, false);
        Data = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        orgID = organizerVM.getOrganizer().getValue().getProfileId();


        //HomeButton Function
        Button Home = view.findViewById(R.id.Organizer_Upcoming_HomeButton);
        Home.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_organizerUpcomingFragment_to_organizerHomeFragment);
        });

        //Displaying Organizer's events
        ListView EventList = view.findViewById(R.id.Organizer_UpcomingEventList);
        Data.fetchOrgsEvents(orgID,events -> {UpdateEventList(events);}, e -> {
            Log.d("Debug", "onCreateView: Error with results".concat(e.toString()));
        });

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
        EditText MaxEntrantsInput = popupView.findViewById(R.id.NewEventPopup_MaxEntrantsDialog);
        EditText DescriptionInput = popupView.findViewById(R.id.NewEventPopup_Description);
        EditText CategoryInput = popupView.findViewById(R.id.NewEventPopup_Category);
        EditText LocationInput = popupView.findViewById(R.id.NewEventPopup_Location);
        EditText CriteriaInput = popupView.findViewById(R.id.NewEventPopup_Criteria);


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


                    String MaxEnt = MaxEntrantsInput.getText().toString().trim();
                    String Location = LocationInput.getText().toString().trim();
                    String Description = DescriptionInput.getText().toString().trim();
                    String Category = CategoryInput.getText().toString().trim();
                    String Criteria = CriteriaInput.getText().toString().trim();


                    Event CreatedEvent = new Event(EventName,RegStartDate,RegEndDate,EventStartDate,EventEndDate,organizerVM.getOrganizer().getValue().getProfileId(),Description,Category,Criteria);
                    //Optionals
                    if (!Location.isEmpty()){
                        CreatedEvent.setLocation(Location);
                    }
                    if (!MaxEnt.isBlank()){
                        CreatedEvent.setMaxEntrants(Integer.parseInt(MaxEnt));
                    }
                    Data.Add(CreatedEvent);


                    //Update Adapter on main page.

                    //Displaying Organizer's events
                    ListView EventList = view.findViewById(R.id.Organizer_UpcomingEventList);
                    Data.fetchOrgsEvents(orgID,events -> {UpdateEventList(events);}, e -> {
                        Log.d("Debug", "onCreateView: Error with results".concat(e.toString()));
                    });


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

        //Setting pop-up to event data


        //TODO: EVENTPOSTER
        //TODO:QR CODE


        //Registration Period: Mon Nov 03 11:11:00 MST 2025 - Tues Nov 04 12:00:00 MST 2025
        RegPeriod.setText("Registration Period: ".concat(event.getRegistrationStart().toString().concat(" - ").concat(event.getRegistrationEnd().toString())));


        //RUNTIME
        RunTime.setText("Event Runtime: ".concat(event.getEventStart().toString()).concat(" - ").concat(event.getEventEnd().toString()));


        //Location
        if (event.hasLocation()){
            Location.setText("Location: ".concat(event.getLocation()));
        }

        //Event Capacity: 12/40, 12/Unlimited
        Capacity.setText("Event Capacity: ".concat(Integer.toString(event.getWaitingList().size())).concat("/".concat(event.getMaxWaitingEntrantsString())));


        //WaitList Button
        Button WaitListButton = popupView.findViewById(R.id.Organizer_EventPopup_WaitList);
        //Log.d("DEBUG","Before Listener");

        //Waitlist Pop-up
        WaitListButton.setOnClickListener(v -> {

            Log.d("DEBUG","PRE BUILDPOPUP");
            View listView = inflater.inflate(R.layout.listview_popup, null);
            Data.getEventWaitlist(event.getEventId(),p->{UpdateProfileList(p,listView.findViewById(R.id.popUp_Listview));},e -> {Log.d("DEBUG: Error", "Firebase Error".concat(e.toString()));});
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, event.getWaitingEntrantIDs());

            Log.d("DEBUG","Building POPUP");
            new AlertDialog.Builder(requireContext())
                    .setTitle("Waitlist")
                    .setView(listView)
                    .setAdapter(adapter, null)
                    .setNegativeButton("Close", null)
                    .setPositiveButton("Export to CSV", ((dialog, which) -> {})).show();
        });

        //Invited List Button
        Button InviteListButton = popupView.findViewById(R.id.Organizer_EventPopup_InvList);

        //Invited List Pop-up
        InviteListButton.setOnClickListener(v -> {
            Log.d("DEBUG","PRE BUILDPOPUP");


            View listView = inflater.inflate(R.layout.listview_popup, null);
            Data.getEventInvitedList(event.getEventId(),p->{UpdateProfileList(p,listView.findViewById(R.id.popUp_Listview));},e -> {Log.d("DEBUG: Error", "Firebase Error".concat(e.toString()));});
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);

            Log.d("DEBUG","Building POPUP");
            new AlertDialog.Builder(requireContext())
                    .setTitle("Invited Entrants")
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

    private void UpdateEventList(List<Event> eventsToShow){
        ListView EventList = view.findViewById(R.id.Organizer_UpcomingEventList);
        ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, eventsToShow);
        EventList.setAdapter(adapter);
        Log.d("DEBUG Updated List", "Organizer Event List update ran");
    }

    private void UpdateProfileList(List<Profile> itemsToShow, ListView EventList){
        ArrayAdapter<Profile> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, itemsToShow);
        EventList.setAdapter(adapter);
        Log.d("DEBUG Updated List", "Organizer Profile List update ran");
    }
}
