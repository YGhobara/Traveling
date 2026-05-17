package com.example.traveling.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WeatherForecastSummary implements Serializable {

    private boolean available;
    private String summaryText;
    private List<WeatherDay> days = new ArrayList<>();

    public WeatherForecastSummary() {
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public List<WeatherDay> getDays() {
        return days;
    }

    public void setDays(List<WeatherDay> days) {
        this.days = days;
    }

    public static class WeatherDay implements Serializable {

        private String date;
        private double minTemperature;
        private double maxTemperature;
        private double precipitationProbability;
        private String weatherDescription;

        public WeatherDay() {
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public double getMinTemperature() {
            return minTemperature;
        }

        public void setMinTemperature(double minTemperature) {
            this.minTemperature = minTemperature;
        }

        public double getMaxTemperature() {
            return maxTemperature;
        }

        public void setMaxTemperature(double maxTemperature) {
            this.maxTemperature = maxTemperature;
        }

        public double getPrecipitationProbability() {
            return precipitationProbability;
        }

        public void setPrecipitationProbability(double precipitationProbability) {
            this.precipitationProbability = precipitationProbability;
        }

        public String getWeatherDescription() {
            return weatherDescription;
        }

        public void setWeatherDescription(String weatherDescription) {
            this.weatherDescription = weatherDescription;
        }
    }
}