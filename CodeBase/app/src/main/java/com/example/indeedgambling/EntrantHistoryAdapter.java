package com.example.indeedgambling;

import android.content.Context;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class EntrantHistoryAdapter extends ArrayAdapter<Event> {

    public EntrantHistoryAdapter(@NonNull Context context, @NonNull List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.item_history, parent, false);
        }

        Event e = getItem(position);

        TextView title = v.findViewById(R.id.history_event_title);
        TextView status = v.findViewById(R.id.history_event_status);

        title.setText(e.getTitle());
        status.setText("Joined Event"); // later we can pull real status

        return v;
    }
}
