package com.example.traveling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.models.Group;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public interface OnJoinClickListener {
        void onJoinClick(Group group);
    }

    private final List<Group> groups = new ArrayList<>();
    private final String currentUserId;
    private final OnGroupClickListener groupClickListener;
    private final OnJoinClickListener joinClickListener;

    public GroupAdapter(String currentUserId,
                        OnGroupClickListener groupClickListener,
                        OnJoinClickListener joinClickListener) {
        this.currentUserId = currentUserId;
        this.groupClickListener = groupClickListener;
        this.joinClickListener = joinClickListener;
    }

    public void submitList(List<Group> newGroups) {
        groups.clear();

        if (newGroups != null) {
            groups.addAll(newGroups);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);

        holder.textGroupName.setText(group.getName());
        holder.textGroupVisibility.setText(group.getVisibilityText());
        holder.textGroupDescription.setText(group.getDisplayDescription());

        String owner = group.getOwnerName() == null || group.getOwnerName().trim().isEmpty()
                ? "un voyageur"
                : group.getOwnerName();

        holder.textGroupMeta.setText(group.getMemberCountText() + " · créé par " + owner);

        boolean isMember = currentUserId != null && group.isMember(currentUserId);

        if (isMember) {
            holder.buttonJoinGroup.setText("Déjà membre");
            holder.buttonJoinGroup.setEnabled(false);
        } else {
            holder.buttonJoinGroup.setText("Rejoindre");
            holder.buttonJoinGroup.setEnabled(currentUserId != null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (groupClickListener != null) {
                groupClickListener.onGroupClick(group);
            }
        });

        holder.buttonJoinGroup.setOnClickListener(v -> {
            if (joinClickListener != null && currentUserId != null && !isMember) {
                joinClickListener.onJoinClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        TextView textGroupName;
        TextView textGroupVisibility;
        TextView textGroupDescription;
        TextView textGroupMeta;
        MaterialButton buttonJoinGroup;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            textGroupName = itemView.findViewById(R.id.textGroupName);
            textGroupVisibility = itemView.findViewById(R.id.textGroupVisibility);
            textGroupDescription = itemView.findViewById(R.id.textGroupDescription);
            textGroupMeta = itemView.findViewById(R.id.textGroupMeta);
            buttonJoinGroup = itemView.findViewById(R.id.buttonJoinGroup);
        }
    }
}