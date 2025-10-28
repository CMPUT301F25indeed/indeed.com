package com.example.indeedgambling;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.concurrent.ExecutionException;

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
    public void hasProfile(String profileName, String password){
    }

    public void hasProfile(Profile profile){
    }

    public void TryLogIn(){

    }

    /** Adds the profile to the Firebase.
     * Does not check for duplicates. Currently overwrites existing profiles.
     * @param profile Object whose attributes are to be stored in firebase. ONLY stores used attributes (anywhere in code).
     */
    public void addProfile(Profile profile){
        String hash = hashProfile(profile);
        Log.d(profile.profileName + "Check new profile Hash", hash);

        //TODO: Check if hash already exists.
        ProfRef.document(hash).set(profile);

    }

    /** Utility function to return a combined hash of the profile's profileName and password
     * @param profile Object to get the hash of
     * @return Hash(profileName).append(Hash(password))
     */
    protected String hashProfile(Profile profile){
        //Function sets the multiplicative result of the two hashes as the signage for the combination
        // ex: -102 , 302 -> -102302
        // ex: -334, -212 -> 334212
        // ex: 123, 456 -> 123456
        // Done to try to resolve an error with writing to Firebase. Using 331-152 as the document title causes Firebase to subtract the values for the title.

        String Hash;



        //If both are negative, make pos. If both pos, keep pos.
        if ((profile.getProfileName().hashCode() < 0 && profile.getPassword().hashCode() < 0) || (profile.getProfileName().hashCode() > 0 && profile.getPassword().hashCode() > 0)){
            Hash = Integer.toString(Math.abs(profile.getProfileName().hashCode())) + Integer.toString(Math.abs(profile.getPassword().hashCode()));
        }
        //Only 1 is negative.
        else{
            Hash = "-" + Integer.toString(Math.abs(profile.getProfileName().hashCode())) + Integer.toString(Math.abs(profile.getPassword().hashCode()));
        }
        return Hash;
        //return Integer.toString(profile.getProfileName().hashCode()) + Integer.toString(profile.getPassword().hashCode());
    }

    /** Overload for hashing the attributes of a Profile.
     *
     * @param name from profile
     * @param password from profile
     * @return Hash(profileName).append(Hash(password))
     */
    protected String hashProfile(String name, String password){

        String Hash;
        //If both are negative, make pos. If both pos, keep pos.
        if ((name.hashCode() < 0 && password.hashCode() < 0) || (name.hashCode() > 0 && password.hashCode() > 0)){
            Hash = Integer.toString(Math.abs(name.hashCode())) + Integer.toString(Math.abs(password.hashCode()));
        }
        //Only 1 is negative.
        else{
            Hash = "-" + Integer.toString(Math.abs(name.hashCode())) + Integer.toString(Math.abs(password.hashCode()));
        }
        //return Integer.toString(name.hashCode()) + Integer.toString(password.hashCode());
        return Hash;
    }
}
