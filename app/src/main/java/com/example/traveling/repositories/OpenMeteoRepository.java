package com.example.traveling.repositories;

import android.os.Handler;
import android.os.Looper;

import com.example.traveling.models.WeatherForecastSummary;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class OpenMeteoRepository {

    public interface WeatherCallback {
        void onSuccess(WeatherForecastSummary summary);
        void onError(Exception exception);
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void getDailyForecast(double latitude,
                                 double longitude,
                                 String startDate,
                                 int durationDays,
                                 WeatherCallback callback) {
        if (durationDays <= 0) {
            durationDays = 1;
        }

        int finalDurationDays = durationDays;

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                String urlString = String.format(
                        Locale.US,
                        "https://api.open-meteo.com/v1/forecast" +
                                "?latitude=%.6f" +
                                "&longitude=%.6f" +
                                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                                "&timezone=auto" +
                                "&start_date=%s" +
                                "&end_date=%s",
                        latitude,
                        longitude,
                        startDate,
                        calculateEndDate(startDate, finalDurationDays)
                );

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);

                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IllegalStateException("Open-Meteo error: HTTP " + responseCode);
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

                WeatherForecastSummary summary = parseWeather(responseBuilder.toString());

                mainHandler.post(() -> callback.onSuccess(summary));

            } catch (Exception exception) {
                mainHandler.post(() -> callback.onError(exception));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private WeatherForecastSummary parseWeather(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONObject daily = root.getJSONObject("daily");

        JSONArray dates = daily.getJSONArray("time");
        JSONArray weatherCodes = daily.getJSONArray("weather_code");
        JSONArray maxTemps = daily.getJSONArray("temperature_2m_max");
        JSONArray minTemps = daily.getJSONArray("temperature_2m_min");
        JSONArray precipitation = daily.getJSONArray("precipitation_probability_max");

        WeatherForecastSummary summary = new WeatherForecastSummary();
        summary.setAvailable(true);

        StringBuilder textBuilder = new StringBuilder();

        for (int i = 0; i < dates.length(); i++) {
            WeatherForecastSummary.WeatherDay day = new WeatherForecastSummary.WeatherDay();

            String date = dates.getString(i);
            int weatherCode = weatherCodes.optInt(i);
            double maxTemp = maxTemps.optDouble(i);
            double minTemp = minTemps.optDouble(i);
            double rainProbability = precipitation.optDouble(i);

            String description = describeWeatherCode(weatherCode);

            day.setDate(date);
            day.setMinTemperature(minTemp);
            day.setMaxTemperature(maxTemp);
            day.setPrecipitationProbability(rainProbability);
            day.setWeatherDescription(description);

            summary.getDays().add(day);

            textBuilder.append("Jour ")
                    .append(i + 1)
                    .append(" (")
                    .append(date)
                    .append(") : ")
                    .append(description)
                    .append(", ")
                    .append(String.format(Locale.FRANCE, "%.0f°C à %.0f°C", minTemp, maxTemp))
                    .append(", probabilité de pluie ")
                    .append(String.format(Locale.FRANCE, "%.0f%%", rainProbability))
                    .append(".\n");
        }

        summary.setSummaryText(textBuilder.toString().trim());

        return summary;
    }

    private String calculateEndDate(String startDate, int durationDays) throws Exception {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
        java.util.Calendar calendar = java.util.Calendar.getInstance();

        calendar.setTime(formatter.parse(startDate));
        calendar.add(java.util.Calendar.DAY_OF_MONTH, durationDays - 1);

        return formatter.format(calendar.getTime());
    }

    private String describeWeatherCode(int code) {
        if (code == 0) return "ciel dégagé";
        if (code == 1 || code == 2 || code == 3) return "partiellement nuageux";
        if (code == 45 || code == 48) return "brouillard";
        if (code >= 51 && code <= 57) return "bruine";
        if (code >= 61 && code <= 67) return "pluie";
        if (code >= 71 && code <= 77) return "neige";
        if (code >= 80 && code <= 82) return "averses";
        if (code >= 95 && code <= 99) return "orage";

        return "météo variable";
    }
}