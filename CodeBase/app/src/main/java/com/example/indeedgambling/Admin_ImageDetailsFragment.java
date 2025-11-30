package com.example.indeedgambling;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Admin_ImageDetailsFragment extends Fragment {

    private FirebaseViewModel firebaseVM;

    private String docId;
    private String eventId;
    private String eventName;
    private String imageBase64;
    private String uploaderId;
    private long uploadedAtMillis;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.admin_image_details_fragment, container, false);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        // read args
        if (getArguments() != null) {
            docId            = getArguments().getString("docId");
            eventId          = getArguments().getString("eventId");
            eventName        = getArguments().getString("eventName");
            imageBase64      = getArguments().getString("imageUrl");   // this is BASE64 string
            uploaderId       = getArguments().getString("uploaderId");
            uploadedAtMillis = getArguments().getLong("uploadedAt", -1L);
        }

        ImageView image          = v.findViewById(R.id.admin_image_full);
        TextView title           = v.findViewById(R.id.admin_image_event_name);
        TextView eventIdTxt      = v.findViewById(R.id.admin_image_event_id);
        TextView organizerNameTx = v.findViewById(R.id.admin_image_organizer_name);
        TextView uploaderTxt     = v.findViewById(R.id.admin_image_uploader);
        TextView uploadedTxt     = v.findViewById(R.id.admin_image_uploaded_at);

        Button backBtn   = v.findViewById(R.id.admin_image_back);
        Button removeBtn = v.findViewById(R.id.admin_image_remove);

        // basic text
        title.setText(eventName != null ? eventName : "(Unknown event)");
        eventIdTxt.setText(eventId != null ? eventId : "unknown");

        uploaderTxt.setText(uploaderId != null ? uploaderId : "unknown");

        if (uploadedAtMillis > 0) {
            Date d = new Date(uploadedAtMillis);
            SimpleDateFormat sdf =
                    new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            uploadedTxt.setText(sdf.format(d));

        } else {
            uploadedTxt.setText("unknown");
        }

        // organiser name (profile lookup)
        if (uploaderId != null && !uploaderId.isEmpty()) {
            firebaseVM.getDb().collection("profiles")
                    .document(uploaderId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String name = doc.getString("personName");
                        if (name == null || name.isEmpty()) {
                            organizerNameTx.setText(uploaderId);
                        } else {
                            organizerNameTx.setText(name);

                        }
                    })
                    .addOnFailureListener(err ->
                            organizerNameTx.setText(uploaderId)
                    );
        } else {
            organizerNameTx.setText("unknown");
        }

        // decode base64 image
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                image.setImageBitmap(bitmap);
            } catch (Exception e) {
                image.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }

        backBtn.setOnClickListener(view ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        removeBtn.setOnClickListener(view -> {
            if (docId == null || docId.isEmpty()) {
                Toast.makeText(getContext(), "Missing image id.", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseVM.deleteImageAndClearEventPoster(
                    docId,
                    eventId,
                    () -> {
                        Toast.makeText(getContext(), "Image removed.", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this).navigateUp();
                    },
                    err -> Toast.makeText(
                            getContext(),
                            "Error: " + err.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show()
            );
        });

        return v;
    }
}