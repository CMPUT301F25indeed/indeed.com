package com.example.indeedgambling;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OrganizerViewModel extends ViewModel {

    private final MutableLiveData<Profile> organizer = new MutableLiveData<>();
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();

    public void setOrganizer(Profile p) {
        organizer.setValue(p);
    }

    public MutableLiveData<Profile> getOrganizer() {
        return organizer;
    }

    public void setSelectedEvent(Event e) {
        selectedEvent.setValue(e);
    }

    public MutableLiveData<Event> getSelectedEvent() {
        return selectedEvent;
    }
}
