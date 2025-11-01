package com.example.indeedgambling;

import androidx.lifecycle.ViewModel;

public class OrganizerViewModel extends ViewModel {
    private Organizer organizer;
    public void setOrganizer(Organizer o) { this.organizer = o; }
    public Organizer getOrganizer() { return organizer; }
}
