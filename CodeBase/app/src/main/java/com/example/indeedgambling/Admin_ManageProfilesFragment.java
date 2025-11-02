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

public class Admin_ManageProfilesFragment extends Fragment {

    private SearchView searchView;
    private ListView listView;
    private Button removeBtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin_browse_profiles_fragment, container, false);

        searchView = view.findViewById(R.id.admin_profiles_search);
        listView = view.findViewById(R.id.admin_profiles_list);
        removeBtn = view.findViewById(R.id.admin_profiles_remove);

        // Remove button (placeholder)
        removeBtn.setOnClickListener(v ->
                Toast.makeText(getContext(), "Remove profile (to implement)", Toast.LENGTH_SHORT).show()
        );

        // Long press list to go back
        listView.setOnItemLongClickListener((parent, v, position, id) -> {
            NavHostFragment.findNavController(this).navigateUp();
            return true;
        });

        return view;
    }
}
