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
import android.widget.ListView;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.api.ResourceDescriptor;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Organizer_UpcomingFragment extends Fragment {

    private EventHandler EH;
    private List<Event> DisplayEvents;
    private ListView EventList;

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.organization_upcomingevents_fragment, container, false);
        EH = new EventHandler();
        EH.SyncWithFireBase();

        //HomeButton Function
        Button Home = view.findViewById(R.id.Organizer_Upcoming_HomeButton);
        Home.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_organizerUpcomingFragment_to_organizerHomeFragment);
        });


        EH.AddEvent(new Event("test1",new Date(), new Date(),new Organizer("diggle","piggle")));

        //Displaying Organizer's events
        ListView EventList = view.findViewById(R.id.Organizer_UpcomingEventList);
        ArrayList<Event> DisplayEvents = EH.GetEvents(); //Placeholder until profile handling added
        ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, DisplayEvents);
        EventList.setAdapter(adapter);



        //+New Event functionality.
        Button NewEvent = view.findViewById(R.id.Organizer_Upcoming_NewEventButton);
        NewEvent.setOnClickListener(v -> { showNewEventPopup();});
        return view;
    }


    private void showNewEventPopup(){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.make_event, null);

        EditText NameInput = popupView.findViewById(R.id.NewEventPopup_NameDialog);
        DatePicker StartDateInput = popupView.findViewById(R.id.NewEventPopup_StartDateDialog);
        TimePicker StartTimeInput = popupView.findViewById(R.id.NewEventPopup_StartTimeDialog);
        DatePicker EndDateInput = popupView.findViewById(R.id.NewEventPopup_EndDateDialog);
        TimePicker EndTimeInput = popupView.findViewById(R.id.NewEventPopup_EndTimeDialog);
        EditText MaxEntrantsInput = popupView.findViewById(R.id.NewEventPopup_MaxEntrantsDialog);




        new AlertDialog.Builder(requireContext())
                .setTitle("New Event")
                .setView(popupView)
                .setPositiveButton("Confirm",((dialog, which) -> {
                    String EventName = NameInput.getText().toString().trim();
                    //Using GregorianCalander to get date class, since Date from values is depreciated
                    Date StartDate = new GregorianCalendar(StartDateInput.getYear(),StartDateInput.getMonth(),StartDateInput.getDayOfMonth(),StartTimeInput.getHour(),StartTimeInput.getMinute()).getTime();
                    Date EndDate = new GregorianCalendar(EndDateInput.getYear(),EndDateInput.getMonth(),EndDateInput.getDayOfMonth(),EndTimeInput.getHour(),EndTimeInput.getMinute()).getTime();

                    String MaxEntInp = MaxEntrantsInput.getText().toString().trim();

                    if (!MaxEntInp.isEmpty()) {
                        int MaxEntrants = Integer.parseInt(MaxEntrantsInput.getText().toString().trim());
                        Event CreatedEvent = new Event(EventName,StartDate,EndDate, new Organizer("billy","bob"), MaxEntrants);
                        Log.d("PopUp Test", "showNewEventPopup: " + CreatedEvent);
                        EH.AddEvent(CreatedEvent);
                    } else {
                        Event CreatedEvent = new Event(EventName,StartDate,EndDate, new Organizer("billy","bob"));
                        Log.d("PopUp Test", "showNewEventPopup: " + CreatedEvent);
                        EH.AddEvent(CreatedEvent);
                        EH.SyncWithFireBase();
                    }

                    //Update Adapter on main page.
                    //Probably not the most efficent
                    ListView EventList = view.findViewById(R.id.Organizer_UpcomingEventList);
                    List<Event> DisplayEvents = EH.GetEvents(); //Placeholder until profile handling added
                    ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, DisplayEvents);
                    EventList.setAdapter(adapter);


                }))
                .setNegativeButton("Cancel", null).show();
    }
}
