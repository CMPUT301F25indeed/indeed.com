package com.example.indeedgambling;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class FireBaseViewModel extends AndroidViewModel {
    private FirebaseFirestore db;
    private MutableLiveData<ArrayList<Event>> Events;
    private MutableLiveData<ArrayList<Profile>> Profiles;


    public FireBaseViewModel(@NonNull Application application){
        super(application);
        db = FirebaseFirestore.getInstance();
        Events = new MutableLiveData<>(new ArrayList<Event>());
        Profiles = new MutableLiveData<>(new ArrayList<Profile>());
        Sync();
    }

    /** Sync local data with Firebase
     * Overwrites local information.
     */
    public void Sync(){
        //Overwriting local with cloud

        //Events Syncing
        db.collection("Event").get().addOnCompleteListener(task ->{
            //Replace local with cloud
            ArrayList<Event> ResultArray = (ArrayList<Event>) task.getResult().toObjects(Event.class);

            //Add difference to cloud.
            //If cloud result has all local, do nothing

            //If the cloud is missing items, push
            if (!ResultArray.containsAll(Objects.requireNonNull(Events.getValue()))){
                //Push all non-existants to cloud.
                for (Event e: Events.getValue()) {
                    if (!ResultArray.contains(e)){
                        this.Add(e);
                    }
                }
            }
            //Updating local to cloud
            else{
                Events.setValue(ResultArray);
            }
        });
        //Profile sync
        db.collection("Profile").get().addOnCompleteListener(task -> {
            Profiles.setValue((ArrayList<Profile>) task.getResult().toObjects(Profile.class));
        });
    }

    /** Deletes an object from the firebase
     *
     * @param CollectionPath ex: event, profile, notification, etc.
     * @param InstanceName Name of the object to delete
     */
    public void Delete(String CollectionPath, String InstanceName){
        db.collection(CollectionPath).document(InstanceName);
    }

    /** Deletes an object from the firebase, path found automatically
     * Deletes an object locally
     * @param ItemToDelete Object deleted from firebase
     */
    public void Delete(Object ItemToDelete){
        db.collection(ItemToDelete.getClass().getSimpleName()).document(ItemToDelete.toString()).delete();

        //Remove from local
        if (ItemToDelete.getClass() == Event.class){
            Log.d("DEBUG Delete", Boolean.toString(Objects.requireNonNull(Events.getValue()).remove((Event) ItemToDelete)));
        }
        else if (ItemToDelete.getClass() == Profile.class){
            Objects.requireNonNull(Profiles.getValue()).remove((Profile) ItemToDelete);
        }
    }


    /** Saves the object under the document with the Class name
     * @param InstanceName Save Name of the object
     * @param ItemToSave Object to be saved
     */
    public void Add(String InstanceName, Object ItemToSave){
        db.collection(ItemToSave.getClass().getSimpleName()).document(InstanceName).set(ItemToSave);
        //Adding to local
        if (ItemToSave.getClass() == Event.class){
            Objects.requireNonNull(Events.getValue()).add((Event) ItemToSave);
        }
        else if (ItemToSave.getClass() == Profile.class){
            Objects.requireNonNull(Profiles.getValue()).add((Profile) ItemToSave);
        }
    }

    /** Saves object to server using Object's name and Class Name.
     * @param ItemToSave
     */
    public void Add(Object ItemToSave){

        //Event object
        if (ItemToSave.getClass() == Event.class){
            //Adding to local for instant change
            Objects.requireNonNull(Events.getValue()).add((Event) ItemToSave);
            //Adding to server
            db.collection(ItemToSave.getClass().getSimpleName()).document(ItemToSave.toString()).set(ItemToSave);
        }
        //Profile object
        else if (ItemToSave.getClass() == Profile.class){
            //Adding to local for instant change
            Objects.requireNonNull(Profiles.getValue()).add((Profile) ItemToSave);
            //Adding to server
            db.collection(ItemToSave.getClass().getSimpleName()).document(String.valueOf(ItemToSave.hashCode())).set(ItemToSave);
        }
    }

    /** Checks if the LocalCache contains the object
     * @param ItemToCheck Object checking
     * @return True if object held, False otherwise. Returns FALSE if object not handled
     */
    public Boolean Contains(Object ItemToCheck){
        //Checking Local
        //Will need to test some more
        if (ItemToCheck.getClass() == Event.class){
            return Objects.requireNonNull(Events.getValue()).contains((Event) ItemToCheck);
        }
        else if (ItemToCheck.getClass() == Profile.class){
            return Objects.requireNonNull(Profiles.getValue()).contains((Profile) ItemToCheck);
        }
        //Catch all. If we don't have a matching type, we definitely don't have it saved.
        return false;
    }

    /** Returns the local cache of Events, from last sync
     * @return ArrayList of Events, sorted
     */
    public ArrayList<Event> getEvents(){
        Objects.requireNonNull(Events.getValue()).sort((Event1, Event2)-> Event1.getRegistrationEnd().compareTo(Event2.getRegistrationEnd()));
        return Events.getValue();
    }

    public ArrayList<Profile> getProfiles(){
        return Profiles.getValue();
    }

    /** Returns all profiles that match the hash
     * May only return 1 profile since duplicate names are not supported in Firebase
     * @return
     */
    private ArrayList<Profile> getMatchingProfiles(Profile profile){
        ArrayList<Profile> ReturnArray = new ArrayList<>();
        for (Profile p : Profiles.getValue()){
          if (p.hashCode() == profile.hashCode()) ReturnArray.add(p);
        }
        return ReturnArray;
    }

    /** Returns an ArrayList for events that are currently open to register and are not full on waiting Entrants.
     * @return Events that can take registration to Waitlist.
     */
    private ArrayList<Event> getRegisterableEvents(){
        ArrayList<Event> ReturnArray = new ArrayList<>();
        for (Event e : Events.getValue()){
            if (e.waitList_registrable()){
                ReturnArray.add(e);
            }
        }

        return ReturnArray;
    }
}
