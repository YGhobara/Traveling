package com.example.traveling.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String id;
    private String name;
    private String description;
    private String ownerId;
    private String ownerName;
    private Timestamp createdAt;
    private boolean publicGroup;
    private List<String> memberIds;
    private int memberCount;

    public Group() {
        // Required empty constructor for Firestore
    }

    public Group(String id, String name, String description, String ownerId, String ownerName,
                 Timestamp createdAt, boolean publicGroup, List<String> memberIds, int memberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.createdAt = createdAt;
        this.publicGroup = publicGroup;
        this.memberIds = memberIds;
        this.memberCount = memberCount;
    }

    public static Group createNew(String name, String description, String ownerId, String ownerName, boolean publicGroup) {
        List<String> members = new ArrayList<>();
        members.add(ownerId);

        return new Group(
                null,
                name,
                description,
                ownerId,
                ownerName,
                Timestamp.now(),
                publicGroup,
                members,
                1
        );
    }

    @Exclude
    public boolean isMember(String userId) {
        return memberIds != null && memberIds.contains(userId);
    }

    @Exclude
    public boolean isOwner(String userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    @Exclude
    public String getMemberCountText() {
        if (memberCount <= 1) {
            return memberCount + " membre";
        }
        return memberCount + " membres";
    }

    @Exclude
    public String getVisibilityText() {
        return publicGroup ? "Public" : "Privé";
    }

    @Exclude
    public String getDisplayDescription() {
        if (description == null || description.trim().isEmpty()) {
            return "Aucune description";
        }
        return description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPublicGroup() {
        return publicGroup;
    }

    public void setPublicGroup(boolean publicGroup) {
        this.publicGroup = publicGroup;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    @Override
    @Exclude
    public String toString() {
        return name != null ? name : "Groupe";
    }
}