package com.example.indeedgambling;

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

    // card adapter + backing list
    private OrganizerEventCardAdapter eventAdapter;
    private final List<Event> upcomingEvents = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.organization_upcomingevents_fragment, container, false);
        Data = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        organizerVM = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        orgID = organizerVM.getOrganizer().getValue().getProfileId();

        // Home button
        Button Home = view.findViewById(R.id.Organizer_Upcoming_HomeButton);
        Home.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_organizerUpcomingFragment_to_organizerHomeFragment));

        // Events list
        EventList = view.findViewById(R.id.Organizer_UpcomingEventList);

        eventAdapter = new OrganizerEventCardAdapter(
                requireContext(),
                new ArrayList<>(),
                Data
        );
        EventList.setAdapter(eventAdapter);

        Data.fetchOrgsUpcomingEvents(orgID, this::UpdateEventList, e ->
                Log.d("Debug", "onCreateView: Error with results".concat(e.toString())));

        // click → popup
        EventList.setOnItemClickListener((parent, itemView, position, id) -> {
            Event clickedEvent = (Event) parent.getItemAtPosition(position);
            showEventPopup(clickedEvent);
        });

        // New Event button
        Button NewEvent = view.findViewById(R.id.Organizer_Upcoming_NewEventButton);
        NewEvent.setOnClickListener(v -> showNewEventPopup());

        return view;
    }

    // ---------------- POPUPS ----------------

    /** New event popup */
    private void showNewEventPopup() {
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
        EditText MaxEntrantsInput = popupView.findViewById(R.id.NewEventPopup_MaxEntrantsDialog);
        EditText DescriptionInput = popupView.findViewById(R.id.NewEventPopup_Description);
        EditText CategoryInput = popupView.findViewById(R.id.NewEventPopup_Category);
        EditText LocationInput = popupView.findViewById(R.id.NewEventPopup_Location);
        EditText CriteriaInput = popupView.findViewById(R.id.NewEventPopup_Criteria);

        View RegistrationOpen = popupView.findViewById(R.id.RegistrationOpen);
        View RegistrationClose = popupView.findViewById(R.id.RegistrationClose);
        View EventOpen = popupView.findViewById(R.id.EventOpen);
        View EventClose = popupView.findViewById(R.id.EventClose);

        // Registration date/time
        DatePicker RegStartDateInput = RegistrationOpen.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker RegStartTimeInput = RegistrationOpen.findViewById(R.id.DateTimePicker_TimeDialog);
        DatePicker RegEndDateInput = RegistrationClose.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker RegEndTimeInput = RegistrationClose.findViewById(R.id.DateTimePicker_TimeDialog);

        // Event date/time
        DatePicker EventStartDateInput = EventOpen.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker EventStartTimeInput = EventOpen.findViewById(R.id.DateTimePicker_TimeDialog);
        DatePicker EventEndDateInput = EventClose.findViewById(R.id.DateTimePicker_DateDialog);
        TimePicker EventEndTimeInput = EventClose.findViewById(R.id.DateTimePicker_TimeDialog);

        // no past events
        long CurrentTime = new Date().getTime();
        EventStartDateInput.setMinDate(CurrentTime);
        EventEndDateInput.setMinDate(CurrentTime);
        RegEndDateInput.setMinDate(CurrentTime);
        RegStartDateInput.setMinDate(CurrentTime);

        AlertDialog NewEvent = new AlertDialog.Builder(requireContext())
                .setTitle("New Event")
                .setView(popupView)
                .setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", null)
                .show();

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

            if (EventStartDate.after(EventEndDate)) {
                WarningToast("Event Start cannot be BEFORE Event End!");
                return;
            }
            if (RegStartDate.after(RegEndDate)) {
                WarningToast("Registration start cannot be BEFORE Registration End!");
                return;
            }

            String EventName = NameInput.getText().toString().trim();
            String Description = DescriptionInput.getText().toString().trim();
            String Location = LocationInput.getText().toString().trim();
            String Category = CategoryInput.getText().toString().trim();
            String Criteria = CriteriaInput.getText().toString().trim();
            String MaxEnt = MaxEntrantsInput.getText().toString().trim();

            if (EventName.isEmpty()) {
                WarningToast("Event Title cannot be empty!");
                return;
            }
            if (Description.isEmpty()) {
                WarningToast("Description cannot be empty!");
                return;
            }
            if (Location.isEmpty()) {
                WarningToast("Must set a Location!");
                return;
            }
            if (Category.isEmpty()) {
                WarningToast("Event must have a Category!");
                return;
            }
            if (Criteria.isEmpty()) {
                WarningToast("Event must have a signup criteria!");
                return;
            }

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

            if (!MaxEnt.isBlank()) {
                CreatedEvent.setMaxWaitingEntrants(Integer.parseInt(MaxEnt));
            }

            Bitmap qrCodeBitmap = CreatedEvent.generateQRCode();
            if (qrCodeBitmap != null) {
                Log.d("QR_CODE", "QR code generated for new event: " + CreatedEvent.getEventName());
            }

            Data.Add(CreatedEvent);

            Runnable refreshUpcoming = () ->
                    Data.fetchOrgsUpcomingEvents(orgID, this::UpdateEventList, e ->
                            Log.d("FIREBASE Error", "Error with Event results".concat(e.toString())));

            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = requireContext()
                            .getContentResolver()
                            .openInputStream(selectedImageUri);
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

                    Map<String, Object> imageData = new HashMap<>();
                    imageData.put("eventId", CreatedEvent.getEventId());
                    imageData.put("uploaderId", orgID);
                    imageData.put("url", base64String);
                    imageData.put("uploadedAt", new Date());
                    imageData.put("approved", true);

                    Data.getDb().collection("images")
                            .add(imageData)
                            .addOnSuccessListener(docRef -> {
                                String imageDocId = docRef.getId();
                                Data.updateEvent(
                                        CreatedEvent.getEventId(),
                                        Map.of("imageUrl", imageDocId),
                                        () -> {
                                            CreatedEvent.setImageUrl(imageDocId);
                                            Toast.makeText(requireContext(), "Poster saved!", Toast.LENGTH_SHORT).show();
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
                refreshUpcoming.run();
            }

            NewEvent.dismiss();
        });
    }

    /** Event details popup */
    private void showEventPopup(Event event) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View popupView = inflater.inflate(R.layout.organization_event_popup, null);

        TextView Description = popupView.findViewById(R.id.Organizer_EventPopup_Description);
        TextView Criteria = popupView.findViewById(R.id.Organizer_EventPopup_Criteria);
        TextView Category = popupView.findViewById(R.id.Organizer_EventPopup_Category);
        TextView Location = popupView.findViewById(R.id.Organizer_EventPopup_EventLocation);
        ImageView QRCode = popupView.findViewById(R.id.Organizer_EventPopup_QR_Code);
        TextView RegPeriod = popupView.findViewById(R.id.Organizer_EventPopup_RegistrationPeriod);
        TextView RunTime = popupView.findViewById(R.id.Organizer_EventPopup_EventRuntime);
        TextView Capacity = popupView.findViewById(R.id.Organizer_EventPopup_Capacity);

        Button updatePosterButton = popupView.findViewById(R.id.btnUpdatePoster);
        Button WaitListButton = popupView.findViewById(R.id.Organizer_EventPopup_WaitList);
        Button InviteListButton = popupView.findViewById(R.id.Organizer_EventPopup_InvList);
        Button CancelledListButton = popupView.findViewById(R.id.Organizer_EventPopup_CancelledList);
        Button AcceptedListButton = popupView.findViewById(R.id.Organizer_EventPopup_AcceptedList);
        Button endRegButton = popupView.findViewById(R.id.Organizer_EventPopup_EndRegistrationNow);
        Button notificationButton = popupView.findViewById(R.id.btnSendNotifications);
        Button viewPosterButton = popupView.findViewById(R.id.btnViewPoster);

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

        Description.setText(event.getDescription());
        Criteria.setText("Event Criteria: " + event.getCriteria());
        Category.setText("Event Cateogry: " + event.getCategory());

        RegPeriod.setText("Registration Period: "
                + event.getRegistrationStart().toString()
                + " - "
                + event.getRegistrationEnd().toString());

        RunTime.setText("Event Runtime: "
                + event.getEventStart().toString()
                + " - "
                + event.getEventEnd().toString());

        if (event.hasLocation()) {
            Location.setText("Location: " + event.getLocation());
        }

        Capacity.setText("Waitlist Capacity: "
                + event.getWaitingList().size()
                + "/"
                + event.getMaxWaitingEntrantsString());

        if (WaitListButton != null) {
            WaitListButton.setOnClickListener(v -> WaitListPopup(event));
        }

        if (InviteListButton != null) {
            InviteListButton.setOnClickListener(v -> {
                Log.d("DEBUG", "PRE BUILDPOPUP INVITEDLIST: " + event.getInvitedList());
                View listView = inflater.inflate(R.layout.listview_popup, null);
                ListView InvitedList = listView.findViewById(R.id.popUp_Listview);
                Data.getEventInvitedList(event.getEventId(),
                        p -> UpdateProfileList(p, InvitedList),
                        e -> Log.d("DEBUG: Error", "Firebase Error".concat(e.toString())));

                new AlertDialog.Builder(requireContext())
                        .setTitle("Invited Entrants")
                        .setView(listView)
                        .setNegativeButton("Close", null)
                        .setPositiveButton("Export to CSV", (dialog, which) -> {})
                        .show();
            });
        }

        if (CancelledListButton != null) {
            CancelledListButton.setOnClickListener(v -> CancelledListPopup(event));
        }

        if (AcceptedListButton != null) {
            AcceptedListButton.setOnClickListener(v -> AcceptedListPopup(event));
        }

        if (endRegButton != null) {
            if (!event.RegistrationOpen()) {
                endRegButton.setVisibility(View.GONE);
            } else {
                endRegButton.setOnClickListener(v -> EndRegPopup(event));
            }
        }

        AlertDialog eventDialog = new AlertDialog.Builder(requireContext())
                .setTitle(event.getEventName())
                .setView(popupView)
                .setNegativeButton("Close", null)
                .show();

        if (notificationButton != null) {
            notificationButton.setOnClickListener(v -> {
                Log.d("DEBUG", "Notification button clicked!");
                organizerVM.setSelectedEvent(event);
                eventDialog.dismiss();
                NavHostFragment.findNavController(Organizer_UpcomingFragment.this)
                        .navigate(R.id.notificationSenderFragment);
            });
        }
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

                    Runnable uploadNewPoster = () -> Data.getDb().collection("images")
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

                    if (oldDocId != null && !oldDocId.isEmpty()) {
                        Data.getDb().collection("images").document(oldDocId)
                                .delete()
                                .addOnSuccessListener(unused -> uploadNewPoster.run())
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Old poster delete failed!", Toast.LENGTH_SHORT).show();
                                    uploadNewPoster.run();
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

    // Waitlist popup
    private void WaitListPopup(Event event) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        Log.d("DEBUG", "PRE BUILDPOPUP");
        View waitlistView = inflater.inflate(R.layout.organization_event_waitlist_popup, null);
        ListView List = waitlistView.findViewById(R.id.waitlistpopup_listview);

        Data.getEventWaitlist(event.getEventId(),
                p -> UpdateProfileList(p, List),
                e -> Log.d("DEBUG: Error", "Firebase Error".concat(e.toString())));

        Button inviteEntrants = waitlistView.findViewById(R.id.waitlistpopup_inviteEntrants_Button);
        inviteEntrants.setOnClickListener(v1 -> InviteNumberPopup(event, inflater, List));

        new AlertDialog.Builder(requireContext())
                .setTitle("Waitlist")
                .setView(waitlistView)
                .setNegativeButton("Close", null)
                .setPositiveButton("Export to CSV", (dialog, which) -> {})
                .show();
    }

    // invite N entrants popup
    private void InviteNumberPopup(Event event, LayoutInflater inflater, ListView waitlistView) {
        View helperView = inflater.inflate(R.layout.text_input_helper, null);
        EditText numberInp = helperView.findViewById(R.id.EditText_helper);

        new AlertDialog.Builder(requireContext())
                .setTitle("Number of entrants to invite (Up to " + event.getWaitingList().size() + ")")
                .setView(helperView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    int number;
                    try {
                        number = Integer.parseInt(numberInp.getText().toString().trim());
                    } catch (Exception e) {
                        number = 0;
                    }

                    event.InviteEntrants(number);

                    Map<String, Object> update = new HashMap<>();
                    update.put("waitingList", event.getWaitingList());
                    update.put("invitedList", event.getInvitedList());

                    Data.updateEvent(event.getEventId(), update,
                            () -> RefreshWaitlist(event, waitlistView),
                            e -> Log.d("Firebase Error", "Error pushing wait/invlist changes to server:".concat(e.toString())));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // -------- list helpers ----------

    private void UpdateEventList(List<Event> eventsToShow) {
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

    private void UpdateProfileList(List<Profile> itemsToShow, ListView EventList) {
        ArrayAdapter<Profile> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                itemsToShow
        );
        EventList.setAdapter(adapter);
    }

    private void RefreshWaitlist(Event event, ListView waitlist) {
        Data.getEventWaitlist(event.getEventId(),
                p -> UpdateProfileList(p, waitlist),
                e -> Log.d("DEBUG: Error", "Firebase Error".concat(e.toString())));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                event.getWaitingList()
        );
        waitlist.setAdapter(adapter);
    }

    private void RefreshUpcomingEventList() {
        Data.fetchOrgsUpcomingEvents(orgID, this::UpdateEventList, e ->
                Log.d("Debug", "Error refreshing upcoming events: ".concat(e.toString())));
    }

    // Cancelled list popup
    private void CancelledListPopup(Event event) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View popupView = inflater.inflate(R.layout.listview_popup, null);
        ListView cancelledListView = popupView.findViewById(R.id.popUp_Listview);

        Data.getProfiles(event.getCancelledEntrants(),
                cancelledList -> {
                    ArrayAdapter<Profile> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            cancelledList
                    );
                    cancelledListView.setAdapter(adapter);
                },
                e -> Log.d("Firestore Error", e.toString()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelled Entrants")
                .setView(popupView)
                .setNegativeButton("Close", null)
                .setPositiveButton("Replace Entrants", (dialog, which) -> {
                    replaceEntrants(event, inflater);
                    RefreshUpcomingEventList();
                })
                .show();
    }

    // Accepted list popup  (now takes Event → fixes "expected no arguments" error)
    private void AcceptedListPopup(Event event) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View popupView = inflater.inflate(R.layout.listview_popup, null);
        ListView acceptedListView = popupView.findViewById(R.id.popUp_Listview);

        Data.getProfiles(event.getAcceptedEntrants(),
                acceptedList -> {
                    ArrayAdapter<Profile> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            acceptedList
                    );
                    acceptedListView.setAdapter(adapter);
                },
                e -> Log.d("Firestore Error", e.toString()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Accepted Entrants")
                .setView(popupView)
                .setNegativeButton("Close", null)
                .setPositiveButton("Export to CSV", (dialog, which) -> {})
                .show();
    }

    // End registration popup
    private void EndRegPopup(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Are you sure you want to end the registration period?")
                .setMessage("Doing so will close the event")
                .setPositiveButton("Yes", (dialog, which) -> {
                    event.endRegistration();

                    Map<String, Object> update = new HashMap<>();
                    update.put("registrationEnd", event.getRegistrationEnd());

                    Data.updateEvent(event.getEventId(), update,
                            () -> {
                                WarningToast("Registration for " + event.getEventName() + " ended");
                                RefreshUpcomingEventList();
                            },
                            e -> Log.d("Firebase Error", "Error pushing registration changes to server:".concat(e.toString())));
                })
                .setNegativeButton("Never mind", null)
                .show();
    }

    // replace cancelled entrants
    private void replaceEntrants(Event event, LayoutInflater inflater) {
        View helperView = inflater.inflate(R.layout.text_input_helper, null);
        EditText numberInp = helperView.findViewById(R.id.EditText_helper);

        int NumCancelled = event.getCancelledEntrants().size();
        int numWaiting = event.getLostList().size() + event.getWaitingList().size();
        if (numWaiting == 0) {
            WarningToast("No waiting entrants to invite!");
            return;
        }
        if (NumCancelled == 0) {
            WarningToast("There are no Cancelled Entrants to replace!");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Number of entrants to replace (Up to " +
                        Math.min(NumCancelled, numWaiting) + ")")
                .setView(helperView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    int number;
                    try {
                        number = Integer.parseInt(numberInp.getText().toString().trim());
                    } catch (Exception e) {
                        number = 0;
                    }

                    if (number > event.getCancelledEntrants().size()) {
                        number = event.getCancelledEntrants().size();
                    }

                    event.InviteEntrants(number);

                    Map<String, Object> update = new HashMap<>();
                    update.put("waitingList", event.getWaitingList());
                    update.put("lostList", event.getLostList());
                    update.put("invitedList", event.getInvitedList());

                    Data.updateEvent(event.getEventId(),
                            update,
                            () -> {},
                            e -> Log.d("Firebase Error", "Error pushing wait/invlist changes to server:".concat(e.toString())));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void WarningToast(String warning) {
        Toast WarningToast = new Toast(requireContext());
        WarningToast.setText(warning);
        WarningToast.show();
    }
}
