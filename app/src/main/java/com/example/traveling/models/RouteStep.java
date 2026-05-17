package com.example.traveling.models;

import java.io.Serializable;

public class RouteStep implements Serializable {

    private String name;
    private String category;
    private String description;
    private String period; // Morning, Afternoon, Evening
    private int dayNumber;
    private int estimatedDurationMinutes;
    private double estimatedCost;
    private double latitude;
    private double longitude;
    private int travelToNextMinutes;
    private String travelToNextMode;

    public RouteStep() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getTravelToNextMinutes() {
        return travelToNextMinutes;
    }

    public void setTravelToNextMinutes(int travelToNextMinutes) {
        this.travelToNextMinutes = travelToNextMinutes;
    }

    public String getTravelToNextMode() {
        return travelToNextMode;
    }

    public void setTravelToNextMode(String travelToNextMode) {
        this.travelToNextMode = travelToNextMode;
    }
}