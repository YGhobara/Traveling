package com.example.traveling.repositories;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class UnsplashRepository {

    public interface UnsplashImageCallback {
        void onSuccess(String imageUrl);
        void onError(Exception exception);
    }

    private static final String UNSPLASH_ACCESS_KEY = "i0i_3TKYB-st3BcDi-_WKrieDrk_4D2HU_KR1LG3Gh4";

    private static final String UNSPLASH_SEARCH_URL = "https://api.unsplash.com/search/photos";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void searchImage(String query, int page, UnsplashImageCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Empty Unsplash query"));
            return;
        }

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                String encodedQuery = URLEncoder.encode(query.trim(), "UTF-8");

                String urlString = UNSPLASH_SEARCH_URL
                        + "?query=" + encodedQuery
                        + "&orientation=landscape"
                        + "&per_page=1"
                        + "&page=" + page
                        + "&client_id=" + UNSPLASH_ACCESS_KEY;

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);

                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IllegalStateException("Unsplash error: HTTP " + responseCode);
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

                String imageUrl = parseImageUrl(responseBuilder.toString());

                mainHandler.post(() -> callback.onSuccess(imageUrl));

            } catch (Exception exception) {
                mainHandler.post(() -> callback.onError(exception));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private String parseImageUrl(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray results = root.getJSONArray("results");

        if (results.length() == 0) {
            throw new IllegalStateException("No Unsplash image found");
        }

        JSONObject firstPhoto = results.getJSONObject(0);
        JSONObject urls = firstPhoto.getJSONObject("urls");

        // "regular" is a good compromise for mobile UI.
        return urls.getString("regular");
    }
}