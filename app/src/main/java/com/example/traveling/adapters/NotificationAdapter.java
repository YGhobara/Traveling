package com.example.traveling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private final List<Notification> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Notification> newNotifications) {
        notifications.clear();

        if (newNotifications != null) {
            notifications.addAll(newNotifications);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.textTitle.setText(notification.getTitle());
        holder.textMessage.setText(notification.getMessage());
        holder.textDate.setText(formatRelativeTime(notification.getCreatedAt()));

        holder.viewUnreadDot.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        View viewUnreadDot;
        TextView textTitle;
        TextView textMessage;
        TextView textDate;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
            textTitle = itemView.findViewById(R.id.textNotificationTitle);
            textMessage = itemView.findViewById(R.id.textNotificationMessage);
            textDate = itemView.findViewById(R.id.textNotificationDate);
        }
    }

    private String formatRelativeTime(long timestamp) {
        if (timestamp <= 0) {
            return "date inconnue";
        }

        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long minute = 60 * 1000;
        long hour = 60 * minute;
        long day = 24 * hour;
        long month = 30 * day;
        long year = 365 * day;

        if (diff < minute) {
            return "à l’instant";
        } else if (diff < hour) {
            return "il y a " + (diff / minute) + " min";
        } else if (diff < day) {
            return "il y a " + (diff / hour) + " h";
        } else if (diff < month) {
            return "il y a " + (diff / day) + " j";
        } else if (diff < year) {
            return "il y a " + (diff / month) + " mois";
        } else {
            return "il y a " + (diff / year) + " an(s)";
        }
    }
}