package com.example.traveling.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RouteOption implements Serializable {

    private String title;
    private String type; // ECONOMIC, BALANCED, COMFORT
    private String summary;
    private double estimatedBudget;
    private int estimatedDurationMinutes;
    private String effortLevel;
    private String weatherAdvice;
    private String destination;
    private List<RouteStep> steps = new ArrayList<>();
    private boolean liked;
    private boolean saved;

    public RouteOption() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getWeatherAdvice() {
        return weatherAdvice;
    }

    public void setWeatherAdvice(String weatherAdvice) {
        this.weatherAdvice = weatherAdvice;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<RouteStep> getSteps() {
        return steps;
    }

    public void setSteps(List<RouteStep> steps) {
        this.steps = steps;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}