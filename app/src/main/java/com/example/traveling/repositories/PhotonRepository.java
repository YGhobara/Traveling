package com.example.traveling.repositories;

import android.os.Handler;
import android.os.Looper;

import com.example.traveling.models.LocationSuggestion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class PhotonRepository {

    public interface OnLocationSuggestionsLoadedListener {
        void onSuccess(List<LocationSuggestion> suggestions);
        void onError(Exception exception);
    }

    private static final String PHOTON_BASE_URL = "https://photon.komoot.io/api/";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void searchLocations(String query, OnLocationSuggestionsLoadedListener listener) {
        if (query == null || query.trim().length() < 3) {
            listener.onSuccess(new ArrayList<>());
            return;
        }

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                String encodedQuery = URLEncoder.encode(query.trim(), "UTF-8");

                String urlString = PHOTON_BASE_URL
                        + "?q=" + encodedQuery
                        + "&limit=5"
                        + "&lang=fr";

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);

                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IllegalStateException("Photon error: HTTP " + responseCode);
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder responseBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                reader.close();

                List<LocationSuggestion> suggestions = parseSuggestions(responseBuilder.toString());

                mainHandler.post(() -> listener.onSuccess(suggestions));

            } catch (Exception exception) {
                mainHandler.post(() -> listener.onError(exception));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private List<LocationSuggestion> parseSuggestions(String json) throws Exception {
        List<LocationSuggestion> suggestions = new ArrayList<>();

        JSONObject root = new JSONObject(json);
        JSONArray features = root.getJSONArray("features");

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);

            JSONObject properties = feature.getJSONObject("properties");
            JSONObject geometry = feature.getJSONObject("geometry");
            JSONArray coordinates = geometry.getJSONArray("coordinates");

            double longitude = coordinates.getDouble(0);
            double latitude = coordinates.getDouble(1);

            String name = properties.optString("name", "Lieu");
            String city = properties.optString("city", "");
            String country = properties.optString("country", "");
            String osmId = properties.optString("osm_id", "");

            String displayName = buildDisplayName(name, city, country);

            suggestions.add(new LocationSuggestion(
                    name,
                    displayName,
                    latitude,
                    longitude,
                    osmId
            ));
        }

        return suggestions;
    }

    private String buildDisplayName(String name, String city, String country) {
        StringBuilder builder = new StringBuilder();

        if (name != null && !name.trim().isEmpty()) {
            builder.append(name.trim());
        }

        if (city != null && !city.trim().isEmpty()
                && !city.trim().equalsIgnoreCase(name)) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(city.trim());
        }

        if (country != null && !country.trim().isEmpty()) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(country.trim());
        }

        return builder.length() > 0 ? builder.toString() : "Lieu inconnu";
    }
}