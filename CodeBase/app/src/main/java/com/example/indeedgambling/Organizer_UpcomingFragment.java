package com.example.indeedgambling;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.ListenerRegistration;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment that displays the organizer's upcoming events.
 *
 * Provides functionality for:
 *  - Displaying a list of upcoming events
 *  - Creating new events via a popup dialog
 *  - Viewing and updating event details (location, poster, waitlist, invited list)
 *  - Sending notifications to event entrants
 *
 * Handles interaction with Firebase via {@link FirebaseViewModel} and
 * state management via {@link OrganizerViewModel}.
 */
public class Organizer_UpcomingFragment extends Fragment {

    //Server info
    private FirebaseViewModel Data;
    private OrganizerViewModel organizerVM;
    private String orgID;

    // Root view + event list
    private View view;
    private ListView EventList;

    // Event list backing data + card adapter
    private final ArrayList<Event> EventArray = new ArrayList<>();
    private OrganizerEventCardAdapter eventAdapter;

    // Local storage for lists (kept in sync by snapshot listener)
    private final ArrayList<Profile> WaitingListArray = new ArrayList<>();
    private ArrayAdapter<Profile> WaitingListAdapter;

    private final ArrayList<Profile> invitedPeople = new ArrayList<>();
    private ArrayAdapter<Profile> inviteListAdapter;

    private final ArrayList<Profile> cancelledPeople = new ArrayList<>();
    private ArrayAdapter<Profile> cancelledListAdapter;

    private final ArrayList<Profile> acceptedPeople = new ArrayList<>();
    private ArrayAdapter<Profile> acceptedListAdapter;

    GeoPoint MapClickedPoint;

    //Other
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

