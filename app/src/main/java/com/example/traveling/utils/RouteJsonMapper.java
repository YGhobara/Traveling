package com.example.traveling.utils;

import com.example.traveling.local.SavedRouteEntity;
import com.example.traveling.models.RouteOption;
import com.example.traveling.models.RouteStep;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RouteJsonMapper {

    public static SavedRouteEntity toEntity(RouteOption routeOption, String destination) throws JSONException {
        SavedRouteEntity entity = new SavedRouteEntity();

        entity.setTitle(routeOption.getTitle());
        entity.setDestination(destination);
        entity.setType(routeOption.getType());
        entity.setSummary(routeOption.getSummary());
        entity.setEstimatedBudget(routeOption.getEstimatedBudget());
        entity.setEstimatedDurationMinutes(routeOption.getEstimatedDurationMinutes());
        entity.setEffortLevel(routeOption.getEffortLevel());
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setLiked(routeOption.isLiked());
        entity.setRouteJson(toJson(routeOption).toString());

        return entity;
    }

    public static RouteOption fromEntity(SavedRouteEntity entity) throws JSONException {
        RouteOption routeOption = fromJson(new JSONObject(entity.getRouteJson()));
        routeOption.setLiked(entity.isLiked());
        routeOption.setSaved(true);
        return routeOption;
    }

    public static JSONObject toJson(RouteOption routeOption) throws JSONException {
        JSONObject object = new JSONObject();

        object.put("title", routeOption.getTitle());
        object.put("type", routeOption.getType());
        object.put("summary", routeOption.getSummary());
        object.put("estimatedBudget", routeOption.getEstimatedBudget());
        object.put("estimatedDurationMinutes", routeOption.getEstimatedDurationMinutes());
        object.put("effortLevel", routeOption.getEffortLevel());
        object.put("weatherAdvice", routeOption.getWeatherAdvice());
        object.put("liked", routeOption.isLiked());
        object.put("saved", routeOption.isSaved());

        JSONArray stepsArray = new JSONArray();

        if (routeOption.getSteps() != null) {
            for (RouteStep step : routeOption.getSteps()) {
                JSONObject stepObject = new JSONObject();

                stepObject.put("dayNumber", step.getDayNumber());
                stepObject.put("name", step.getName());
                stepObject.put("category", step.getCategory());
                stepObject.put("description", step.getDescription());
                stepObject.put("period", step.getPeriod());
                stepObject.put("estimatedDurationMinutes", step.getEstimatedDurationMinutes());
                stepObject.put("estimatedCost", step.getEstimatedCost());
                stepObject.put("latitude", step.getLatitude());
                stepObject.put("longitude", step.getLongitude());
                stepObject.put("travelToNextMinutes", step.getTravelToNextMinutes());
                stepObject.put("travelToNextMode", step.getTravelToNextMode());

                stepsArray.put(stepObject);
            }
        }

        object.put("steps", stepsArray);

        return object;
    }

    public static RouteOption fromJson(JSONObject object) throws JSONException {
        RouteOption routeOption = new RouteOption();

        routeOption.setTitle(object.optString("title"));
        routeOption.setType(object.optString("type"));
        routeOption.setSummary(object.optString("summary"));
        routeOption.setEstimatedBudget(object.optDouble("estimatedBudget"));
        routeOption.setEstimatedDurationMinutes(object.optInt("estimatedDurationMinutes"));
        routeOption.setEffortLevel(object.optString("effortLevel"));
        routeOption.setWeatherAdvice(object.optString("weatherAdvice"));
        routeOption.setLiked(object.optBoolean("liked", false));
        routeOption.setSaved(object.optBoolean("saved", false));

        JSONArray stepsArray = object.optJSONArray("steps");
        List<RouteStep> steps = new ArrayList<>();

        if (stepsArray != null) {
            for (int i = 0; i < stepsArray.length(); i++) {
                JSONObject stepObject = stepsArray.getJSONObject(i);

                RouteStep step = new RouteStep();
                step.setDayNumber(stepObject.optInt("dayNumber", 1));
                step.setName(stepObject.optString("name"));
                step.setCategory(stepObject.optString("category"));
                step.setDescription(stepObject.optString("description"));
                step.setPeriod(stepObject.optString("period"));
                step.setEstimatedDurationMinutes(stepObject.optInt("estimatedDurationMinutes"));
                step.setEstimatedCost(stepObject.optDouble("estimatedCost"));
                step.setLatitude(stepObject.optDouble("latitude"));
                step.setLongitude(stepObject.optDouble("longitude"));
                step.setTravelToNextMinutes(stepObject.optInt("travelToNextMinutes"));
                step.setTravelToNextMode(stepObject.optString("travelToNextMode"));

                steps.add(step);
            }
        }

        routeOption.setSteps(steps);

        return routeOption;
    }
}