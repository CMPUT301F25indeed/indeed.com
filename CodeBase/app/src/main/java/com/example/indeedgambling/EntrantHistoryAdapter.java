package com.example.indeedgambling;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 *
 */
public class EntrantHistoryAdapter extends ArrayAdapter<Event> {

    private final String entrantId;

    /**
     *
     */
    public EntrantHistoryAdapter(@NonNull Context context,
                                 @NonNull List<Event> events,
                                 @NonNull String entrantId) {
        super(context, 0, events);
        this.entrantId = entrantId;
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_history, parent, false);
        }

        Event e = getItem(position);
        if (e == null) {
            return v;
        }

        TextView title = v.findViewById(R.id.history_event_title);
        TextView status = v.findViewById(R.id.history_event_status);

        title.setText(e.getEventName());

        String which = e.whichList(entrantId);
        String label;
        switch (which) {
            case "invited":
                label = "Invited";
                break;
            case "accepted":
                label = "Accepted";
                break;
            case "cancelled":
                label = "Not selected (lost lottery)";
                break;
            case "waitlist":
                label = "Waitlisted";
                break;
            default:
                label = "Joined";
                break;
        }
        status.setText(label);

        return v;
    }
}
