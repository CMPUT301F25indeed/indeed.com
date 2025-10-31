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
        db.collection("events").get().addOnCompleteListener(task ->{
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


    /** Saves the object under the document with the Class name
     * @param InstanceName Save Name of the object
     * @param ItemToSave Object to be saved
     */
    public void Add(String InstanceName, Object ItemToSave){
        db.collection(ItemToSave.getClass().getSimpleName()).document(InstanceName).set(ItemToSave);
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

    public ArrayList<Event> getEvents(){
        return Events.getValue();
    }

    public ArrayList<Profile> getProfiles(){
        return Profiles.getValue();
    }


}
