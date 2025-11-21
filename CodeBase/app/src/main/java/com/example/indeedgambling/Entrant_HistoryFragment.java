package com.example.indeedgambling;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class Entrant_HistoryFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private EntrantViewModel entrantVM;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_history_fragment, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        entrantVM = new ViewModelProvider(requireActivity()).get(EntrantViewModel.class);

        Button home = view.findViewById(R.id.entrant_home_button_history);
        ListView listView = view.findViewById(R.id.entrant_activity_history);

        home.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrant_HistoryFragment_to_entrantHomeFragment));

        // TODO: later set adapter when Firebase ready
        // listView.setAdapter(historyAdapter);

        return view;
    }
}
