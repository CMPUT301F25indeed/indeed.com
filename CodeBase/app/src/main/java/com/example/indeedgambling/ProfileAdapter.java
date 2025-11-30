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

    public ProfileAdapter(FirebaseViewModel firebaseVM) {
        this.firebaseVM = firebaseVM;
    }

    public interface OnDeleteClickListener {
        void onDelete(Profile profile);
    }

    public interface OnItemClickListener {
        void onItemClick(Profile profile);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setProfiles(List<Profile> newProfiles) {
        this.profiles = newProfiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profiles.get(position);

        holder.name.setText(profile.getPersonName());
        holder.email.setText(profile.getEmail());
        holder.role.setText(profile.getRole());

        // ---- LOAD PROFILE IMAGE FROM FIREBASE ----
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
                    .addOnFailureListener(e -> {
                        holder.profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
                    });

        } else {
            holder.profileImage.setImageResource(android.R.drawable.ic_menu_report_image);
        }
        // -------- END IMAGE LOADING --------

        // ITEM CLICK
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(profile);
            }
        });

        // DELETE BUTTON VISIBILITY
        if (profile.getRole().equalsIgnoreCase("admin")) {
            holder.deleteBtn.setVisibility(View.GONE);
        } else {
            holder.deleteBtn.setVisibility(View.VISIBLE);
        }

        // DELETE HANDLER
        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(profile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {

        TextView name, email, role;
        ImageView profileImage;
        Button deleteBtn;

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
