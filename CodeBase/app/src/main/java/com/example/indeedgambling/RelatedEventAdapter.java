/**
 * Adapter used to display events that are related to a specific user.
 *
 * This adapter is used inside ProfileDetailsFragment to show:
 * - For organizers: events they created, labeled by event status.
 * - For entrants: events they interacted with, labeled by participation
 *   status such as Accepted, Invited, Waitlisted, Lost, or Cancelled.
 *
 * The adapter:
 * - Filters events so only related events appear
 * - Computes relation type dynamically based on user role
 * - Supports click callbacks for opening event details
 */
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
    private String userId;
    private boolean isOrganizer;
    private OnItemClickListener listener;

    /**
     * Callback used when a related event is clicked.
     * Provides both the event and its relation type.
     */
    public interface OnItemClickListener {
        void onClick(Event event, String relationType);
    }

    /**
     * Registers a callback for item clicks.
     *
     * @param l listener triggered on event tap
     */
    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    /**
     * Sets the user context for filtering.
     *
     * @param userId user profile ID
     * @param isOrganizer true if the profile belongs to an organizer
     */
    public void setUser(String userId, boolean isOrganizer) {
        this.userId = userId;
        this.isOrganizer = isOrganizer;
    }

    /**
     * Updates the event list, filtering out any unrelated events.
     *
     * @param list full list of events from Firestore
     */
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

    @NonNull
    @Override
    public RelatedEventViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_relation, parent, false);
        return new RelatedEventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RelatedEventViewHolder holder,
            int position
    ) {
        Event event = events.get(position);

        String relation = determineRelation(event);
        holder.relationType.setText(relation);

        holder.eventName.setText(
                event.getEventName() != null ? event.getEventName() : "Event"
        );

        holder.eventId.setText(
                event.getEventId() != null ? event.getEventId() : "Unknown ID"
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(event, relation);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Determines the user's relationship with an event.
     *
     * Organizer:
     * - Must match event.organizerId
     * - Displays the event's status as the relation type
     *
     * Entrant:
     * - Checks if the user is in accepted, invited, waiting, lost, or cancelled lists
     *
     * @param e event being evaluated
     * @return String representing relation type, or "None" if unrelated
     */
    private String determineRelation(Event e) {

        if (isOrganizer) {
            if (e.getOrganizerId() == null || !e.getOrganizerId().equals(userId))
                return "None";

            String status = e.getStatus();
            if (status == null || status.trim().isEmpty())
                return "None";

            return status.substring(0, 1).toUpperCase() + status.substring(1);
        }

        if (e.getAcceptedEntrants() != null &&
                e.getAcceptedEntrants().contains(userId))
            return "Accepted";

        if (e.getInvitedList() != null &&
                e.getInvitedList().contains(userId))
            return "Invited";

        if (e.getWaitingList() != null &&
                e.getWaitingList().contains(userId))
            return "Waitlisted";

        if (e.getLostList() != null &&
                e.getLostList().contains(userId))
            return "Lost";

        if (e.getCancelledEntrants() != null &&
                e.getCancelledEntrants().contains(userId))
            return "Cancelled";

        return "None";
    }

    /**
     * ViewHolder for related event rows.
     */
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
