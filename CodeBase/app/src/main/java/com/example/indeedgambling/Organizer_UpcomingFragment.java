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

    private FirebaseViewModel Data;
    private OrganizerViewModel organizerVM;
    private String orgID;
    private View view;
    private ListView EventList;

    private static final int PICK_IMAGE_REQUEST = 999;
    private Uri selectedImageUri;
    private Event currentEventForUpdate;

    private Uri selectedPosterUri;

    // ---- new: card adapter + backing list ----
    private OrganizerEventCardAdapter eventAdapter;
    private final List<Event> upcomingEvents = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.organization_upcomingevents_fragment, container, false);
        Data = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        orgID = organizerVM.getOrganizer().getValue().getProfileId();

        // HomeButton Function
        Button Home = view.findViewById(R.id.Organizer_Upcoming_HomeButton);
        Home.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_organizerUpcomingFragment_to_organizerHomeFragment);
        });

        // Displaying Organizer's events
        EventList = view.findViewById(R.id.Organizer_UpcomingEventList);

        // use card-style adapter (same style as admin)
        eventAdapter = new OrganizerEventCardAdapter(
                requireContext(),
                new ArrayList<>(),
                Data
        );
        EventList.setAdapter(eventAdapter);

        Data.fetchOrgsUpcomingEvents(orgID, events -> {
            UpdateEventList(events);
        }, e -> {
            Log.d("Debug", "onCreateView: Error with results".concat(e.toString()));
        });

        // Show popup when clicking an event in the list
        EventList.setOnItemClickListener((parent, itemView, position, id) -> {
            Event clickedEvent = (Event) parent.getItemAtPosition(position);
            showEventPopup(clickedEvent);
        });

        // +New Event functionality.
        Button NewEvent = view.findViewById(R.id.Organizer_Upcoming_NewEventButton);
        NewEvent.setOnClickListener(v -> { showNewEventPopup(); });

        return view;
    }

    //---------------- POPUPS -------------//
    /**
     * Displays a popup dialog to create a new event.
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

        // Registration DateTime
        DatePicker RegStartDateInput = RegistrationOpen.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker RegStartTimeInput = RegistrationOpen.findViewById(R.id.DateTimePicker_TimeDialog);
        DatePicker RegEndDateInput = RegistrationClose.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker RegEndTimeInput = RegistrationClose.findViewById(R.id.DateTimePicker_TimeDialog);

        // Event DateTime
        DatePicker EventStartDateInput = EventOpen.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker EventStartTimeInput = EventOpen.findViewById(R.id.DateTimePicker_TimeDialog);
        DatePicker EventEndDateInput = EventClose.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker EventEndTimeInput = EventClose.findViewById(R.id.DateTimePicker_TimeDialog);

        // Preventing making events in the past
        Long CurrentTime = new Date().getTime();
        EventStartDateInput.setMinDate(CurrentTime);
        EventEndDateInput.setMinDate(CurrentTime);

        // Prevent Registration in the past
        RegEndDateInput.setMinDate(CurrentTime);
        RegStartDateInput.setMinDate(CurrentTime);

        // New Event Making Dialog
        AlertDialog NewEvent = new AlertDialog.Builder(requireContext())
                .setTitle("New Event")
                .setView(popupView)
                .setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", null).show();

        // This allows not closing the dialog but refusing input
        NewEvent.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Date RegStartDate = new GregorianCalendar(
                    RegStartDateInput.getYear(),
                    RegStartDateInput.getMonth(),
                    RegStartDateInput.getDayOfMonth(),
                    RegStartTimeInput.getHour(),
                    RegStartTimeInput.getMinute()
            ).getTime();

            Date RegEndDate = new GregorianCalendar(
                    RegEndDateInput.getYear(),
                    RegEndDateInput.getMonth(),
                    RegEndDateInput.getDayOfMonth(),
                    RegEndTimeInput.getHour(),
                    RegEndTimeInput.getMinute()
            ).getTime();

            Date EventStartDate = new GregorianCalendar(
                    EventStartDateInput.getYear(),
                    EventStartDateInput.getMonth(),
                    EventStartDateInput.getDayOfMonth(),
                    EventStartTimeInput.getHour(),
                    EventStartTimeInput.getMinute()
            ).getTime();

            Date EventEndDate = new GregorianCalendar(
                    EventEndDateInput.getYear(),
                    EventEndDateInput.getMonth(),
                    EventEndDateInput.getDayOfMonth(),
                    EventEndTimeInput.getHour(),
                    EventEndTimeInput.getMinute()
            ).getTime();

            // Refuse incorrect start-end date for event
            if (EventStartDate.after(EventEndDate)){
                WarningToast("Event Start cannot be BEFORE Event End!");
                return;
            }
            // Refuse incorrect start-end date for registration
            if (RegStartDate.after(RegEndDate)){
                WarningToast("Registration start cannot be BEFORE Registration End!");
                return;
            }

            // String inputs
            String EventName = NameInput.getText().toString().trim();
            String Description = DescriptionInput.getText().toString().trim();
            String Location = LocationInput.getText().toString().trim();
            String Category = CategoryInput.getText().toString().trim();
            String Criteria = CriteriaInput.getText().toString().trim();
            String MaxEnt = MaxEntrantsInput.getText().toString().trim();

            // Validate
            if (EventName.isEmpty()){
                WarningToast("Event Title cannot be empty!");
                return;
            }
            if (Description.isEmpty()){
                WarningToast("Description cannot be empty!");
                return;
            }
            if (Location.isEmpty()){
                WarningToast("Must set a Location!");
                return;
            }
            if (Category.isEmpty()){
                WarningToast("Event must have a Category!");
                return;
            }
            if (Criteria.isEmpty()){
                WarningToast("Event must have a signup criteria!");
                return;
            }

            // Create event
            Event CreatedEvent = new Event(
                    EventName,
                    RegStartDate,
                    RegEndDate,
                    EventStartDate,
                    EventEndDate,
                    orgID,
                    Description,
                    Criteria,
                    Category
            );
            CreatedEvent.setLocation(Location);

            // Optionals
            if (!MaxEnt.isBlank()){
                CreatedEvent.setMaxWaitingEntrants(Integer.parseInt(MaxEnt));
            }

            // Generate QR code for the new event
            Bitmap qrCodeBitmap = CreatedEvent.generateQRCode();
            if (qrCodeBitmap != null) {
                Log.d("QR_CODE", "QR code generated for new event: " + CreatedEvent.getEventName());
            }

            Data.Add(CreatedEvent);

            // helper: refresh upcoming list
            Runnable refreshUpcoming = () ->
                    Data.fetchOrgsUpcomingEvents(orgID, this::UpdateEventList, e -> {
                        Log.d("FIREBASE Error", "onCreateView: Error with Event results".concat(e.toString()));
                    });

            // Poster upload
            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = requireContext()
                            .getContentResolver()
                            .openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    // prevent giant posters
                    if (bitmap.getByteCount() > 2_000_000) {
                        bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true);
                        Toast.makeText(requireContext(), "Poster compressed to fit upload size.", Toast.LENGTH_SHORT).show();
                    } else if (bitmap.getByteCount() > 800_000) {
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
                                Data.updateEvent(
                                        CreatedEvent.getEventId(),
                                        Map.of("imageUrl", imageDocId),
                                        () -> {
                                            CreatedEvent.setImageUrl(imageDocId);
                                            Toast.makeText(requireContext(), "Poster saved!", Toast.LENGTH_SHORT).show();
                                            // refresh AFTER link is saved
                                            refreshUpcoming.run();
                                        },
                                        e -> {
                                            Toast.makeText(requireContext(), "Failed to link poster!", Toast.LENGTH_SHORT).show();
                                            refreshUpcoming.run();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Poster upload failed!", Toast.LENGTH_SHORT).show();
                                refreshUpcoming.run();
                            });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(requireContext(), "Image convert failed", Toast.LENGTH_SHORT).show();
                    refreshUpcoming.run();
                }
            } else {
                // no poster chosen → just refresh
                refreshUpcoming.run();
            }

            // Close the popup
            NewEvent.dismiss();
        });
    }

    /** Popup to display created Event's information. */
    private void showEventPopup(Event event){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.organization_event_popup, null);

        //Setting References
        TextView Description = popupView.findViewById(R.id.Organizer_EventPopup_Description);
        TextView Criteria = popupView.findViewById(R.id.Organizer_EventPopup_Criteria);
        TextView Category = popupView.findViewById(R.id.Organizer_EventPopup_Category);
        TextView Location = popupView.findViewById(R.id.Organizer_EventPopup_EventLocation);

        ImageView QRCode = popupView.findViewById(R.id.Organizer_EventPopup_QR_Code);
        TextView RegPeriod = popupView.findViewById(R.id.Organizer_EventPopup_RegistrationPeriod);
        TextView RunTime = popupView.findViewById(R.id.Organizer_EventPopup_EventRuntime);
        TextView Capacity = popupView.findViewById(R.id.Organizer_EventPopup_Capacity);

        // Event Poster
        Button updatePosterButton = popupView.findViewById(R.id.btnUpdatePoster);
        if (updatePosterButton != null) {
            updatePosterButton.setOnClickListener(v -> {
                currentEventForUpdate = event;
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(pickIntent, "Select New Poster"), PICK_IMAGE_REQUEST);
            });
        }

        Button viewPosterButton = popupView.findViewById(R.id.btnViewPoster);
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

        Bitmap qrBitmap = event.generateQRCode();
        if (qrBitmap != null) {
            QRCode.setImageBitmap(qrBitmap);
        } else {
            QRCode.setImageResource(android.R.drawable.ic_dialog_alert);
            Toast.makeText(requireContext(), "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }

        //Description
        Description.setText(event.getDescription());
        Criteria.setText("Event Criteria: ".concat(event.getCriteria()));
        Category.setText("Event Cateogry: ".concat(event.getCategory()));

        RegPeriod.setText("Registration Period: "
                .concat(event.getRegistrationStart().toString())
                .concat(" - ")
                .concat(event.getRegistrationEnd().toString()));

        RunTime.setText("Event Runtime: "
                .concat(event.getEventStart().toString())
                .concat(" - ")
                .concat(event.getEventEnd().toString()));

        if (event.hasLocation()){
            Location.setText("Location: ".concat(event.getLocation()));
        }

        Capacity.setText("Waitlist Capacity: "
                .concat(Integer.toString(event.getWaitingList().size()))
                .concat("/")
                .concat(event.getMaxWaitingEntrantsString()));

        //WaitList Button
        Button WaitListButton = popupView.findViewById(R.id.Organizer_EventPopup_WaitList);
        WaitListButton.setOnClickListener(v -> {
            WaitListPopup(event);
        });

        //Invited List Button
        Button InviteListButton = popupView.findViewById(R.id.Organizer_EventPopup_InvList);
        InviteListButton.setOnClickListener(v -> {
            Log.d("DEBUG","PRE BUILDPOPUP INVITEDLIST: ".concat(event.getInvitedList().toString()));
            View listView = inflater.inflate(R.layout.listview_popup, null);
            ListView InvitedList = listView.findViewById(R.id.popUp_Listview);
            Data.getEventInvitedList(event.getEventId(),
                    p -> { UpdateProfileList(p, InvitedList); },
                    e -> { Log.d("DEBUG: Error", "Firebase Error".concat(e.toString())); });

            new AlertDialog.Builder(requireContext())
                    .setTitle("Invited Entrants")
                    .setView(listView)
                    .setNegativeButton("Close", null)
                    .setPositiveButton("Export to CSV", ((dialog, which) -> {})).show();
        });

        //Show the Popup
        AlertDialog eventDialog = new AlertDialog.Builder(requireContext())
                .setTitle(event.getEventName())
                .setView(popupView)
                .setNegativeButton("Close", null)
                .show();

        //Notification Pop-up, closes event pop-up
        Button notificationButton = popupView.findViewById(R.id.btnSendNotifications);
        notificationButton.setOnClickListener(v->{
            Log.d("DEBUG", "Notification button clicked!");
            organizerVM.setSelectedEvent(event);
            eventDialog.dismiss();
            NavHostFragment.findNavController(Organizer_UpcomingFragment.this)
                    .navigate(R.id.notificationSenderFragment);
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

                    if (bitmap.getByteCount() > 2_000_000) {
                        bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true);
                        Toast.makeText(requireContext(), "Poster compressed to fit upload size.", Toast.LENGTH_SHORT).show();
                    } else if (bitmap.getByteCount() > 800_000) {
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

                    // If old poster exists → delete it first
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
        View popupView = inflater.inflate(R.layout.organization_event_popup, null);
        Log.d("DEBUG","PRE BUILDPOPUP");
        View waitlistView = inflater.inflate(R.layout.organization_event_waitlist_popup, null);
        ListView List = waitlistView.findViewById(R.id.waitlistpopup_listview);

        Data.getEventWaitlist(event.getEventId(),
                p -> { UpdateProfileList(p, List); },
                e -> { Log.d("DEBUG: Error", "Firebase Error".concat(e.toString())); });

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

    /** POPUP that invites the entrants according to the number inputted by the user. */
    private void InviteNumberPopup(Event event, LayoutInflater inflater, ListView waitlistView){
        View helperView = inflater.inflate(R.layout.text_input_helper,null);
        EditText numberInp = helperView.findViewById(R.id.EditText_helper);

        new AlertDialog.Builder(requireContext())
                .setTitle("Number of entrants to invite (Up to ".concat(Integer.toString(event.getWaitingList().size())).concat(")"))
                .setView(helperView)
                .setPositiveButton("Confirm",((dialog, which) -> {
                    int number;
                    try {
                        number = Integer.parseInt(numberInp.getText().toString().trim());
                    } catch (Exception e) {
                        number = 0;
                    }

                    event.InviteEntrants(number);

                    Map<String, Object> update = new HashMap<>();
                    update.put("waitingList",event.getWaitingList());
                    update.put("invitedList",event.getInvitedList());

                    Data.updateEvent(event.getEventId(), update,
                            () -> { RefreshWaitlist(event,waitlistView); },
                            e -> Log.d("Firebase Error", "Error pushing wait/invlist changes to server:".concat(e.toString())));
                }))
                .setNegativeButton("Cancel",null)
                .show();
    }

    // -------------------- UPDATING LISTVIEWS -------------//

    private void UpdateEventList(List<Event> eventsToShow){
        upcomingEvents.clear();
        if (eventsToShow != null) {
            upcomingEvents.addAll(eventsToShow);
        }
        if (eventAdapter != null) {
            eventAdapter.clear();
            eventAdapter.addAll(upcomingEvents);
            eventAdapter.notifyDataSetChanged();
        }
        Log.d("DEBUG Updated List", "Organizer Event List update ran");
    }

    /** Updates the passed ListView with the array passed. Sets the adapter */
    private void UpdateProfileList(List<Profile> itemsToShow, ListView EventList){
        ArrayAdapter<Profile> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                itemsToShow
        );
        EventList.setAdapter(adapter);
    }

    /**
     * Refreshes the waitlist for a given event and updates the ListView.
     */
    private void RefreshWaitlist(Event event, ListView waitlist){
        Data.getEventWaitlist(event.getEventId(),
                p -> { UpdateProfileList(p,waitlist); },
                e -> { Log.d("DEBUG: Error", "Firebase Error".concat(e.toString())); });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                event.getWaitingList()
        );
        waitlist.setAdapter(adapter);
    }

    private void WarningToast(String warning){
        Toast WarningToast = new Toast(requireContext());
        WarningToast.setText(warning);
        WarningToast.show();
    }
}
