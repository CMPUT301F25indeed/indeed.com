package com.example.indeedgambling;

import android.os.Bundle;
import android.view.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.widget.Button;

public class Admin_ReviewImagesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_browse_images_fragment, container, false);

        Button back = view.findViewById(R.id.admin_images_back);
        back.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_admin_to_logs));

        return view;
    }
}
