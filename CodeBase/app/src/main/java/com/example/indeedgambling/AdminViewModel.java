package com.example.indeedgambling;

import androidx.lifecycle.ViewModel;

import java.util.Map;

/**
 * ViewModel for Admin user.
 *
 * Purpose:
 * - Stores the currently logged-in Admin object in memory.
 * - Allows fragments to access admin data without passing it through arguments.
 *
 * Unlike Entrant & Organizer, this ViewModel does not currently use LiveData
 * because there is only one admin object and UI is unlikely to observe changes.
 * Can be easily upgraded later if reactive updates are needed.
 */
public class AdminViewModel extends ViewModel {

    /** Holds the logged-in admin user */
    private Admin admin;

    /**
     * Save the admin instance when login happens
     *
     * @param a Admin object for the current logged-in admin
     */
    public void setAdmin(Admin a) { this.admin = a; }

    /**
     * Get the stored admin info
     *
     * @return Admin object for the logged-in admin
     */
    public Admin getAdmin() { return admin; }

    public void updateSettings(Map<String, Object> updates) {
        if (admin == null) return;

        for (String key : updates.keySet()) {

            if (key.equals("personName")) {
                admin.setPersonName((String) updates.get(key));
            }

            if (key.equals("email")) {
                admin.setEmail((String) updates.get(key));
            }

            if (key.equals("phone")) {
                admin.setPhone((String) updates.get(key));
            }

            if (key.equals("notificationsEnabled")) {
                admin.setNotificationsEnabled((Boolean) updates.get(key));
            }

            if (key.equals("lightMode")) {
                admin.setLightMode((Boolean) updates.get(key));
            }
        }
    }

}
