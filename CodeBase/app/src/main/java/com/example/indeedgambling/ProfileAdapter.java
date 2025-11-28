package com.example.indeedgambling;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<Profile> profiles = new ArrayList<>();
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDelete(Profile profile);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
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
        Profile p = profiles.get(position);

        holder.name.setText(p.getPersonName());
        holder.email.setText(p.getEmail());
        holder.role.setText(p.getRole());

        // DELETE BUTTON HANDLER
        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, role;
        Button deleteBtn;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            name      = itemView.findViewById(R.id.profile_name);
            email     = itemView.findViewById(R.id.profile_email);
            role      = itemView.findViewById(R.id.profile_role);
            deleteBtn = itemView.findViewById(R.id.profile_delete_btn);
        }
    }
}
