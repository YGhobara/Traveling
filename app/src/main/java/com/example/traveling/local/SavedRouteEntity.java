package com.example.traveling.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saved_routes")
public class SavedRouteEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String destination;
    private String type;
    private String summary;
    private double estimatedBudget;
    private int estimatedDurationMinutes;
    private String effortLevel;
    private long createdAt;
    private String routeJson;
    private boolean liked;

    public SavedRouteEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public double getEstimatedBudget() {
        return estimatedBudget;
    }

    public void setEstimatedBudget(double estimatedBudget) {
        this.estimatedBudget = estimatedBudget;
    }

    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public String getEffortLevel() {
        return effortLevel;
    }

    public void setEffortLevel(String effortLevel) {
        this.effortLevel = effortLevel;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getRouteJson() {
        return routeJson;
    }

    public void setRouteJson(String routeJson) {
        this.routeJson = routeJson;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}