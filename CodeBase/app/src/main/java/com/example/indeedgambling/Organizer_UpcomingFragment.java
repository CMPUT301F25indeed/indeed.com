package com.example.indeedgambling;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;


import android.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import android.util.Base64;
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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Fragment that displays the organizer's upcoming events.
 * <p>
 * Provides functionality for:
 * <ul>
 *     <li>Displaying a list of upcoming events</li>
 *     <li>Creating new events via a popup dialog</li>
 *     <li>Viewing and updating event details (location, poster, waitlist, invited list)</li>
 *     <li>Sending notifications to event entrants</li>
 * </ul>
 * <p>
 * Handles interaction with Firebase via {@link FirebaseViewModel} and
 * state management via {@link OrganizerViewModel}.
 */


public class Organizer_UpcomingFragment extends Fragment {

    private FirebaseViewModel Data;
    private OrganizerViewModel organizerVM;
    private String orgID;
    private View view;
    private ListView EventList;

    private static final int PICK_IMAGE_REQUEST = 999;
    private Uri selectedImageUri;
    private Event currentEventForUpdate;

    private Uri selectedPosterUri;

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
        RefreshUpcomingEventList();

        // Show popup when clicking an event in the list
        EventList.setOnItemClickListener((parent, itemView, position, id) -> {
            Event clickedEvent = (Event) parent.getItemAtPosition(position);
            showEventPopup(clickedEvent); // Back to original
        });


        //+New Event functionality.
        Button NewEvent = view.findViewById(R.id.Organizer_Upcoming_NewEventButton);
        NewEvent.setOnClickListener(v -> { showNewEventPopup();});

