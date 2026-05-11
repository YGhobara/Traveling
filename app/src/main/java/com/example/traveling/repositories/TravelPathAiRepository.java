package com.example.traveling.repositories;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.traveling.models.RouteOption;
import com.example.traveling.models.RoutePreferences;
import com.example.traveling.models.RouteStep;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerationConfig;
import com.google.firebase.ai.type.Schema;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.ai.GenerativeModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TravelPathAiRepository {

    public interface GenerateRoutesCallback {
        void onSuccess(List<RouteOption> routes);
        void onError(Exception exception);
    }

    private final GenerativeModelFutures model;
    private final Executor executor;
    private final Handler mainHandler;

    public TravelPathAiRepository() {
        Schema schema = buildRouteSchema();

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.responseMimeType = "application/json";
        configBuilder.responseSchema = schema;

        GenerationConfig generationConfig = configBuilder.build();

        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel(
                        "gemini-3.1-flash-lite",
                        generationConfig
                );

        model = GenerativeModelFutures.from(ai);
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void generateRoutes(@NonNull RoutePreferences preferences,
                               @NonNull GenerateRoutesCallback callback) {
        String prompt = buildPrompt(preferences);

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String json = result.getText();

                    if (json == null || json.trim().isEmpty()) {
                        throw new JSONException("Empty AI response");
                    }

                    List<RouteOption> routes = parseRoutes(json);

                    mainHandler.post(() -> callback.onSuccess(routes));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Exception exception = t instanceof Exception
                        ? (Exception) t
                        : new Exception(t);

                mainHandler.post(() -> callback.onError(exception));
            }
        }, executor);
    }

    private Schema buildRouteSchema() {
        Schema stepSchema = Schema.obj(
                Map.of(
                        "name", Schema.str(),
                        "category", Schema.str(),
                        "description", Schema.str(),
                        "period", Schema.str(),
                        "estimatedDurationMinutes", Schema.numInt(),
                        "estimatedCost", Schema.numDouble(),
                        "latitude", Schema.numDouble(),
                        "longitude", Schema.numDouble(),
                        "travelToNextMinutes", Schema.numInt(),
                        "travelToNextMode", Schema.str()
                )
        );

        Schema routeSchema = Schema.obj(
                Map.of(
                        "title", Schema.str(),
                        "type", Schema.enumeration(List.of("ECONOMIC", "BALANCED", "COMFORT")),
                        "summary", Schema.str(),
                        "estimatedBudget", Schema.numDouble(),
                        "estimatedDurationMinutes", Schema.numInt(),
                        "effortLevel", Schema.str(),
                        "weatherAdvice", Schema.str(),
                        "steps", Schema.array(stepSchema)
                )
        );

        return Schema.obj(
                Map.of(
                        "routes", Schema.array(routeSchema)
                )
        );
    }

    private String buildPrompt(RoutePreferences preferences) {
        return "Tu es un assistant expert en planification de voyages.\n\n" +
                "Génère exactement 3 options de parcours pour visiter la destination demandée :\n" +
                "1. ECONOMIC : économique\n" +
                "2. BALANCED : équilibré\n" +
                "3. COMFORT : confortable\n\n" +

                "Contraintes utilisateur :\n" +
                "- Destination : " + preferences.getDestination() + "\n" +
                "- Activités souhaitées : " + preferences.getActivities() + "\n" +
                "- Budget : " + preferences.getBudgetLevel() + "\n" +
                "- Durée : " + preferences.getDurationDays() + " jour(s)\n" +
                "- Effort accepté : " + preferences.getEffortLevel() + "\n" +
                "- Saison préférée : " + preferences.getPreferredSeason() + "\n" +
                "- Lieux obligatoires : " + preferences.getMustSeePlaces() + "\n" +
                "- Éviter pluie : " + preferences.isAvoidRain() + "\n" +
                "- Éviter chaleur : " + preferences.isAvoidHeat() + "\n" +
                "- Éviter froid : " + preferences.isAvoidCold() + "\n" +
                "- Éviter humidité : " + preferences.isAvoidHumidity() + "\n\n" +

                "Règles importantes :\n" +
                "- Réponds uniquement avec le JSON demandé par le schéma.\n" +
                "- Les parcours doivent être réalistes pour un visiteur.\n" +
                "- Inclure les lieux obligatoires si possible.\n" +
                "- Les étapes doivent être ordonnées logiquement.\n" +
                "- Utilise les périodes : Matin, Après-midi, Soir.\n" +
                "- Donne des estimations raisonnables de budget et durée.\n" +
                "- Si tu connais des coordonnées approximatives, ajoute latitude/longitude.\n" +
                "- Ne promets pas de disponibilité réelle des lieux ou horaires exacts.\n";
    }

    private List<RouteOption> parseRoutes(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONArray routesArray = root.getJSONArray("routes");

        List<RouteOption> routes = new ArrayList<>();

        for (int i = 0; i < routesArray.length(); i++) {
            JSONObject routeObject = routesArray.getJSONObject(i);

            RouteOption route = new RouteOption();
            route.setTitle(routeObject.optString("title"));
            route.setType(routeObject.optString("type"));
            route.setSummary(routeObject.optString("summary"));
            route.setEstimatedBudget(routeObject.optDouble("estimatedBudget"));
            route.setEstimatedDurationMinutes(routeObject.optInt("estimatedDurationMinutes"));
            route.setEffortLevel(routeObject.optString("effortLevel"));
            route.setWeatherAdvice(routeObject.optString("weatherAdvice"));

            JSONArray stepsArray = routeObject.optJSONArray("steps");
            List<RouteStep> steps = new ArrayList<>();

            if (stepsArray != null) {
                for (int j = 0; j < stepsArray.length(); j++) {
                    JSONObject stepObject = stepsArray.getJSONObject(j);

                    RouteStep step = new RouteStep();
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

            route.setSteps(steps);
            routes.add(route);
        }

        return routes;
    }
}