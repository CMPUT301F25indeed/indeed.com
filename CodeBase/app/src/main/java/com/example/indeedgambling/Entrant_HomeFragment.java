package com.example.indeedgambling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Entrant_HomeFragment extends Fragment {
    private FirebaseFirestore db;

    private CollectionReference testRef;
    public Entrant_HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //Testing Firestore
        Log.d("TAG", "onCreateView: FireBase");
        db = FirebaseFirestore.getInstance();
        testRef = db.collection("test");
        DocumentReference docRef = testRef.document("1");
        docRef.set(new Profile("testpass","testprofile")).addOnSuccessListener(aVoid -> Log.d("TAG", "onCreateView: Success")).addOnFailureListener(e -> Log.e("Firestore","Error Moment",e));


        return inflater.inflate(R.layout.entrant_home_fragment, container, false);
    }
}
