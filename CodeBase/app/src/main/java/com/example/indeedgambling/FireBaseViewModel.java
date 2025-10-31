package com.example.indeedgambling;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
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
        Sync();
    }

    /** Sync local data with Firebase
     * Overwrites local information.
     */
    public void Sync(){
        //Events Syncing
        //Overwriting local with cloud
        db.collection("Event").get().addOnCompleteListener(task ->{
           Events.setValue((ArrayList<Event>) task.getResult().toObjects(Event.class));
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
        //Adding directly to server
        db.collection(ItemToSave.getClass().getSimpleName()).document(ItemToSave.toString()).set(ItemToSave);

        //Adding to local
        if (ItemToSave.getClass() == Event.class){
            Objects.requireNonNull(Events.getValue()).add((Event) ItemToSave);
        }
        else if (ItemToSave.getClass() == Profile.class){
            Objects.requireNonNull(Profiles.getValue()).add((Profile) ItemToSave);
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
}
