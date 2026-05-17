package com.example.traveling.repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerationConfig;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.ai.type.Schema;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TravelShareAiRepository {

    public interface SuggestPostMetadataCallback {
        void onSuccess(PostMetadataSuggestion suggestion);
        void onError(Exception exception);
    }

    public static class PostMetadataSuggestion {
        private final String placeType;
        private final List<String> tags;
        private final String suggestedCaption;

        public PostMetadataSuggestion(String placeType,
                                      List<String> tags,
                                      String suggestedCaption) {
            this.placeType = placeType;
            this.tags = tags;
            this.suggestedCaption = suggestedCaption;
        }

        public String getPlaceType() {
            return placeType;
        }

        public List<String> getTags() {
            return tags;
        }

        public String getSuggestedCaption() {
            return suggestedCaption;
        }
    }

    private final Context appContext;
    private final GenerativeModelFutures model;
    private final Executor executor;
    private final Handler mainHandler;

    public TravelShareAiRepository(@NonNull Context context) {
        this.appContext = context.getApplicationContext();

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.responseMimeType = "application/json";
        configBuilder.responseSchema = buildSuggestionSchema();

        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel(
                        "gemini-3.1-flash-lite",
                        configBuilder.build()
                );

        model = GenerativeModelFutures.from(ai);
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void suggestPostMetadata(@NonNull Uri imageUri,
                                    @NonNull SuggestPostMetadataCallback callback) {
        executor.execute(() -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        appContext.getContentResolver(),
                        imageUri
                );

                Content content = new Content.Builder()
                        .addText(buildPrompt())
                        .addImage(bitmap)
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

                            PostMetadataSuggestion suggestion = parseSuggestion(json);
                            mainHandler.post(() -> callback.onSuccess(suggestion));
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

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private Schema buildSuggestionSchema() {
        HashMap<String, Schema> fields = new HashMap<>();

        fields.put("placeType", Schema.enumeration(List.of(
                "Nature",
                "Musée",
                "Monument",
                "Rue",
                "Restaurant",
                "Magasin",
                "Plage",
                "Montagne",
                "Ville",
                "Autre"
        )));

        fields.put("tags", Schema.array(Schema.str()));
        fields.put("suggestedCaption", Schema.str());

        return Schema.obj(fields);
    }

    private String buildPrompt() {
        return "Tu es un assistant d'annotation pour une application de partage de photos de voyage.\n\n" +
                "Analyse l'image fournie et retourne uniquement le JSON demandé par le schéma.\n\n" +
                "Objectif : aider l'utilisateur à publier une photo de voyage.\n\n" +
                "Contraintes :\n" +
                "- Tous les textes doivent être en français.\n" +
                "- Le champ placeType doit être exactement une des valeurs autorisées.\n" +
                "- Les tags doivent être courts, pertinents, sans hashtag, en minuscules si possible.\n" +
                "- Propose entre 3 et 8 tags.\n" +
                "- La légende doit être courte, naturelle et adaptée à une publication de voyage.\n" +
                "- Ne prétends pas connaître un lieu exact si l'image ne le montre pas clairement.\n" +
                "- Si l'image est ambiguë, utilise placeType = Autre et des tags génériques.";
    }

    private PostMetadataSuggestion parseSuggestion(String json) throws JSONException {
        JSONObject root = new JSONObject(json);

        String placeType = root.optString("placeType", "Autre");
        String suggestedCaption = root.optString("suggestedCaption", "");

        JSONArray tagsArray = root.optJSONArray("tags");
        List<String> tags = new ArrayList<>();

        if (tagsArray != null) {
            for (int i = 0; i < tagsArray.length(); i++) {
                String tag = tagsArray.optString(i, "").trim();

                if (!tag.isEmpty() && !tags.contains(tag)) {
                    tags.add(tag);
                }
            }
        }

        return new PostMetadataSuggestion(placeType, tags, suggestedCaption);
    }
}