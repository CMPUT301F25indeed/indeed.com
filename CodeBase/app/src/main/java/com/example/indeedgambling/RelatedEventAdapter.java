package com.example.indeedgambling;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RelatedEventAdapter extends RecyclerView.Adapter<RelatedEventAdapter.RelatedEventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private String userId;      // entrant or organizer
    private boolean isOrganizer;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Event event, String relationType);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    public void setUser(String userId, boolean isOrganizer) {
        this.userId = userId;
        this.isOrganizer = isOrganizer;
    }

    /**  ------------ FIXED VERSION - ONLY THIS ONE ------------ */
    public void setEvents(List<Event> list) {
        if (list == null) {
            this.events = new ArrayList<>();
            notifyDataSetChanged();
            return;
        }

        List<Event> filtered = new ArrayList<>();
        for (Event e : list) {
            if (!determineRelation(e).equals("None")) {
                filtered.add(e);
            }
        }

        this.events = filtered;
        notifyDataSetChanged();
    }
    /** --------------------------------------------------------- */


    @NonNull
    @Override
    public RelatedEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_relation, parent, false);
        return new RelatedEventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedEventViewHolder holder, int position) {
        Event event = events.get(position);

        String relation = determineRelation(event);
        holder.relationType.setText(relation);

        holder.eventName.setText(event.getEventName() != null ? event.getEventName() : "Event");
        holder.eventId.setText(event.getEventId() != null ? event.getEventId() : "Unknown ID");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(event, relation);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }


    /**
     * Relation logic:
     * Organizer:
     *   - Must match organizerId
     *   - Shows event.status (Planned/Open/Closed/Completed)
     *
     * Entrant:
     *   - Accepted / Invited / Waitlisted / Lost / Cancelled
     */
    private String determineRelation(Event e) {

        // ---------------- ORGANIZER MODE ----------------
        if (isOrganizer) {

            // If they are NOT the organizer → not related
            if (e.getOrganizerId() == null || !e.getOrganizerId().equals(userId))
                return "None";

            // Use event status for display
            String status = e.getStatus();
            if (status == null || status.trim().isEmpty())
                return "None";

            // Capitalize first letter
            return status.substring(0, 1).toUpperCase() + status.substring(1);
        }


        // ---------------- ENTRANT MODE ----------------
        if (e.getAcceptedEntrants() != null && e.getAcceptedEntrants().contains(userId))
            return "Accepted";

        if (e.getInvitedList() != null && e.getInvitedList().contains(userId))
            return "Invited";

        if (e.getWaitingList() != null && e.getWaitingList().contains(userId))
            return "Waitlisted";

        if (e.getLostList() != null && e.getLostList().contains(userId))
            return "Lost";

        if (e.getCancelledEntrants() != null && e.getCancelledEntrants().contains(userId))
            return "Cancelled";

        return "None";
    }


    static class RelatedEventViewHolder extends RecyclerView.ViewHolder {
        TextView relationType, eventName, eventId;

        public RelatedEventViewHolder(@NonNull View itemView) {
            super(itemView);
            relationType = itemView.findViewById(R.id.relation_type);
            eventName    = itemView.findViewById(R.id.relation_event_name);
            eventId      = itemView.findViewById(R.id.relation_event_id);
        }
    }
}
