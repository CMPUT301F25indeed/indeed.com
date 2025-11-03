package com.example.indeedgambling;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for Entrant user.
 *
 * Holds the logged-in Entrant profile and the currently selected Event.
 * Used to share data between fragments without passing arguments manually.
 *
 * LiveData makes sure UI updates automatically when values change.
 */
public class EntrantViewModel extends ViewModel {

    /** Stores the currently logged-in entrant profile */
    private final MutableLiveData<Profile> entrant = new MutableLiveData<>();

    /** Stores the event currently selected by entrant (when browsing or joining) */
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();

    /**
     * Sets the logged-in entrant profile
     *
     * @param p Profile object of the logged-in entrant
     */
    // set currently logged in entrant
    public void setEntrant(Profile p) {
        entrant.setValue(p);
    }

    /**
     * @return Profile of current logged-in entrant (non LiveData direct getter)
     */
    // get currently logged in entrant
    public Profile getCurrentEntrant() {
        return entrant.getValue();
    }

    /**
     * @return LiveData for observing entrant profile changes from UI
     */
    // LiveData getter if UI wants to observe
    public MutableLiveData<Profile> getEntrant() {
        return entrant;
    }

    /**
     * Sets the event currently selected by the entrant
     *
     * @param e event selected
     */
    // event selection storage
    public void setSelectedEvent(Event e) {
        selectedEvent.setValue(e);
    }

    /**
     * @return LiveData for observing selected event changes
     */
    public MutableLiveData<Event> getSelectedEvent() {
        return selectedEvent;
    }
}
