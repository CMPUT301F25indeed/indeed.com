package com.example.indeedgambling;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Admin_ReviewImagesFragment extends Fragment {

    private ListView listView;
    private Button backBtn, removeBtn;
    private SearchView searchView;



    private FirebaseViewModel firebaseVM;

    private final List<ImageRow> allRows = new ArrayList<>();
    private ImageCardAdapter adapter;

    // eventId -> eventName
    private final HashMap<String, String> eventNames = new HashMap<>();

    // small holder for each row
    private static class ImageRow {
        String docId;
        ImageUpload img;
        String eventName;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin_browse_images_fragment, container, false);

        listView   = view.findViewById(R.id.admin_images_list);
        backBtn    = view.findViewById(R.id.admin_images_back);
        removeBtn  = view.findViewById(R.id.admin_images_remove);
        searchView = view.findViewById(R.id.admin_images_search);

        // Make search text white
        int searchTextId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);

        TextView searchText = searchView.findViewById(searchTextId);
        searchText.setTextColor(Color.WHITE);
        searchText.setHintTextColor(Color.GRAY);

        // we delete inside details fragment, so hide this button here
        removeBtn.setVisibility(View.GONE);

        firebaseVM = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        adapter = new ImageCardAdapter(new ArrayList<>());
        listView.setAdapter(adapter);

        // keep event names in a map
        firebaseVM.getEventsLive().observe(getViewLifecycleOwner(), events -> {
            eventNames.clear();
            if (events != null) {
                for (Event e : events) {
                    if (e.getEventId() != null) {
                        eventNames.put(e.getEventId(), e.getEventName());
                    }
                }
            }
            rebuildFiltered(searchView.getQuery() != null ? searchView.getQuery().toString() : "");
        });

        // load all images once
        firebaseVM.getDb()
                .collection("images")
                .get()
                .addOnSuccessListener(snap -> {
                    allRows.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        ImageUpload img = doc.toObject(ImageUpload.class);
                        if (img == null) continue;

                        ImageRow row = new ImageRow();
                        row.docId = doc.getId();
                        row.img = img;

                        String evName = eventNames.get(img.getEventId());
                        row.eventName = evName != null ? evName : "(Unknown event)";

                        allRows.add(row);
                    }
                    rebuildFiltered("");
                })
                .addOnFailureListener(err ->
                        Toast.makeText(getContext(),
                                "Failed to load images: " + err.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );

        // search filter
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                rebuildFiltered(query);
                return true;
            }

            @Override public boolean onQueryTextChange(String newText) {
                rebuildFiltered(newText);
                return true;
            }
        });

        // click → open details fragment
        listView.setOnItemClickListener((parent, v1, position, id) -> {
            ImageRow row = adapter.getItem(position);
            if (row == null || row.img == null) return;

            Bundle args = new Bundle();
            args.putString("docId", row.docId);
            args.putString("eventId", row.img.getEventId());
            args.putString("eventName", row.eventName);
            args.putString("imageUrl", row.img.getUrl());          // base64 string
            args.putString("uploaderId", row.img.getUploaderId());
            long time = row.img.getUploadedAt() != null
                    ? row.img.getUploadedAt().getTime()
                    : -1L;
            args.putLong("uploadedAt", time);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminImageDetailsFragment, args);
        });

        // back button – just go back in nav stack
        backBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        return view;
    }

    private void rebuildFiltered(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<ImageRow> filtered = new ArrayList<>();

        for (ImageRow row : allRows) {
            String eventName = row.eventName != null ? row.eventName.toLowerCase() : "";
            String uploader = (row.img != null && row.img.getUploaderId() != null)
                    ? row.img.getUploaderId().toLowerCase()
                    : "";
            if (q.isEmpty() || eventName.contains(q) || uploader.contains(q)) {
                filtered.add(row);
            }
        }

        adapter.clear();
        adapter.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    /** Adapter using item_admin_image.xml and loading actual image (base64) */
    private class ImageCardAdapter extends ArrayAdapter<ImageRow> {

        ImageCardAdapter(List<ImageRow> items) {
            super(requireContext(), 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_admin_image, parent, false);
            }

            ImageRow row = getItem(position);

            TextView title = convertView.findViewById(R.id.image_event_title);
            ImageView image = convertView.findViewById(R.id.image_thumb);

            String titleText =
                    (row != null && row.eventName != null) ? row.eventName : "(Unknown event)";
            title.setText(titleText);

            // images.url is a BASE64 string, not a network URL
            if (row != null && row.img != null &&
                    row.img.getUrl() != null && !row.img.getUrl().isEmpty()) {
                try {
                    String base64 = row.img.getUrl();
                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    image.setImageBitmap(bitmap);
                } catch (Exception e) {
                    image.setImageDrawable(null);
                    image.setBackgroundColor(0xFFDDDDDD);
                }
            } else {
                image.setImageDrawable(null);
                image.setBackgroundColor(0xFFDDDDDD);
            }

            return convertView;
        }
    }
}
