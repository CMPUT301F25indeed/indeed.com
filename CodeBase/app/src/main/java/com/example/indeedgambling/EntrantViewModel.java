package com.example.indeedgambling;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for Entrant user.
 *
 * Holds the logged-in Entrant profile and the currently selected Event.
 * Used to share data between fragments without passing arguments manually.
 *
 * LiveData ensures UI updates automatically when data changes.
 */
public class EntrantViewModel extends ViewModel {

    /** Stores the currently logged-in entrant profile */
    private final MutableLiveData<Entrant> entrant = new MutableLiveData<>();

    /** Stores the event currently selected by entrant (when browsing or joining) */
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();

    /**
     * Sets the logged-in entrant profile
     *
     * @param e Entrant object of the logged-in user
     */
    public void setEntrant(@NonNull Entrant e) {
        entrant.setValue(e);
    }

    /**
     * @return Entrant object for direct access (not observable)
     */
    public Entrant getCurrentEntrant() {
        return entrant.getValue();
    }

    /**
     * @return LiveData for observing entrant profile changes from UI
     */
    public MutableLiveData<Entrant> getEntrant() {
        return entrant;
    }

    /**
     * Sets the event currently selected by the entrant
     *
     * @param e event selected
     */
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
