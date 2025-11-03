package com.example.indeedgambling;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventHolder> {

    private List<Event> list = new ArrayList<>();
    private final OnEventClick listener;

    public interface OnEventClick {
        void clicked(Event e);
    }

    public EventsAdapter(OnEventClick listener) {
        this.listener = listener;
    }

    public void setData(List<Event> events) {
        this.list = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        Event e = list.get(position);
        holder.title.setText(e.getEventName());
        holder.desc.setText(e.getDescription());
        holder.itemView.setOnClickListener(v -> listener.clicked(e));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class EventHolder extends RecyclerView.ViewHolder {
        TextView title, desc;

        public EventHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            desc = itemView.findViewById(R.id.event_desc);
        }
    }
}