        // --- profile list adapters ---
        WaitingListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, WaitingListArray);
        inviteListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, invitedPeople);
        cancelledListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, cancelledPeople);
        acceptedListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, acceptedPeople);

        // --- event card list ---
        EventList = view.findViewById(R.id.Organizer_UpcomingEventList);
        eventAdapter = new OrganizerEventCardAdapter(
                requireContext(),
                EventArray,
                Data
        );
        EventList.setAdapter(eventAdapter);

        // Pull data for upcoming events
        RefreshUpcomingEventList();



                    //Button Functionality



        //HomeButton Function
        Button Home = view.findViewById(R.id.Organizer_Upcoming_HomeButton);
        Home.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_organizerUpcomingFragment_to_organizerHomeFragment);
        });

        // Popup event on click
        EventList.setOnItemClickListener((parent, itemView, position, id) -> {
            Event clickedEvent = (Event) parent.getItemAtPosition(position);
            showEventPopup(clickedEvent);
        });

        // +New Event button functionality
        Button NewEvent = view.findViewById(R.id.Organizer_Upcoming_NewEventButton);
        NewEvent.setOnClickListener(v -> showNewEventPopup());

        return view;
    }

    // ---------------- POPUPS ----------------

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
        MapClickedPoint = null;


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
        MapView LocationSelector = popupView.findViewById(R.id.NewEventPopup_Location_Picker);
        CheckBox LocationRequirement = popupView.findViewById(R.id.NewEventPopup_GeoRequirement);
        EditText RequirementRadius = popupView.findViewById(R.id.NewEventPopup_GeoRequirement_Radius);
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


        //New Event Dialog
        AlertDialog NewEvent = new AlertDialog.Builder(requireContext())
                .setTitle("New Event")
                .setView(popupView)
                .setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", null)
                .show();

        //Map location chooser.
        //Set to user location. Nicety, not needed.
        LocationSelector.getController().setZoom(15.0);
        LocationSelector.getController().setCenter(new GeoPoint(51.05, -114.07)); // Default example

        Marker marker = new Marker(LocationSelector);
        marker.setTitle("Selected Location");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        LocationSelector.getOverlays().add(marker);


        LocationSelector.setOnTouchListener((v, event) -> {
            GeoPoint point = (GeoPoint) LocationSelector.getProjection().fromPixels(
                    (int) event.getX(),
                    (int) event.getY()
            );

            marker.setPosition(point);
            MapClickedPoint = point;
            LocationSelector.invalidate();
            return false;
        });


        LocationRequirement.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                RequirementRadius.setVisibility(View.VISIBLE);
            } else {
                RequirementRadius.setVisibility(View.GONE);
            }
        });




        //This allows not closing the dialog but refusing input
        NewEvent.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            //Using GregorianCalander to get date class, since Date from values is depreciated
            Date RegStartDate = new GregorianCalendar(RegStartDateInput.getYear(),RegStartDateInput.getMonth(),RegStartDateInput.getDayOfMonth(),RegStartTimeInput.getHour(),RegStartTimeInput.getMinute()).getTime();
            Date RegEndDate = new GregorianCalendar(RegEndDateInput.getYear(),RegEndDateInput.getMonth(),RegEndDateInput.getDayOfMonth(),RegEndTimeInput.getHour(),RegEndTimeInput.getMinute()).getTime();

            Date EventStartDate = new GregorianCalendar(EventStartDateInput.getYear(),EventStartDateInput.getMonth(), EventStartDateInput.getDayOfMonth(), EventStartTimeInput.getHour(), EventStartTimeInput.getMinute()).getTime();
            Date EventEndDate  = new GregorianCalendar(EventEndDateInput.getYear(),EventEndDateInput.getMonth(), EventEndDateInput.getDayOfMonth(), EventEndTimeInput.getHour(), EventEndTimeInput.getMinute()).getTime();

            //Checks that there is
            if (LocationRequirement.isChecked()) {
                if (RequirementRadius.getText().toString().isBlank()) {
                    WarningToast("A radius is required!");
                    return;
                }
            }
            if (MapClickedPoint == null){
                WarningToast("A Location is required!");
                return;
            }



            //String inputs
            String EventName = NameInput.getText().toString().trim();
            String Description = DescriptionInput.getText().toString().trim();
            String Category = CategoryInput.getText().toString().trim();
            String Criteria = CriteriaInput.getText().toString().trim();
            String MaxEnt = MaxEntrantsInput.getText().toString().trim();
            //Get Radius value if requirement toggled
            int Radius = 0;
            if (LocationRequirement.isChecked()) {
                Radius = Integer.parseInt(RequirementRadius.getText().toString().trim());
            }
            boolean RadiusRequirement = LocationRequirement.isChecked();



            //Refuse incorrect start-end date for event
            if (EventStartDate.after(EventEndDate)){
                WarningToast("Event Start cannot be BEFORE Event End!");
                return;
            }
            //Refuse incorrect start-end date for registration
            if (RegStartDate.after(RegEndDate)){
                WarningToast("Registration end cannot be BEFORE Registration start!");
                return;
            }

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


            //US 02.01.04 : Optional for unlimited
            Event CreatedEvent = new Event(EventName,RegStartDate,RegEndDate,EventStartDate,EventEndDate,orgID,Description,Criteria,Category);
            CreatedEvent.setLocation(MapClickedPoint.getLatitude(),MapClickedPoint.getLongitude());


            CreatedEvent.setregistrationRadiusEnabled(RadiusRequirement);
            CreatedEvent.setregisterableRadius(Radius);
            //Optionals
            if (!MaxEnt.isBlank()){
                CreatedEvent.setMaxWaitingEntrants(Integer.parseInt(MaxEnt));
            }

            Bitmap qrCodeBitmap = CreatedEvent.generateQRCode();
            if (qrCodeBitmap != null) {
                Log.d("QR_CODE", "QR code generated for new event: " + CreatedEvent.getEventName());
            }

            Data.Add(CreatedEvent);



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

    /** Popup to display created Event's information and do basic actions */
    private void showEventPopup(Event event) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.organization_event_popup, null);

        //Setting UI References
        TextView Description = popupView.findViewById(R.id.Organizer_EventPopup_Description);
        TextView Criteria = popupView.findViewById(R.id.Organizer_EventPopup_Criteria);
        TextView Category = popupView.findViewById(R.id.Organizer_EventPopup_Category);
        TextView LocationString = popupView.findViewById(R.id.Organizer_EventPopup_LocationText);
        TextView Radius = popupView.findViewById(R.id.Organizer_EventPopup_Radius);
        MapView LocationMap = popupView.findViewById(R.id.Organizer_EventPopup_LocationMap);


        ImageView QRCode = popupView.findViewById(R.id.Organizer_EventPopup_QR_Code);
        TextView RegPeriod = popupView.findViewById(R.id.Organizer_EventPopup_RegistrationPeriod);
        TextView RunTime = popupView.findViewById(R.id.Organizer_EventPopup_EventRuntime);
        TextView Capacity = popupView.findViewById(R.id.Organizer_EventPopup_Capacity);


        //Setting Button References
        Button updatePosterButton = popupView.findViewById(R.id.btnUpdatePoster);
        Button WaitListButton = popupView.findViewById(R.id.Organizer_EventPopup_WaitList);
        Button InviteListButton = popupView.findViewById(R.id.Organizer_EventPopup_InvList);
        Button CancelledListButton = popupView.findViewById(R.id.Organizer_EventPopup_CancelledList);
        Button AcceptedListButton = popupView.findViewById(R.id.Organizer_EventPopup_AcceptedList);
        Button notificationButton = popupView.findViewById(R.id.btnSendNotifications);
        Button viewPosterButton = popupView.findViewById(R.id.btnViewPoster);
        Button endRegButton = popupView.findViewById(R.id.Organizer_EventPopup_EndRegistrationNow);
        Button mapButton = popupView.findViewById(R.id.btnViewMap);
        Button exportFinalBtn = popupView.findViewById(R.id.btnExport);
        CheckBox locationRequirement = popupView.findViewById(R.id.Organizer_EventPopup_RadiusEnabled);

        locationRequirement.setChecked(event.isregistrationRadiusEnabled());


        //Hiding the end registration button if it is not needed
        if (!event.RegistrationOpen()){
            endRegButton.setVisibility(GONE);
            ViewGroup parent = (ViewGroup) endRegButton.getParent();
            if (parent != null) {
                parent.removeView(endRegButton);
            }
        }

                                            //--    Setting Display Texts -- //

        // --- set text fields ---
        Description.setText(event.getDescription());
        Criteria.setText(event.getCriteria());
        Category.setText(event.getCategory());

        //Registration Period: Mon Nov 03 11:11:00 MST 2025 - Tues Nov 04 12:00:00 MST 2025
        RegPeriod.setText(event.getRegistrationStart().toString().concat(" - ").concat(event.getRegistrationEnd().toString()));

        //RUNTIME
        RunTime.setText(event.getEventStart().toString().concat(" - ").concat(event.getEventEnd().toString()));

        //Setting Map pin and view
        LocationMap.getController().setCenter(new GeoPoint(event.getLatitude(),event.getLongitude()));
        LocationMap.getController().setZoom(15.0);

        //Setting the maker
        Marker m = new Marker(LocationMap);
        m.setPosition(new GeoPoint(event.getLatitude(),event.getLongitude()));
        m.setTitle("Event Location");
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        LocationMap.getOverlays().add(m);
        LocationMap.invalidate(); //Makes the map get redrawn to show maker.


        //Location
        LocationString.setText(event.getLocationString());


        Radius.setText(Double.toString(event.getregisterableRadius()).concat(" meters"));

        //Event Capacity: 12/40, 3/Unlimited, 0/30
        Capacity.setText((Integer.toString(event.getWaitingList().size() + event.getLostList().size())).concat("/".concat(event.getMaxWaitingEntrantsString())));




        //Pull data
        ListenerRegistration EventListener =
                Data.getDb()
                        .collection("events")
                        .document(event.getEventId())
                        .addSnapshotListener((docSnapshot, error) -> {
                            if (error != null || docSnapshot == null || !docSnapshot.exists()) {
                                return;
                            }

                            Event updatedEvent = docSnapshot.toObject(Event.class);
                            if (updatedEvent == null) return;

                            //Update local data
                            event.setWaitingList((ArrayList<String>) updatedEvent.getWaitingList());
                            event.setInvitedList((ArrayList<String>) updatedEvent.getInvitedList());
                            event.setLostList(updatedEvent.getLostList());
                            event.setAcceptedEntrants(updatedEvent.getAcceptedEntrants());
                            event.setCancelledEntrants(updatedEvent.getCancelledEntrants());
                            event.setregisterableRadius(updatedEvent.getregisterableRadius());
                            Radius.setText(Double.toString(event.getregisterableRadius()).concat(" meters"));
                            updateCapacityDisplay(Capacity,event);


                            //Waitlist Data
                            ArrayList<String> queryList = new ArrayList<>();
                            queryList.addAll(updatedEvent.getWaitingList());
                            queryList.addAll(updatedEvent.getLostList());
                            Data.getProfiles(queryList,
                                    (QueryResult)->{
                                        //Updating arrays
                                        WaitingListArray.clear();
                                        WaitingListArray.addAll(QueryResult);
                                        WaitingListAdapter.notifyDataSetChanged();
                                    },
                                    e->{Log.d("Firestore Error",e.toString());});

                            //inviteList Data
                            Data.getProfiles(updatedEvent.getInvitedList(),
                                    (InvitedList)->{
                                        //Updating array
                                        invitedPeople.clear();
                                        invitedPeople.addAll(InvitedList);
                                        inviteListAdapter.notifyDataSetChanged();
                                    },
                                    e->Log.d("Firestore Error",e.toString()));

                            //CancelledList Data
                            Data.getProfiles(updatedEvent.getCancelledEntrants(),
                                    (cancelledList)->{
                                        //Updating array
                                        cancelledPeople.clear();
                                        cancelledPeople.addAll(cancelledList);
                                        cancelledListAdapter.notifyDataSetChanged();
                                    },
                                    e -> Log.d("Firestore Error", e.toString()));

                            //AcceptedList Data
                            Data.getProfiles(updatedEvent.getAcceptedEntrants(),
                                    (acceptedList)->{
                                        //Updating array
                                        acceptedPeople.clear();
                                        acceptedPeople.addAll(acceptedList);
                                        acceptedListAdapter.notifyDataSetChanged();
                                    },
                                    e -> Log.d("Firestore Error", e.toString()));
                        });



        //-- Button Interactions -- //


        //Waitlist Button Pop-up
        WaitListButton.setOnClickListener(v -> {
            WaitListPopup(event);
        });

        //Invited List Button Pop-up
        InviteListButton.setOnClickListener(v -> {
            InviteListPopup();
        });

        //Cancelled List Button Pop-up
        CancelledListButton.setOnClickListener(v -> {
            CancelledListPopup(event);
        });

        //Accepted List Pop-up
        AcceptedListButton.setOnClickListener(v -> {
            AcceptedListPopup(event);
        });
        //Waitlist Geolocation Map Pop-up
        if (mapButton != null){
            mapButton.setOnClickListener(v -> showEntrantMap(event));
        }
        //Export ALL Entrants button
        if (exportFinalBtn != null){
            exportFinalBtn.setOnClickListener(v -> exportAllEnrolledEntrants(event));
        }
        //End Registration Now pop-up

        //Prompt setting a radius if there was not one before.
        locationRequirement.setOnClickListener(v -> {
            View radiusInput = inflater.inflate(R.layout.text_input_helper,null);
            EditText RadTextInput = radiusInput.findViewById(R.id.EditText_helper);
            if (locationRequirement.isChecked()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Set a Radius!")
                        .setView(radiusInput)
                        .setPositiveButton("Accept", (dialog, which) -> Radius.setText(RadTextInput.getText().toString().concat(" meters")))
                        .setNegativeButton("Cancel", ((dialog, which) -> {
                            //Reset checkbox if they refuse to put something that is non-zero.
                            if (event.getregisterableRadius() == 0) {
                                locationRequirement.setChecked(false);
                            }
                        }))
                        .show();
            }
            }
        );





        AlertDialog eventDialog = new AlertDialog.Builder(requireContext())
                .setTitle(event.getEventName())
                .setView(popupView)
                .setNegativeButton("Close", null)
                .setOnDismissListener(dialog -> {
                    EventListener.remove();

                    HashMap<String, Object> Update = new HashMap<String, Object>();
                    //Update the Radius on the server if it has changed.
                    if (!Radius.getText().toString().equals(Double.toString(event.getregisterableRadius()).concat(" meters"))) {
                        Update.put("registerableRadius", Double.parseDouble(Radius.getText().toString().replace(" meters","")));
                        event.setregisterableRadius(Double.parseDouble(Radius.getText().toString().replace(" meters","")));
                        //eventAdapter.notifyDataSetChanged();
                    }

                    //Update Checkbox to server if needed.
                    if (locationRequirement.isChecked() != event.isregistrationRadiusEnabled()) {
                        //Locale change
                        event.setregistrationRadiusEnabled(locationRequirement.isChecked());
                        //Push to server
                        Update.put("registrationRadiusEnabled", event.isregistrationRadiusEnabled());
                    }

                    if (!Update.isEmpty()) {
                        Data.updateEvent(event.getEventId(), Update, () -> {
                        }, e -> {
                            Log.d("Firebase Error", "Error Pushing to Server: ".concat(e.toString()));
                            //Put event back to normal if error occured on server.
                            event.setregistrationRadiusEnabled(locationRequirement.isChecked());
                        });
                    }
                })
                .show();

        //Event Poster Buttons

        if (updatePosterButton != null) {
            updatePosterButton.setOnClickListener(v -> {
                currentEventForUpdate = event;
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(pickIntent, "Select New Poster"), PICK_IMAGE_REQUEST);
            });
        }

        // View poster button
        if (viewPosterButton != null) {
            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                viewPosterButton.setVisibility(VISIBLE);
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
                viewPosterButton.setVisibility(GONE);
            }
        }

        // QR code
        Bitmap qrBitmap = event.generateQRCode();
        if (qrBitmap != null) {
            QRCode.setImageBitmap(qrBitmap);
        } else {
            QRCode.setImageResource(android.R.drawable.ic_dialog_alert);
            Toast.makeText(requireContext(), "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }

        //Notification Pop-up, closes event pop-up
        if (notificationButton != null){
        notificationButton.setOnClickListener(v->{
            Log.d("DEBUG", "Notification button clicked!");
            //is button click working
            //Toast.makeText(requireContext(), "Opening notifications...", Toast.LENGTH_SHORT).show();
            organizerVM.setSelectedEvent(event);
            //close current dialog/popup
            eventDialog.dismiss();

            NavHostFragment.findNavController(Organizer_UpcomingFragment.this).navigate(R.id.notificationSenderFragment);
        });}
    }

    // poster update result
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

                    String oldDocId = currentEventForUpdate.getImageUrl();

                    Map<String, Object> imageData = new HashMap<>();
                    imageData.put("eventId", currentEventForUpdate.getEventId());
                    imageData.put("uploaderId", orgID);
                    imageData.put("url", base64String);
                    imageData.put("uploadedAt", new Date());
                    imageData.put("approved", true);

                    // Upload new poster after deleting old one
                    Runnable uploadNewPoster = () -> {
                        Data.getDb().collection("images")
                                .add(imageData)
                                .addOnSuccessListener(docRef -> {
                                    String newDocId = docRef.getId();
                                    Data.updateEvent(
                                            currentEventForUpdate.getEventId(),
                                            Map.of("imageUrl", newDocId),
                                            () -> {
                                                currentEventForUpdate.setImageUrl(newDocId);
                                                Toast.makeText(requireContext(), "Poster updated!", Toast.LENGTH_SHORT).show();
                                            },
                                            e -> Toast.makeText(requireContext(), "Failed to link new poster!", Toast.LENGTH_SHORT).show()
                                    );
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Poster upload failed!", Toast.LENGTH_SHORT).show()
                                );
                    };

                    if (oldDocId != null && !oldDocId.isEmpty()) {
                        Data.getDb().collection("images").document(oldDocId)
                                .delete()
                                .addOnSuccessListener(unused -> uploadNewPoster.run())
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Old poster delete failed!", Toast.LENGTH_SHORT).show();
                                    uploadNewPoster.run(); // still upload new one
                                });
                    } else {
                        uploadNewPoster.run();
                    }

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

        Log.d("DEBUG","PRE BUILDPOPUP");
        View waitlistView = inflater.inflate(R.layout.organization_event_waitlist_popup, null);
        ListView WaitingList = waitlistView.findViewById(R.id.waitlistpopup_listview);

        WaitingList.setAdapter(WaitingListAdapter);

        //Inviting Entrants
        Button inviteEntrants = waitlistView.findViewById(R.id.waitlistpopup_inviteEntrants_Button);
        inviteEntrants.setOnClickListener(v1 -> {
            //Skip popup if there is nobody to invite
            if (event.getWaitingList().size() + event.getLostList().size() == 0){
                WarningToast("Nobody to invite!");
                return;
            }

            InviteNumberPopup(event, inflater);
        });

        //Waitlist Actual popup
        new AlertDialog.Builder(requireContext())
                .setTitle("Waitlist")
                .setView(waitlistView)
                .setNegativeButton("Close", null);
    }

    /** POPUP that displays all the entrants listed under the event's cancelled entrants. Uses local data.
     * US 02.06.02 As an organizer I want to see a list of all the cancelled entrants
     * @param event
     */
    private void CancelledListPopup(Event event){
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View popupView = inflater.inflate(R.layout.listview_popup, null);
        ListView cancelledListView = popupView.findViewById(R.id.popUp_Listview);

        cancelledListView.setAdapter(cancelledListAdapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelled Entrants")
                .setView(popupView)
                .setNegativeButton("Close", null)
                .setPositiveButton("Replace Entrants", (dialog, which) -> {replaceEntrants(event,inflater);
                //Refresh Data
                RefreshUpcomingEventList();})
                .show();
    }

    /** Invited entrants popup (uses local invitedPeople list) */
    private void InviteListPopup() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View popupView = inflater.inflate(R.layout.listview_popup, null);
        ListView inviteListView = popupView.findViewById(R.id.popUp_Listview);
        inviteListView.setAdapter(inviteListAdapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("Invited Entrants")
                .setView(popupView)
                .setNegativeButton("Close", null)
                .show();
    }

    /** Accepted entrants popup (uses local acceptedPeople list) */
    private void AcceptedListPopup(Event event) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View popupView = inflater.inflate(R.layout.listview_popup, null);
        ListView AcceptedListView = popupView.findViewById(R.id.popUp_Listview);
        AcceptedListView.setAdapter(acceptedListAdapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("Accepted Entrants")
                .setView(popupView)
                .setNegativeButton("Close", null)
                .setPositiveButton("Export to CSV", (dialog, which) -> {
                    exportAcceptedEntrantsList(event);
                })
                .show();
    }

    /** End registration popup */
    private void EndRegPopup(Event event) {
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
                                RefreshUpcomingEventList();
                            },
                            e -> Log.d("Firebase Error", "Error pushing registration changes to server:".concat(e.toString())));
                }))
                .setNegativeButton("Never mind",null)
                .show();
    }

    // -------------------- UPDATING EVENT LIST -------------------- //

    private void UpdateEventList(List<Event> eventsToShow) {
        EventArray.clear();
        EventArray.addAll(eventsToShow);
        //Notify the adapter
        eventAdapter.notifyDataSetChanged();
        Log.d("DEBUG Updated List", "Organizer Event List update ran");
    }


    /** Refreshes the Upcoming Event list using a separate Thread
     * Uses the Current Org ID for the Upcoming Events
     */
    private void RefreshUpcomingEventList(){
        new Thread(()->{Data.fetchOrgsUpcomingEvents(orgID, this::UpdateEventList, e -> {
            Log.d("Debug", "onCreateView: Error with results".concat(e.toString()));
        });}).start();
    }

    // -------------------- HELPERS -------------------- //
    private void showEntrantMap(Event event) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View mapView = inflater.inflate(R.layout.organizer_event_map_popup, null);


        org.osmdroid.views.MapView mapViewWidget = mapView.findViewById(R.id.mapView);
        TextView mapInfoText = mapView.findViewById(R.id.mapInfoText);


        // get all entrants
        ArrayList<String> allEntrants = new ArrayList<>();
        allEntrants.addAll(event.getWaitingList());
        allEntrants.addAll(event.getInvitedList());
        allEntrants.addAll(event.getAcceptedEntrants());


        if (allEntrants.isEmpty()) {
            WarningToast("No entrants to show on map");
            return;
        }


        // OSMdroid setup - no API
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapViewWidget.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);


        org.osmdroid.util.GeoPoint startPoint = new org.osmdroid.util.GeoPoint(51.0447, -114.0719);
        mapViewWidget.getController().setZoom(10.0);
        mapViewWidget.getController().setCenter(startPoint);


        Data.getProfiles(allEntrants,
                (profiles) -> {
                    // Add markers
                    for (Profile profile : profiles) {
                        double lat = 51.0447 + (Math.random() * 0.02 - 0.01);
                        double lon = -114.0719 + (Math.random() * 0.02 - 0.01);


                        org.osmdroid.views.overlay.Marker marker = new org.osmdroid.views.overlay.Marker(mapViewWidget);
                        marker.setPosition(new org.osmdroid.util.GeoPoint(lat, lon));
                        marker.setTitle(profile.getPersonName());
                        marker.setSnippet("Joined: " + event.getEventName());
                        mapViewWidget.getOverlays().add(marker);
                    }


                    mapInfoText.setText("Showing " + profiles.size() + " entrants");
                    mapViewWidget.invalidate();
                },
                e -> mapInfoText.setText("Error loading entrant data")
        );


        new AlertDialog.Builder(requireContext())
                .setTitle("Entrant Locations - " + event.getEventName())
                .setView(mapView)
                .setPositiveButton("Close", null)
                .show();
    }
    private void exportAllEnrolledEntrants(Event event) {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Exporting CSV")
                .setMessage("Preparing all enrolled entrants list...")
                .setCancelable(false)
                .show();


        // Get ALL enrolled entrants - waitlist + invited + accepted
        ArrayList<String> allEnrolledEntrants = new ArrayList<>();


        if (event.getWaitingList() != null) {
            allEnrolledEntrants.addAll(event.getWaitingList());
        }
        if (event.getInvitedList() != null) {
            allEnrolledEntrants.addAll(event.getInvitedList());
        }
        if (event.getAcceptedEntrants() != null) {
            allEnrolledEntrants.addAll(event.getAcceptedEntrants());
        }


        if (allEnrolledEntrants.isEmpty()) {
            loadingDialog.dismiss();
            WarningToast("No enrolled entrants to export!");
            return;
        }


        Data.getProfiles(allEnrolledEntrants,
                (profiles) -> {
                    loadingDialog.dismiss();


                    boolean success = CSVExporter.exportAllEnrolledEntrants(
                            requireContext(), event, profiles);


                    if (success) {
                        Toast.makeText(requireContext(),
                                "All enrolled entrants list exported to Downloads folder!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(),
                                "Failed to export CSV. Check storage permissions.",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Error fetching enrolled entrants data", Toast.LENGTH_SHORT).show();
                }
        );
    }
    /**
     * Exports ONLY accepted entrants to a CSV file
     * Shows loading dialog and handles empty list cases
     *
     * @param event The event to export accepted entrants from
     */
    private void exportAcceptedEntrantsList(Event event) {
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Exporting CSV")
                .setMessage("Preparing accepted entrants list...")
                .setCancelable(false)
                .show();


        // just accepted entrants
        ArrayList<String> acceptedEntrants = new ArrayList<>();


        if (event.getAcceptedEntrants() != null) {
            acceptedEntrants.addAll(event.getAcceptedEntrants());
        }


        if (acceptedEntrants.isEmpty()) {
            loadingDialog.dismiss();
            WarningToast("No accepted entrants to export!");
            return;
        }


        Data.getProfiles(acceptedEntrants,
                (profiles) -> {
                    loadingDialog.dismiss();


                    boolean success = CSVExporter.exportAcceptedEntrants(
                            requireContext(), event, profiles);


                    if (success) {
                        Toast.makeText(requireContext(),
                                "Accepted entrants list exported to Downloads folder!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(),
                                "Failed to export CSV.",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Error fetching accepted entrants data", Toast.LENGTH_SHORT).show();
                }
        );
    }


    /** POPUP that invites the entrants according to the number inputted by the user.
     * US 02.05.02 As an organizer I want to set the system to sample a specified number of attendees to register for the event.
     * @param event Event whose waitlist and invitelist to affect
     * @param inflater current screen inflator
     */
    private void InviteNumberPopup(Event event, LayoutInflater inflater){
        View helperView = inflater.inflate(R.layout.text_input_helper,null);
        EditText numberInp = helperView.findViewById(R.id.EditText_helper);

        new AlertDialog.Builder(requireContext())
                .setTitle("Number of entrants to invite (Up to ".concat(Integer.toString(event.getWaitingList().size() + event.getLostList().size())).concat(")"))
                .setView(helperView)
                .setPositiveButton("Confirm",((dialog, which) -> {
                    //Preventing non-numbers from being used
                    int number;
                    try {
                        number = Integer.parseInt(numberInp.getText().toString().trim());
                    } catch (Exception e) {
                        number = 0;
                    }

                    event.InviteEntrants(number);

                    Map<String, Object> update = new HashMap<>();
                    update.put("waitingList",event.getWaitingList());
                    update.put("lostList",event.getLostList());
                    update.put("invitedList",event.getInvitedList());

                    Data.updateEvent(event.getEventId(),
                            update,
                            ()->{},
                            e -> Log.d("Firebase Error", "Error pushing wait/invlist changes to server:".concat(e.toString())));

                }))
                .setNegativeButton("Cancel",null)
                .show();
    }



    /** Replaces the cancelled entrants with a number upto the cancelled number
     * US 02.05.03
     * @param event Event to shuffle entrants around in
     * @param inflater Display
     */
    private void replaceEntrants(Event event, LayoutInflater inflater){
        View helperView = inflater.inflate(R.layout.text_input_helper,null);
        EditText numberInp = helperView.findViewById(R.id.EditText_helper);

        int NumCancelled = event.getCancelledEntrants().size();
        int numWaiting = event.getLostList().size() + event.getWaitingList().size();
        if (numWaiting == 0){
            WarningToast("No waiting entrants to invite!");
            return;
        }
        if (NumCancelled == 0){
            WarningToast("There are no Cancelled Entrants to replace!");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Number of entrants to replace (Up to ".concat(Integer.toString(Math.min(NumCancelled,numWaiting))).concat(")"))
                .setView(helperView)
                .setPositiveButton("Confirm",((dialog, which) -> {
                    //Preventing non-numbers from being used
                    int number;
                    //If a non-int was passed, do nothing
                    try {
                        number = Integer.parseInt(numberInp.getText().toString().trim());
                        WarningToast("Please enter only digits!");
                    } catch (Exception e) {
                        number = 0;
                    }

                    //limit to cancelled entrant count
                    if (number > event.getCancelledEntrants().size()){
                        number = event.getCancelledEntrants().size();
                    }
                    //Send out invites
                    event.InviteEntrants(number);

                    Map<String, Object> update = new HashMap<>();
                    update.put("waitingList",event.getWaitingList());
                    update.put("lostList",event.getLostList());
                    update.put("invitedList",event.getInvitedList());

                    Data.updateEvent(event.getEventId(),
                            update,
                            ()->{},
                            e -> Log.d("Firebase Error", "Error pushing wait/invlist changes to server:".concat(e.toString())));

                }))
                .setNegativeButton("Cancel",null)
                .show();
    }


    /** EoA function that makes Toasts easier. Standard Toast popup helper
     * @param warning Message for Toast to display
     */
    private void WarningToast(String warning){
        Toast WarningToast = new Toast(requireContext());
        WarningToast.setText(warning);
        WarningToast.show();
    }

    private void updateCapacityDisplay(TextView Capacity, Event event){
        Capacity.setText((Integer.toString(event.getWaitingList().size()+event.getLostList().size())).concat("/".concat(event.getMaxWaitingEntrantsString())));
    }
}
