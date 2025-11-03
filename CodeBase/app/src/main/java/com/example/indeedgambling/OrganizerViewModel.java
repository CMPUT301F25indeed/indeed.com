package com.example.indeedgambling;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for Organizer user.
 *
 * Stores the logged-in organizer's profile and the event they have selected.
 * Helps share data between fragments without passing it through fragment arguments.
 *
 * LiveData ensures UI reacts to changes automatically.
 */
public class OrganizerViewModel extends ViewModel {

    /** Holds currently logged-in organizer's profile */
    private final MutableLiveData<Profile> organizer = new MutableLiveData<>();

    /** Holds the organizer's currently selected event (for editing, viewing entrants, etc.) */
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();

    /**
     * Assign the logged-in organizer profile
     *
     * @param p organizer profile
     */
    public void setOrganizer(Profile p) {
        organizer.setValue(p);
    }

    /**
     * @return LiveData for organizer profile (UI can observe)
     */
    public MutableLiveData<Profile> getOrganizer() {
        return organizer;
    }

    /**
     * Stores the event selected by the organizer
     *
     * @param e event selected
     */
    public void setSelectedEvent(Event e) {
        selectedEvent.setValue(e);
    }

    /**
     * @return LiveData for current selected event
     */
    public MutableLiveData<Event> getSelectedEvent() {
        return selectedEvent;
    }
}
