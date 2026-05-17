package com.example.traveling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.models.RouteOption;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RouteOptionAdapter extends RecyclerView.Adapter<RouteOptionAdapter.RouteOptionViewHolder> {

    public interface OnRouteClickListener {
        void onRouteClick(RouteOption routeOption);
    }

    private final List<RouteOption> routes = new ArrayList<>();
    private final OnRouteClickListener listener;

    public RouteOptionAdapter(OnRouteClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<RouteOption> newRoutes) {
        routes.clear();

        if (newRoutes != null) {
            routes.addAll(newRoutes);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RouteOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_option, parent, false);

        return new RouteOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteOptionViewHolder holder, int position) {
        RouteOption route = routes.get(position);

        holder.textRouteTitle.setText(route.getTitle());
        holder.textRouteType.setText(formatRouteType(route.getType()));
        holder.textRouteSummary.setText(route.getSummary());

        holder.textRouteBudget.setText(String.format(Locale.FRANCE, "%.0f €", route.getEstimatedBudget()));
        holder.textRouteDuration.setText(formatDuration(route.getEstimatedDurationMinutes()));
        holder.textRouteEffort.setText(route.getEffortLevel());

        holder.buttonViewRouteDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRouteClick(route);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRouteClick(route);
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

    static class RouteOptionViewHolder extends RecyclerView.ViewHolder {

        TextView textRouteTitle;
        TextView textRouteType;
        TextView textRouteSummary;
        TextView textRouteBudget;
        TextView textRouteDuration;
        TextView textRouteEffort;
        MaterialButton buttonViewRouteDetails;

        public RouteOptionViewHolder(@NonNull View itemView) {
            super(itemView);

            textRouteTitle = itemView.findViewById(R.id.textRouteTitle);
            textRouteType = itemView.findViewById(R.id.textRouteType);
            textRouteSummary = itemView.findViewById(R.id.textRouteSummary);
            textRouteBudget = itemView.findViewById(R.id.textRouteBudget);
            textRouteDuration = itemView.findViewById(R.id.textRouteDuration);
            textRouteEffort = itemView.findViewById(R.id.textRouteEffort);
            buttonViewRouteDetails = itemView.findViewById(R.id.buttonViewRouteDetails);
        }
    }
}