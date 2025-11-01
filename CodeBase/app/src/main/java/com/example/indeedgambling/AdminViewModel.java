package com.example.indeedgambling;

import androidx.lifecycle.ViewModel;

public class AdminViewModel extends ViewModel {
    private Admin admin;
    public void setAdmin(Admin a) { this.admin = a; }
    public Admin getAdmin() { return admin; }
}
