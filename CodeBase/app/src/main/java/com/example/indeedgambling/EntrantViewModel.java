package com.example.indeedgambling;

import androidx.lifecycle.ViewModel;

public class EntrantViewModel extends ViewModel {
    private Entrant entrant;
    public void setEntrant(Entrant e) { this.entrant = e; }
    public Entrant getEntrant() { return entrant; }
}
