package com.example.traveling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.local.SavedRouteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SavedRouteAdapter extends RecyclerView.Adapter<SavedRouteAdapter.SavedRouteViewHolder> {

    public interface OnSavedRouteClickListener {
        void onSavedRouteClick(SavedRouteEntity savedRoute);
    }

    private final List<SavedRouteEntity> routes = new ArrayList<>();
    private final OnSavedRouteClickListener listener;

    public SavedRouteAdapter(OnSavedRouteClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SavedRouteEntity> newRoutes) {
        routes.clear();

        if (newRoutes != null) {
            routes.addAll(newRoutes);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavedRouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_route, parent, false);

        return new SavedRouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedRouteViewHolder holder, int position) {
        SavedRouteEntity route = routes.get(position);

        holder.textTitle.setText(route.getTitle());
        holder.textDestination.setText(route.getDestination());
        holder.textType.setText(formatRouteType(route.getType()));
        holder.textSummary.setText(route.getSummary());

        holder.textBudget.setText(String.format(Locale.FRANCE, "%.0f €", route.getEstimatedBudget()));
        holder.textDuration.setText(formatDuration(route.getEstimatedDurationMinutes()));
        holder.textEffort.setText(route.getEffortLevel());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSavedRouteClick(route);
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    private String formatRouteType(String type) {
        if (type == null) {
            return "Parcours";
        }

        switch (type) {
            case "ECONOMIC":
                return "Économique";
            case "BALANCED":
                return "Équilibré";
            case "COMFORT":
                return "Confort";
            default:
                return type;
        }
    }

    private String formatDuration(int minutes) {
        if (minutes <= 0) {
            return "-";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours <= 0) {
            return remainingMinutes + " min";
        }

        if (remainingMinutes == 0) {
            return hours + "h";
        }

        return hours + "h" + remainingMinutes;
    }

    static class SavedRouteViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle;
        TextView textDestination;
        TextView textType;
        TextView textSummary;
        TextView textBudget;
        TextView textDuration;
        TextView textEffort;

        public SavedRouteViewHolder(@NonNull View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.textSavedRouteTitle);
            textDestination = itemView.findViewById(R.id.textSavedRouteDestination);
            textType = itemView.findViewById(R.id.textSavedRouteType);
            textSummary = itemView.findViewById(R.id.textSavedRouteSummary);
            textBudget = itemView.findViewById(R.id.textSavedRouteBudget);
            textDuration = itemView.findViewById(R.id.textSavedRouteDuration);
            textEffort = itemView.findViewById(R.id.textSavedRouteEffort);
        }
    }
}