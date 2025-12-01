package com.example.indeedgambling;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/** Display Adapter for non-admin Array purposes, like in Upcoming Event profile lists.
 */
public class ProfileCardAdapter extends ArrayAdapter<Profile> {

    private final LayoutInflater inflater;
    private final FirebaseViewModel firebaseVM;

    public ProfileCardAdapter(@NonNull Context context,
                              @NonNull FirebaseViewModel firebaseVM,
                              @NonNull List<Profile> profiles
                              ) {
        super(context, 0, profiles);
        this.inflater = LayoutInflater.from(context);
        this.firebaseVM = firebaseVM;
    }

    /** Makes handling view easier
     *
     */
    static class ViewHolder {
        ImageView profilePic;
        TextView name;
        TextView email;
        String boundImageDocId; // avoids wrong image due to recycling
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_profile_listview, parent, false);
            holder = new ViewHolder();
            holder.profilePic = convertView.findViewById(R.id.profile_image);
            holder.name = convertView.findViewById(R.id.profile_name);
            holder.email = convertView.findViewById(R.id.profile_email);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Profile profile = getItem(position);
        if (profile == null) return convertView;

        // Name
        holder.name.setText(profile.getPersonName());
        holder.email.setText(profile.getEmail());

        // Reset image to placeholder
        holder.profilePic.setImageResource(android.R.drawable.ic_menu_report_image);
        holder.boundImageDocId = profile.getProfileImageUrl(); // store ID for recycling

        String imageDocId = profile.getProfileImageUrl();
        if (imageDocId == null || imageDocId.isEmpty()) return convertView;

        // Fetch image from Firestore
        firebaseVM.getDb().collection("images")
                .document(imageDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!imageDocId.equals(holder.boundImageDocId)) return;

                    if (doc != null && doc.exists()) {
                        String base64 = doc.getString("url");
                        if (base64 != null && !base64.isEmpty()) {
                            try {
                                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                holder.profilePic.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                holder.profilePic.setImageResource(android.R.drawable.sym_def_app_icon);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> holder.profilePic.setImageResource(android.R.drawable.sym_def_app_icon));

        return convertView;
    }
}

