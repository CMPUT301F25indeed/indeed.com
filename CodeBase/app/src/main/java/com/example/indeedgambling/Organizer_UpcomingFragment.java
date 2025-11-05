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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Organizer_UpcomingFragment extends Fragment {

    private FirebaseViewModel Data;
    private OrganizerViewModel organizerVM;
    private String orgID;
    private View view;
    private ListView EventList;

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
        EventList = view.findViewById(R.id.Organizer_UpcomingEventList);
        Data.fetchOrgsUpcomingEvents(orgID,events -> {UpdateEventList(events);}, e -> {
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


    //---------------- POPUPS -------------//

    private void showNewEventPopup(){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.make_event, null);

        EditText NameInput = popupView.findViewById(R.id.NewEventPopup_NameDialog);
        //US 02.01.04
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

        //Preventing making events in the past
        Long CurrentTime = new Date().getTime();
        EventStartDateInput.setMinDate(CurrentTime);
        EventEndDateInput.setMinDate(CurrentTime);

        //Prevent Registration in the past
        RegEndDateInput.setMinDate(CurrentTime);
        RegStartDateInput.setMinDate(CurrentTime);


        //New Event Making Dialog
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

                    //US 02.01.04 : Optional for unlimited

                    Event CreatedEvent = new Event(EventName,RegStartDate,RegEndDate,EventStartDate,EventEndDate,orgID,Description,Category,Criteria);
                    //Optionals
                    if (!Location.isEmpty()){
                        CreatedEvent.setLocation(Location);
                    }
                    if (!MaxEnt.isBlank()){
                        CreatedEvent.setMaxWaitingEntrants(Integer.parseInt(MaxEnt));
                    }
                    Data.Add(CreatedEvent);

                    //Displaying Organizer's events
                    Data.fetchOrgsUpcomingEvents(orgID, this::UpdateEventList, e -> {
                        Log.d("FIREBASE Error", "onCreateView: Error with Event results".concat(e.toString()));
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
        TextView Description = popupView.findViewById(R.id.Organizer_EventPopup_Description);
        ImageButton EventPoster = popupView.findViewById(R.id.Organizer_EventPopup_View_Event_Poster);
        ImageView QRCode = popupView.findViewById(R.id.Organizer_EventPopup_QR_Code);
        TextView RegPeriod = popupView.findViewById(R.id.Organizer_EventPopup_RegistrationPeriod);
        TextView RunTime = popupView.findViewById(R.id.Organizer_EventPopup_EventRuntime);
        TextView Location = popupView.findViewById(R.id.Organizer_EventPopup_EventLocation);
        TextView Capacity = popupView.findViewById(R.id.Organizer_EventPopup_Capacity);

        //Setting pop-up to event data

        Log.d("DEBUG ERROR", event.getDescription());
        Description.setText(event.getDescription());



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

        //Event Capacity: 12/40, 3/Unlimited, 0/30
        Capacity.setText("Waitlist Capacity: ".concat(Integer.toString(event.getWaitingList().size())).concat("/".concat(event.getMaxWaitingEntrantsString())));


        //WaitList Button
        Button WaitListButton = popupView.findViewById(R.id.Organizer_EventPopup_WaitList);
        //Log.d("DEBUG","Before Listener");

        //Waitlist Pop-up
        WaitListButton.setOnClickListener(v -> {
            WaitListPopup(event);
        });

        //Invited List Button
        Button InviteListButton = popupView.findViewById(R.id.Organizer_EventPopup_InvList);

        //Invited List Pop-up
        InviteListButton.setOnClickListener(v -> {
            Log.d("DEBUG","PRE BUILDPOPUP INVITEDLIST: ".concat(event.getInvitedList().toString()));
            View listView = inflater.inflate(R.layout.listview_popup, null);
            ListView InvitedList = listView.findViewById(R.id.popUp_Listview);
            Data.getEventInvitedList(event.getEventId(),p->{UpdateProfileList(p,InvitedList);},e -> {Log.d("DEBUG: Error", "Firebase Error".concat(e.toString()));});
            //ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);

            //ACtual popup
            Log.d("DEBUG","Building POPUP");
            new AlertDialog.Builder(requireContext())
                    .setTitle("Invited Entrants")
                    .setView(listView)
                    .setNegativeButton("Close", null)
                    .setPositiveButton("Export to CSV", ((dialog, which) -> {})).show();
        });

        new AlertDialog.Builder(requireContext()).setTitle(event.getEventName())
                .setView(popupView)
                .setNegativeButton("Close", ((dialog, which) -> {}))
                .show();
    }

    //US 02.02.01 && US 02.06.01
    private void WaitListPopup(Event event){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.organization_event_popup, null);
        Log.d("DEBUG","PRE BUILDPOPUP");
        View waitlistView = inflater.inflate(R.layout.organization_event_waitlist_popup, null);

        Data.getEventWaitlist(event.getEventId(),p->{UpdateProfileList(p,waitlistView.findViewById(R.id.waitlistpopup_listview));},e -> {Log.d("DEBUG: Error", "Firebase Error".concat(e.toString()));});

        //Inviting Entrants
        Button inviteEntrants = waitlistView.findViewById(R.id.waitlistpopup_inviteEntrants_Button);
        inviteEntrants.setOnClickListener(v1 -> {
            InviteNumberPopup(event, inflater);
        });

        //Waitlist Actual popup
        new AlertDialog.Builder(requireContext())
                .setTitle("Waitlist")
                .setView(waitlistView)
                .setNegativeButton("Close", null)
                .setPositiveButton("Export to CSV", ((dialog, which) -> {})).show();
    }

    private void InviteNumberPopup(Event event, LayoutInflater inflater){
        View helperView = inflater.inflate(R.layout.text_input_helper,null);
        EditText numberInp = helperView.findViewById(R.id.EditText_helper);
        //Building integer input dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Number of entrants to invite (Up to ".concat(event.getMaxWaitingEntrantsString()).concat(")"))
                .setView(helperView)
                .setPositiveButton("Confirm",((dialog, which) -> {
                    //Preventing non-numbers from being used
                    int number;
                    try {
                        number = Integer.parseInt(numberInp.getText().toString().trim());
                    //If a non-int was passed, do nothing
                    } catch (Exception e) {
                        number = 0;
                        //throw new RuntimeException(e);
                    }


                    //Send out invites
                    event.InviteEntrants(number);

                    Map<String, Object> update = new HashMap<>();
                    update.put("waitingList",event.getWaitingList());
                    update.put("invitedList",event.getInvitedList());

                    Data.updateEvent(event.getEventId(), update, ()->{RefreshWaitlist(event);}, e -> Log.d("Firebase Error", "Error pushing wait/invlist changes to server:".concat(e.toString())));

                }))
                .setNegativeButton("Cancel",null)
                .show();
    }


    // -------------------- UPDATING LISTVIEWS -------------//

    private void UpdateEventList(List<Event> eventsToShow){
        ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, eventsToShow);
        EventList.setAdapter(adapter);
        Log.d("DEBUG Updated List", "Organizer Event List update ran");
    }

    /** Updates the passed ListView with the array passed. Sets the adapter
     *
     * @param itemsToShow
     * @param EventList
     */
    private void UpdateProfileList(List<Profile> itemsToShow, ListView EventList){
        ArrayAdapter<Profile> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, itemsToShow);
        EventList.setAdapter(adapter);
    }

    /** TODO: FIX
     *
     * @param event
     */
    private void RefreshWaitlist(Event event){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View waitlistView = inflater.inflate(R.layout.organization_event_waitlist_popup, null);


        Data.getEventWaitlist(event.getEventId(),p->{UpdateProfileList(p,waitlistView.findViewById(R.id.waitlistpopup_listview));},e -> {Log.d("DEBUG: Error", "Firebase Error".concat(e.toString()));});
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, event.getWaitingList());
        ListView waitlist = waitlistView.findViewById(R.id.waitlistpopup_listview);
        waitlist.setAdapter(adapter);
    }
}
