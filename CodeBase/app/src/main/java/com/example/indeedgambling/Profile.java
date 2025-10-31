package com.example.indeedgambling;

import android.util.Log;

import androidx.annotation.NonNull;

public class Profile {
    public String profileName;
    private String password;

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Profile(String password, String profileName) {
        this.password = password;
        this.profileName = profileName;
    }

    //No arg constructor for Firebase
    public Profile(){}


    /** Overriding hashcode() to add the hash of profileName and password together
     *  Does not return a unique value, as all hashes do.
     * @return
     */
    @Override
    public int hashCode(){
        //If same sign, subtract.
        //Opposite sign, add.
        // This should ensure we are within the valid int range.
        int ProfHash = this.profileName.hashCode();
        int PassHash = this.password.hashCode();

        //Same sign
        if (ProfHash * PassHash > 0){
            return ProfHash - PassHash;
        } else{
            return ProfHash + PassHash;
        }
    }

    //TODO: Equals function to use in Firebase Contains()


    /** Ovverride of toString() that returns the profile name.
     * @return Profilename
     */
    @NonNull
    @Override
    public String toString(){
        return this.profileName;
    }
}
