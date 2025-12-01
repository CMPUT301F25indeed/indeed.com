/**
 * Adapter for displaying user profiles in a RecyclerView.
 *
 * This adapter is used by the Admin interface to display all system profiles.
 * Each row shows:
 * - Profile image (loaded from Firestore Base64)
 * - Name, email, and role
 * - Optional delete button (hidden for admin accounts)
 *
 * Features:
 * - Supports row click via OnItemClickListener
 * - Supports delete action via OnDeleteClickListener
 * - Uses efficient image decoding for Base64 images stored in Firestore
 */
package com.example.indeedgambling;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<Profile> profiles = new ArrayList<>();
    private final FirebaseViewModel firebaseVM;

    private OnDeleteClickListener deleteListener;
    private OnItemClickListener itemClickListener;

    /**
     * Listener for delete button events.
     */
    public interface OnDeleteClickListener {
        void onDelete(Profile profile);
    }

    /**
     * Listener for general row click.
     */
    public interface OnItemClickListener {
        void onItemClick(Profile profile);
    }

    /**
     * Creates a Profile adapter.
     *
     * @param firebaseVM ViewModel used for retrieving Base64 profile images
     */
    public ProfileAdapter(FirebaseViewModel firebaseVM) {
        this.firebaseVM = firebaseVM;
    }

    /**
     * Sets the listener for deletion events.
     */
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    /**
     * Sets the listener for row click events.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /**
     * Updates the list of profiles shown in the RecyclerView.
     */
    public void setProfiles(List<Profile> newProfiles) {
        this.profiles = newProfiles;
        notifyDataSetChanged();
    }

    /**
     * Inflates a profile row layout and creates a ViewHolder.
     */
    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    /**
     * Binds profile data (name, email, role, image) to a ViewHolder row.
     */
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profiles.get(position);

        holder.name.setText(profile.getPersonName());
        holder.email.setText(profile.getEmail());
        holder.role.setText(profile.getRole());

        String imageId = profile.getProfileImageUrl();

        if (imageId != null && !imageId.isEmpty()) {
            firebaseVM.getDb()
                    .collection("images")
                    .document(imageId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            String base64 = doc.getString("url");
                            if (base64 != null && !base64.isEmpty()) {
                                try {
                                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                    holder.profileImage.setImageBitmap(bitmap);
                                } catch (Exception e) {
                                    holder.profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
                                }
                            } else {
                                holder.profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
                            }
                        } else {
                            holder.profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
                        }
                    })
                    .addOnFailureListener(e ->
                            holder.profileImage.setImageResource(android.R.drawable.ic_menu_report_image)
                    );
        } else {
            holder.profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(profile);
            }
        });

        if (profile.getRole().equalsIgnoreCase("admin")) {
            holder.deleteBtn.setVisibility(View.GONE);
        } else {
            holder.deleteBtn.setVisibility(View.VISIBLE);
        }

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(profile);
            }
        });
    }

    /**
     * Returns total number of profiles.
     */
    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * Holds all views for a profile list row.
     */
    static class ProfileViewHolder extends RecyclerView.ViewHolder {

        TextView name, email, role;
        ImageView profileImage;
        Button deleteBtn;

        /**
         * Initializes view references for a profile row.
         */
        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profile_image);
            name         = itemView.findViewById(R.id.profile_name);
            email        = itemView.findViewById(R.id.profile_email);
            role         = itemView.findViewById(R.id.profile_role);
            deleteBtn    = itemView.findViewById(R.id.profile_delete_btn);
        }
    }
}
