package com.example.indeedgambling;

import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

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
        Log.d("CheckHash item:" + profileName, hashProfile(profileName,password));
        Log.d("CheckHash profile:" + profileName, hashProfile(new Profile(password,profileName)));
        try{
            DocumentSnapshot snapshot = Tasks.await(ProfRef.document(hashProfile(profileName,password)).get());
            return snapshot.exists();
        } catch (Exception e){
            Log.e("Error", e.toString());
            return false;
        }
        //return ProfRef.document(hashProfile(profileName,password)).get().isSuccessful();
    }

    public boolean hasProfile(Profile profile){
        Log.d("CheckHash item:" + profile.getProfileName(), hashProfile(profile.getProfileName(), profile.getPassword()));
        Log.d("CheckHash profile:"  + profile.getProfileName(), hashProfile(profile));
        try{
            DocumentSnapshot snapshot = Tasks.await(ProfRef.document(hashProfile(profile)).get());
            return snapshot.exists();
        } catch (Exception e){
            Log.e("Error", e.toString());
            return false;
        }
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
        //TODO: resolve error with negative secondary value. Does not write to Firebase
        //Ex: 10238123-1923123 does not write.
        // Perhaps do a classic mult of their signs.
            // neg * neg = pos
            // neg * pos = neg

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
