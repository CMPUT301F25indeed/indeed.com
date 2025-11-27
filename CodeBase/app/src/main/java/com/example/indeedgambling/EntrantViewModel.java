package com.example.indeedgambling;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Map;

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


    /**
     * Sets the logged-in entrant profile
     * @param e Entrant object of the logged-in user*/
    public void setEntrant(@NonNull Entrant e) {
        entrant.setValue(e);
    }

    /** @return Entrant object for direct access*/
    public Entrant getCurrentEntrant() {
        return entrant.getValue();
    }

    /**@return LiveData for observing entrant profile changes from UI*/
    public MutableLiveData<Entrant> getEntrant() {
        return entrant;
    }

    public String returnID() {
        Entrant e = entrant.getValue();
        return (e != null) ? e.getProfileId() : null;
    }


    public void addEventToEntrant(@NonNull String eventId) {
        Entrant e = entrant.getValue();
        if (e != null) {
            e.add2Entrant(eventId);
            entrant.setValue(e); // notify observers
        }
    }

    public void removeEventFromEntrant(@NonNull String eventId) {
        Entrant e = entrant.getValue();
        if (e != null) {
            e.remove2Entrant(eventId);
            entrant.setValue(e); // notify observers
        }
    }

    public void inviteEntrantRemoveWaitlist(@NonNull String eventId) {
        Entrant e = entrant.getValue();
        if (e != null) {
            e.removeWaitlistedEvent(eventId);
            entrant.setValue(e); // notify observers
        }
    }



    public void updateSettings(Map<String, Object> updates) {
        Entrant e = entrant.getValue();
        if (e == null) return;

        for (String key : updates.keySet()) {

            if (key.equals("personName")) {
                e.setPersonName((String) updates.get(key));
            }

            if (key.equals("email")) {
                e.setEmail((String) updates.get(key));
            }

            if (key.equals("phone")) {
                e.setPhone((String) updates.get(key));
            }

            if (key.equals("notificationsEnabled")) {
                e.setNotificationsEnabled((Boolean) updates.get(key));
            }

            if (key.equals("lightMode")) {
                e.setLightMode((Boolean) updates.get(key));
            }
        }

        entrant.setValue(e); // notify observers
    }

}
