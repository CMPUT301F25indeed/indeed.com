package com.example.indeedgambling;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/** A handler class for all functions upon all stored profiles.
 *  Accesses our Firebase for Profiles.
 *
 */
public class ProfileHandler {
    private FirebaseFirestore db;
    private CollectionReference ProfRef;

    /**
     * Creation of ProfileHandler class accesses Firebase to gather profile data.
     */
    public ProfileHandler(){
        db = FirebaseFirestore.getInstance();
        ProfRef = db.collection("profiles");
    }

    /**
     * Boolean function that tests if a given profile has matching data in the database.
     * @param profileName Username to match
     * @param password Password to match
     * @return Boolean
     */
    public boolean hasProfile(String profileName, String password){
        return false;
    }
}