        return view;

    }


                        //---------------- POPUPS -------------//
    /**
     * Displays a popup dialog to create a new event.
     * <p>
     * Handles user input for event name, description, location, category, criteria,
     * maximum entrants, registration period, event runtime, and optional poster upload.
     * Ensures dates are valid and compresses large images before uploading.
     */

    private void showNewEventPopup(){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.make_event, null);

        Button uploadPosterButton = popupView.findViewById(R.id.NewEventPopup_UploadPosterButton);
        ImageView posterPreview = popupView.findViewById(R.id.NewEventPopup_PosterPreview);

        uploadPosterButton.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(pickIntent, "Select Event Poster"), PICK_IMAGE_REQUEST);
        });

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
        AlertDialog NewEvent = new AlertDialog.Builder(requireContext())
                .setTitle("New Event")
                .setView(popupView)
                .setPositiveButton("Confirm",null)
                .setNegativeButton("Cancel", null).show();

        //This allows not closing the dialog but refusing input
        NewEvent.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            //Using GregorianCalander to get date class, since Date from values is depreciated
            Date RegStartDate = new GregorianCalendar(RegStartDateInput.getYear(),RegStartDateInput.getMonth(),RegStartDateInput.getDayOfMonth(),RegStartTimeInput.getHour(),RegStartTimeInput.getMinute()).getTime();
            Date RegEndDate = new GregorianCalendar(RegEndDateInput.getYear(),RegEndDateInput.getMonth(),RegEndDateInput.getDayOfMonth(),RegEndTimeInput.getHour(),RegEndTimeInput.getMinute()).getTime();

            Date EventStartDate = new GregorianCalendar(EventStartDateInput.getYear(),EventStartDateInput.getMonth(), EventStartDateInput.getDayOfMonth(), EventStartTimeInput.getHour(), EventStartTimeInput.getMinute()).getTime();
            Date EventEndDate  = new GregorianCalendar(EventEndDateInput.getYear(),EventEndDateInput.getMonth(), EventEndDateInput.getDayOfMonth(), EventEndTimeInput.getHour(), EventEndTimeInput.getMinute()).getTime();

            //Refuse incorrect start-end date for event
            if (EventStartDate.after(EventEndDate)){
                WarningToast("Event Start cannot be BEFORE Event End!");
                return;
            }
            //Refuse incorrect start-end date for registration
            if (RegStartDate.after(RegEndDate)){
                WarningToast("Registration start cannot be BEFORE Registration End!");
                return;
            }




            //String inputs
            String EventName = NameInput.getText().toString().trim();
            String Description = DescriptionInput.getText().toString().trim();
            String Location = LocationInput.getText().toString().trim();
            String Category = CategoryInput.getText().toString().trim();
            String Criteria = CriteriaInput.getText().toString().trim();
            String MaxEnt = MaxEntrantsInput.getText().toString().trim();


            //Refuse empty title
            if (EventName.isEmpty()){
                WarningToast("Event Title cannot be empty!");
                return;
            }
            //Require description
            if (Description.isEmpty()){
                WarningToast("Description cannot be empty!");
                return;
            }
            //Require Location
            if (Location.isEmpty()){
                WarningToast("Must set a Location!");
                return;
            }
            //Require Category
            if (Category.isEmpty()){
                WarningToast("Event must have a Category!");
                return;
            }
            //Require Criteria
            if (Criteria.isEmpty()){
                WarningToast("Event must have a signup criteria!");
                return;
            }


            //Event(String EventName, Date RegistrationOpen, Date RegistrationClose, Date EventStart, Date EventEnd, String OrgID, String Description, String Critera, String Category, String QRCodeURL, int MaxEntrants) {
            //

            //US 02.01.04 : Optional for unlimited
            Event CreatedEvent = new Event(EventName,RegStartDate,RegEndDate,EventStartDate,EventEndDate,orgID,Description,Criteria,Category);
            CreatedEvent.setLocation(Location);

            //Optionals
            if (!MaxEnt.isBlank()){
                CreatedEvent.setMaxWaitingEntrants(Integer.parseInt(MaxEnt));
            }
            Data.Add(CreatedEvent);




            //Amrit
            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    // prevent giant posters
                    if (bitmap.getByteCount() > 2_000_000) { // only compress if very big
                        bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true);
                        Toast.makeText(requireContext(), "Poster compressed to fit upload size.", Toast.LENGTH_SHORT).show();
                    } else if (bitmap.getByteCount() > 800_000) { // moderate resize
                        bitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    String base64String = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                    // Create image data document
                    Map<String, Object> imageData = new HashMap<>();
                    imageData.put("eventId", CreatedEvent.getEventId());
                    imageData.put("uploaderId", orgID);
                    imageData.put("url", base64String);
                    imageData.put("uploadedAt", new Date());
                    imageData.put("approved", true);

                    // Store Base64 image inside /images collection
                    Data.getDb().collection("images")
                            .add(imageData)
                            .addOnSuccessListener(docRef -> {
                                String imageDocId = docRef.getId();

                                // Save imageDocId into event.imageUrl
                                Data.updateEvent(CreatedEvent.getEventId(),
                                        Map.of("imageUrl", imageDocId),
                                        () -> {
                                            CreatedEvent.setImageUrl(imageDocId);
                                            Toast.makeText(requireContext(), "Poster saved!", Toast.LENGTH_SHORT).show();
                                        },
                                        e -> Toast.makeText(requireContext(), "Failed to link poster!", Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Poster upload failed!", Toast.LENGTH_SHORT).show());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(requireContext(), "Image convert failed", Toast.LENGTH_SHORT).show();
                }
            }


            //Update the event list on the Upcoming screen.
            RefreshUpcomingEventList();

            //Close the popup
            NewEvent.dismiss();
        });


    }

    /** Popup to display created Event's information and do basic actions
     *
     */
    private void showEventPopup(Event event){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.organization_event_popup, null);

        //Setting UI References
        TextView Description = popupView.findViewById(R.id.Organizer_EventPopup_Description);
        TextView Criteria = popupView.findViewById(R.id.Organizer_EventPopup_Criteria);
        TextView Category = popupView.findViewById(R.id.Organizer_EventPopup_Category);
        TextView Location = popupView.findViewById(R.id.Organizer_EventPopup_EventLocation);


        ImageView QRCode = popupView.findViewById(R.id.Organizer_EventPopup_QR_Code);
        TextView RegPeriod = popupView.findViewById(R.id.Organizer_EventPopup_RegistrationPeriod);
        TextView RunTime = popupView.findViewById(R.id.Organizer_EventPopup_EventRuntime);
        TextView Capacity = popupView.findViewById(R.id.Organizer_EventPopup_Capacity);


        //Setting Button References
        Button updatePosterButton = popupView.findViewById(R.id.btnUpdatePoster);
        Button WaitListButton = popupView.findViewById(R.id.Organizer_EventPopup_WaitList);
        Button InviteListButton = popupView.findViewById(R.id.Organizer_EventPopup_InvList);
        Button notificationButton = popupView.findViewById(R.id.btnSendNotifications);
        Button viewPosterButton = popupView.findViewById(R.id.btnViewPoster);
        Button endRegButton = popupView.findViewById(R.id.Organizer_EventPopup_EndRegistrationNow);



        //Hiding the end registration button if it is not needed
        if (!event.RegistrationOpen()){
            endRegButton.setVisibility(View.GONE);
            ((ViewGroup) endRegButton.getParent()).removeView(endRegButton);
        }



                                            //--    Setting Display Texts -- //


        //Description
        Description.setText(event.getDescription());

        Criteria.setText("Event Criteria: ".concat(event.getCriteria()));

        Category.setText("Event Cateogry: ".concat(event.getCategory()));

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





                                                    //-- Button Interactions -- //


        //Waitlist Button Pop-up
        WaitListButton.setOnClickListener(v -> {
            WaitListPopup(event);
        });



        //Invited List Button Pop-up
        InviteListButton.setOnClickListener(v -> {
            Log.d("DEBUG","PRE BUILDPOPUP INVITEDLIST: ".concat(event.getInvitedList().toString()));
            View listView = inflater.inflate(R.layout.listview_popup, null);
            ListView InvitedList = listView.findViewById(R.id.popUp_Listview);
            //Putting on seperate Thread
            //Data.getEventInvitedList(event.getEventId(),p->{UpdateProfileList(p,InvitedList);},e -> {Log.d("DEBUG: Error", "Firebase Error".concat(e.toString()));});
            RefreshProfileList(InvitedList,event);

            //Actual popup
            Log.d("DEBUG","Building POPUP");
            new AlertDialog.Builder(requireContext())
                    .setTitle("Invited Entrants")
                    .setView(listView)
                    .setNegativeButton("Close", null)
                    .setPositiveButton("Export to CSV", ((dialog, which) -> {})).show();
        });

        //Show the Invite List Popup
        AlertDialog eventDialog = new AlertDialog.Builder(requireContext())
                .setTitle(event.getEventName())
                .setView(popupView)
                .setNegativeButton("Close", null)
                .show();



        //End Registration Now pop-up
        endRegButton.setOnClickListener(v -> {
            //Are you sure? popup

            new AlertDialog.Builder(requireContext())
                    .setTitle("Are you sure you want to end the registration period?")
                    .setMessage("Doing so will close the event")
                    .setPositiveButton("Yes", ((dialog, which) -> {
                        event.endRegistration(); //Update local

                        //Update Server
                        Map<String, Object> update = new HashMap<>();
                        update.put("registrationEnd",event.getRegistrationEnd());

                        //Close popup of event on success and update events
                        Data.updateEvent(event.getEventId(), update, ()->{
                            WarningToast("Registration for ".concat(event.getEventName()).concat(" ended"));

                            //Refresh page on success
                            RefreshUpcomingEventList();
                            },
                                e -> Log.d("Firebase Error", "Error pushing registration changes to server:".concat(e.toString())));

                        //Close event screen
                    }))
                    .setNegativeButton("Never mind",null)
                    .show();
        });





        //Event Poster Buttons

        if (updatePosterButton != null) {
            updatePosterButton.setOnClickListener(v -> {
                currentEventForUpdate = event;
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(pickIntent, "Select New Poster"), PICK_IMAGE_REQUEST);
            });
        }
        if (viewPosterButton != null) {
            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                viewPosterButton.setVisibility(View.VISIBLE);
                viewPosterButton.setOnClickListener(v -> {
                    String imageDocId = event.getImageUrl();

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    ImageView posterView = new ImageView(requireContext());
                    posterView.setAdjustViewBounds(true);
                    posterView.setImageResource(android.R.drawable.stat_sys_download);
                    builder.setView(posterView);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    // Fetch image from Firestore /images
                    Data.getDb().collection("images").document(imageDocId)
                            .addSnapshotListener((doc, err) -> {
                                if (!dialog.isShowing()) return;
                                if (err != null || doc == null || !doc.exists()) {
                                    Toast.makeText(requireContext(), "Poster not found!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    return;
                                }

                                String base64 = doc.getString("url");
                                if (base64 == null || base64.isEmpty()) {
                                    Toast.makeText(requireContext(), "Poster empty!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    return;
                                }

                                try {
                                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                    posterView.setImageBitmap(bitmap);
                                } catch (Exception e) {
                                    posterView.setImageResource(android.R.drawable.ic_menu_report_image);
                                    Toast.makeText(requireContext(), "Decode failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            } else {
                viewPosterButton.setVisibility(View.GONE);
            }
        }



        //Notification Pop-up, closes event pop-up
        notificationButton.setOnClickListener(v->{
            Log.d("DEBUG", "Notification button clicked!");
            //is button click working
            //Toast.makeText(requireContext(), "Opening notifications...", Toast.LENGTH_SHORT).show();
            organizerVM.setSelectedEvent(event);
            //close current dialog/popup
            eventDialog.dismiss();

            NavHostFragment.findNavController(Organizer_UpcomingFragment.this).navigate(R.id.notificationSenderFragment);

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                return;
            }

            //  Only handle updates here, not new-event uploads
            if (currentEventForUpdate != null) {
                try {
                    InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    if (bitmap.getByteCount() > 2_000_000) { // only compress if very big
                        bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true);
                        Toast.makeText(requireContext(), "Poster compressed to fit upload size.", Toast.LENGTH_SHORT).show();
                    } else if (bitmap.getByteCount() > 800_000) { // moderate resize
                        bitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    String base64String = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                    // Upload updated image to /images collection
                    Map<String, Object> imageData = new HashMap<>();
                    imageData.put("eventId", currentEventForUpdate.getEventId());
                    imageData.put("uploaderId", orgID);
                    imageData.put("url", base64String);
                    imageData.put("uploadedAt", new Date());
                    imageData.put("approved", true);

                    Data.getDb().collection("images")
                            .add(imageData)
                            .addOnSuccessListener(docRef -> {
                                String imageDocId = docRef.getId();

                                Data.updateEvent(currentEventForUpdate.getEventId(),
                                        Map.of("imageUrl", imageDocId),
                                        () -> {
                                            currentEventForUpdate.setImageUrl(imageDocId);
                                            Toast.makeText(requireContext(), "Poster updated!", Toast.LENGTH_SHORT).show();
                                        },
                                        e -> Toast.makeText(requireContext(), "Failed to link new poster!", Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Poster upload failed!", Toast.LENGTH_SHORT).show());

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Image convert failed", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }


    //US 02.02.01 && US 02.06.01
    private void WaitListPopup(Event event){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.organization_event_popup, null);
        Log.d("DEBUG","PRE BUILDPOPUP");
        View waitlistView = inflater.inflate(R.layout.organization_event_waitlist_popup, null);
        ListView List = waitlistView.findViewById(R.id.waitlistpopup_listview);

        //Putting on seperate Thread
        //Data.getEventWaitlist(event.getEventId(),p->{UpdateProfileList(p,List);},e -> {Log.d("DEBUG: Error", "Firebase Error".concat(e.toString()));});
        RefreshProfileList(List,event);

        //Inviting Entrants
        Button inviteEntrants = waitlistView.findViewById(R.id.waitlistpopup_inviteEntrants_Button);
        inviteEntrants.setOnClickListener(v1 -> {
            InviteNumberPopup(event, inflater, List);
        });

        //Waitlist Actual popup
        new AlertDialog.Builder(requireContext())
                .setTitle("Waitlist")
                .setView(waitlistView)
                .setNegativeButton("Close", null)
                .setPositiveButton("Export to CSV", ((dialog, which) -> {})).show();
    }

    /** POPUP that invites the entrants according to the number inputted by the user.
     * @param event Event whose waitlist and invitelist to affect
     * @param inflater current screen inflator
     * @param waitlistView The waitlist view we need to update after inviting entrants
     */
    private void InviteNumberPopup(Event event, LayoutInflater inflater, ListView waitlistView){
        View helperView = inflater.inflate(R.layout.text_input_helper,null);
        EditText numberInp = helperView.findViewById(R.id.EditText_helper);

        //Building integer input dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Number of entrants to invite (Up to ".concat(Integer.toString(event.getWaitingList().size())).concat(")"))
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

                    Data.updateEvent(event.getEventId(), update, ()->{RefreshWaitlist(event,waitlistView);}, e -> Log.d("Firebase Error", "Error pushing wait/invlist changes to server:".concat(e.toString())));

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


    private void RefreshProfileList(ListView ProfileList, Event event){
        new Thread(()->{
            Data.getEventWaitlist(event.getEventId(),p->{UpdateProfileList(p,ProfileList);},e -> {Log.d("DEBUG: Error", "Firebase Error".concat(e.toString()));});
        }).start();
    }

    /**
     * Refreshes the waitlist for a given event and updates the ListView.
     * Runs on a separate thread than the GUI
     * @param event Event whose waitlist will be refreshed.
     * @param waitlist ListView to update.
     */
    private void RefreshWaitlist(Event event, ListView waitlist){
        new Thread(()->{Data.getEventWaitlist(event.getEventId(),p->{UpdateProfileList(p,waitlist);},e -> {Log.d("DEBUG: Error", "Firebase Error".concat(e.toString()));});
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, event.getWaitingList());
            waitlist.setAdapter(adapter);}).start();

    }

    /** Refreshes the Upcoming Event list using a separate Thread
     * Uses the Current Org ID for the Upcoming Events
     */
    private void RefreshUpcomingEventList(){
        new Thread(()->{Data.fetchOrgsUpcomingEvents(orgID, this::UpdateEventList, e -> {
            Log.d("Debug", "onCreateView: Error with results".concat(e.toString()));
        });}).start();
    }

    /** EoA function that makes Toasts easier. Standard Toast popup helper
     * @param warning Message for Toast to display
     */
    private void WarningToast(String warning){
        Toast WarningToast = new Toast(requireContext());
        WarningToast.setText(warning);
        WarningToast.show();
    }
}