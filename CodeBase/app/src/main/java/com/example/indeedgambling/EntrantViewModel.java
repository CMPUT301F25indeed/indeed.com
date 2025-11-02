package com.example.indeedgambling;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EntrantViewModel extends ViewModel {

    private final MutableLiveData<Profile> entrant = new MutableLiveData<>();
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();

    // set currently logged in entrant
    public void setEntrant(Profile p) {
        entrant.setValue(p);
    }

    // get currently logged in entrant
    public Profile getCurrentEntrant() {
        return entrant.getValue();
    }

    // LiveData getter if UI wants to observe
    public MutableLiveData<Profile> getEntrant() {
        return entrant;
    }

    // event selection storage
    public void setSelectedEvent(Event e) {
        selectedEvent.setValue(e);
    }

    public MutableLiveData<Event> getSelectedEvent() {
        return selectedEvent;
    }
}
