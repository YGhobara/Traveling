package com.example.traveling.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoutePreferences implements Serializable {
    private String startDate; // yyyy-MM-dd
    private double destinationLatitude;
    private double destinationLongitude;
    private boolean hasDestinationCoordinates;
    private String destination;
    private List<String> activities = new ArrayList<>();
    private String budgetLevel;
    private int durationDays;
    private String effortLevel;
    private boolean avoidRain;
    private boolean avoidHeat;
    private boolean avoidCold;
    private boolean avoidHumidity;
    private String preferredSeason;
    private List<String> mustSeePlaces = new ArrayList<>();

    public RoutePreferences() {
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public boolean hasDestinationCoordinates() {
        return hasDestinationCoordinates;
    }

    public void setHasDestinationCoordinates(boolean hasDestinationCoordinates) {
        this.hasDestinationCoordinates = hasDestinationCoordinates;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public String getBudgetLevel() {
        return budgetLevel;
    }

    public void setBudgetLevel(String budgetLevel) {
        this.budgetLevel = budgetLevel;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getEffortLevel() {
        return effortLevel;
    }

    public void setEffortLevel(String effortLevel) {
        this.effortLevel = effortLevel;
    }

    public boolean isAvoidRain() {
        return avoidRain;
    }

    public void setAvoidRain(boolean avoidRain) {
        this.avoidRain = avoidRain;
    }

    public boolean isAvoidHeat() {
        return avoidHeat;
    }

    public void setAvoidHeat(boolean avoidHeat) {
        this.avoidHeat = avoidHeat;
    }

    public boolean isAvoidCold() {
        return avoidCold;
    }

    public void setAvoidCold(boolean avoidCold) {
        this.avoidCold = avoidCold;
    }

    public boolean isAvoidHumidity() {
        return avoidHumidity;
    }

    public void setAvoidHumidity(boolean avoidHumidity) {
        this.avoidHumidity = avoidHumidity;
    }

    public String getPreferredSeason() {
        return preferredSeason;
    }

    public void setPreferredSeason(String preferredSeason) {
        this.preferredSeason = preferredSeason;
    }

    public List<String> getMustSeePlaces() {
        return mustSeePlaces;
    }

    public void setMustSeePlaces(List<String> mustSeePlaces) {
        this.mustSeePlaces = mustSeePlaces;
    }
}