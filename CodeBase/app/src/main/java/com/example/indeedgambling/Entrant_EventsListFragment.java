package com.example.indeedgambling;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class Entrant_EventsListFragment extends Fragment {
    private EventsAdapter adapter;
    private FirebaseFirestore db;

    public Entrant_EventsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_events_list, container, false);

        RecyclerView rv = v.findViewById(R.id.events_recycler);
        EditText search = v.findViewById(R.id.search_box);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventsAdapter(e -> { /* TODO: open details/join */ });
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadEvents(null);

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                loadEvents(q.isEmpty()? null : q);
            }
        });

        return v;
    }

    private void loadEvents(String categoryQuery) {
        CollectionReference ref = db.collection("events");
        Query q = (categoryQuery == null) ? ref : ref.whereEqualTo("category", categoryQuery);

        q.get().addOnSuccessListener(snap -> {
            List<Event> list = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                Event e = d.toObject(Event.class);
                if (e != null) {
                    if (e.getEventId() == null) e.setEventId(d.getId());
                    list.add(e);
                }
            }
            adapter.setData(list);
        });
    }
}
