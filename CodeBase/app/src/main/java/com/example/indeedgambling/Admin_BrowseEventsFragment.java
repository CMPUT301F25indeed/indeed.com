package com.example.indeedgambling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class Admin_BrowseEventsFragment extends Fragment {

    private SearchView searchView;
    private ListView listView;
    private Button removeBtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin_browse_events_fragment, container, false);

        searchView = view.findViewById(R.id.admin_events_search);
        listView = view.findViewById(R.id.admin_events_list);
        removeBtn = view.findViewById(R.id.admin_events_remove);

        // temp test click
        removeBtn.setOnClickListener(v ->
                Toast.makeText(getContext(), "Remove event (to implement)", Toast.LENGTH_SHORT).show()
        );

        // optional: back on long press (temp)
        listView.setOnItemLongClickListener((parent, v, position, id) -> {
            NavHostFragment.findNavController(this)
                    .navigateUp();
            return true;
        });

        return view;
    }
}
