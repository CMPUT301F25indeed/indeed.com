package com.example.indeedgambling;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Custom adapter for entrant history list.
 * Displays event name, description, and the entrant's status.
 */
public class HistoryAdapter extends ArrayAdapter<Event> {

    private final LayoutInflater inflater;
    private final List<Event> eventList;

    public HistoryAdapter(@NonNull Context context, @NonNull List<Event> events) {
        super(context, 0, events);
        this.inflater = LayoutInflater.from(context);
        this.eventList = events;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_history, parent, false);
        }

        Event event = eventList.get(position);

        TextView title = convertView.findViewById(R.id.history_event_title);
        TextView desc = convertView.findViewById(R.id.history_event_desc);
        TextView date = convertView.findViewById(R.id.history_event_date);
        TextView status = convertView.findViewById(R.id.history_event_status);

        title.setText(event.getEventName());
        desc.setText(event.getDescription());

        // *** Format event dates
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        date.setText("From " + sdf.format(event.getEventStart()) + " to " + sdf.format(event.getEventEnd()));

        // *** Derive entrant status
        String entrantId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        String entryStatus = "Not Selected";

        if (event.getAcceptedEntrants().contains(entrantId))
            entryStatus = "Accepted";
        else if (event.getInvitedList().contains(entrantId))
            entryStatus = "Invited";
        else if (event.getCancelledEntrants().contains(entrantId))
            entryStatus = "Rejected";
        else if (event.getWaitingList().contains(entrantId))
            entryStatus = "Registered (Waiting)";

        status.setText("Status: " + entryStatus);
        return convertView;
    }
}
