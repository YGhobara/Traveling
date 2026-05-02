package com.example.traveling.models;

public class LocationSuggestion {

    private final String name;
    private final String displayName;
    private final double latitude;
    private final double longitude;
    private final String photonPlaceId;

    public LocationSuggestion(String name,
                              String displayName,
                              double latitude,
                              double longitude,
                              String photonPlaceId) {
        this.name = name;
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photonPlaceId = photonPlaceId;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPhotonPlaceId() {
        return photonPlaceId;
    }

    @Override
    public String toString() {
        return displayName;
    }
}