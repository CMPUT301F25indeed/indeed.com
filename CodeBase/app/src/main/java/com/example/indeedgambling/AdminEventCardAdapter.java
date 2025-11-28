package com.example.indeedgambling;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class AdminEventCardAdapter extends ArrayAdapter<Event> {

    private final FirebaseViewModel firebaseVM;

    public AdminEventCardAdapter(@NonNull Context ctx,
                                 @NonNull List<Event> items,
                                 @NonNull FirebaseViewModel vm) {
        super(ctx, 0, items);
        this.firebaseVM = vm;
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.admin_event_card_item, parent, false);
        }

        Event event = getItem(position);
        ImageView poster = convertView.findViewById(R.id.eventPosterImage);
        TextView title   = convertView.findViewById(R.id.eventTitleText);

        // title
        if (event != null && event.getEventName() != null) {
            title.setText(event.getEventName());
        } else {
            title.setText("(Unknown event)");
        }

        // default gray background
        poster.setImageBitmap(null);
        poster.setBackgroundColor(0xFFEEEEEE);

        if (event == null) return convertView;

        String imageId = event.getImageUrl();
        if (!TextUtils.isEmpty(imageId)) {
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
                                    poster.setBackgroundColor(0x00000000);
                                    poster.setImageBitmap(bmp);
                                } catch (Exception ignored) {
                                    // keep gray placeholder
                                }
                            }
                        }
                    });
        }

        return convertView;
    }
}
