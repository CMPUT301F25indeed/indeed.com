package com.example.indeedgambling;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.Holder> {
    public interface OnEventClick { void onClick(Event e); }
    private List<Event> data = new ArrayList<>();
    private final OnEventClick click;

    public EventsAdapter(OnEventClick click) { this.click = click; }
    public void setData(List<Event> list) { data = list; notifyDataSetChanged(); }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_event, p, false);
        return new Holder(v);
    }
    @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
        Event e = data.get(pos);
        h.title.setText(e.getEventName());
        h.desc.setText(e.getDescription());
        h.itemView.setOnClickListener(v -> click.onClick(e));
    }
    @Override public int getItemCount() { return data.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView title, desc;
        Holder(View v) {
            super(v);
            title = v.findViewById(R.id.event_title);
            desc  = v.findViewById(R.id.event_desc);
        }
    }
}
