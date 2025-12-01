package com.example.indeedgambling;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.ImageView;
import android.util.Base64;

import com.google.firebase.firestore.DocumentSnapshot;


public class Admin_EventDetailsFragment extends Fragment {

    private FirebaseViewModel firebaseVM;
    private Event event;

    private ImageView posterView;

    private TextView name, desc, loc, dates, reg, status, category, total;
    private Button backBtn, removeBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.admin_event_details_fragment, container, false);



        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        name     = v.findViewById(R.id.event_name);
        desc     = v.findViewById(R.id.event_description);
        loc      = v.findViewById(R.id.event_location);
        dates    = v.findViewById(R.id.event_dates);
        reg      = v.findViewById(R.id.event_registration);
        status   = v.findViewById(R.id.event_status);
        category = v.findViewById(R.id.event_category);
        total    = v.findViewById(R.id.event_total_entrant);
        backBtn  = v.findViewById(R.id.admin_event_back);
        removeBtn= v.findViewById(R.id.admin_event_remove);
        posterView = v.findViewById(R.id.event_poster);

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        if (event != null) {
            bindEvent();
        } else {
            Toast.makeText(getContext(), "Event not found.", Toast.LENGTH_SHORT).show();
        }

        backBtn.setOnClickListener(view1 ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        removeBtn.setOnClickListener(view12 -> {
            if (event == null) return;
            confirmDelete();
        });

        return v;
    }

    private void bindEvent() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

        // reset poster to grey placeholder
        if (posterView != null) {
            posterView.setImageBitmap(null);
            posterView.setBackgroundColor(0xFFEEEEEE);
        }

        // load poster if this event has one
        String imageId = event.getImageUrl();
        if (!TextUtils.isEmpty(imageId) && posterView != null) {
            firebaseVM.getDb().collection("images")
                    .document(imageId)
                    .get()
                    .addOnSuccessListener((DocumentSnapshot doc) -> {
                        if (doc != null && doc.exists()) {
                            String base64 = doc.getString("url");
                            if (!TextUtils.isEmpty(base64)) {
                                try {
                                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                    posterView.setBackgroundColor(0x00000000);
                                    posterView.setImageBitmap(bmp);
                                } catch (Exception ignored) {
                                    // keep grey placeholder if decode fails
                                }
                            }
                        }
                    });
        }


        name.setText(event.getEventName());
        desc.setText(event.getDescription());

        if (event.getLocation() != null) {
            // only value, label is in XML
            loc.setText(event.getLocationString());
        }

        if (event.getCategory() != null) {
            category.setText(event.getCategory());
        }

        if (event.getEventStart() != null && event.getEventEnd() != null) {
            dates.setText(
                    sdf.format(event.getEventStart()) + " – " +
                            sdf.format(event.getEventEnd())
            );
        }

        if (event.getRegistrationStart() != null && event.getRegistrationEnd() != null) {
            reg.setText(
                    sdf.format(event.getRegistrationStart()) + " – " +
                            sdf.format(event.getRegistrationEnd())
            );
        }

        status.setText(event.getStatus());


        if (event.getWaitingList() != null&&event.RegistrationOpen()) {
            total.setText(String.valueOf(event.getWaitingList().size()+"/"+event.getMaxWaitingEntrantsString()));
        } else if (!(event.RegistrationOpen())&&event.getLostList() != null) {
            total.setText(String.valueOf(event.getLostList().size()+"/"+event.getMaxWaitingEntrantsString()));
        }

    }

    private void confirmDelete() {
        if (getContext() == null || event == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Remove event")
                .setMessage("This will delete the event and related data.\nAre you sure?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    firebaseVM.adminDeleteEventAndCleanup(
                            event.getEventId(),
                            () -> {
                                Toast.makeText(getContext(), "Event removed.", Toast.LENGTH_SHORT).show();
                                NavHostFragment.findNavController(this).navigateUp();
                            },
                            err -> Toast.makeText(
                                    getContext(),
                                    "Error removing event: " + (err != null ? err.getMessage() : "unknown"),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
