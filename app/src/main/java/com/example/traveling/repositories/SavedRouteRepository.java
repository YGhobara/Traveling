package com.example.traveling.repositories;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.traveling.local.SavedRouteDao;
import com.example.traveling.local.SavedRouteEntity;
import com.example.traveling.local.TravelingDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedRouteRepository {

    public interface SaveRouteCallback {
        void onSuccess(long routeId);
        void onError(Exception exception);
    }

    public interface LoadRoutesCallback {
        void onSuccess(List<SavedRouteEntity> routes);
        void onError(Exception exception);
    }

    public interface LoadRouteCallback {
        void onSuccess(SavedRouteEntity route);
        void onError(Exception exception);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception exception);
    }

    private final SavedRouteDao savedRouteDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public SavedRouteRepository(Context context) {
        TravelingDatabase database = TravelingDatabase.getInstance(context);
        savedRouteDao = database.savedRouteDao();

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void saveRoute(SavedRouteEntity route, SaveRouteCallback callback) {
        executorService.execute(() -> {
            try {
                long id = savedRouteDao.insert(route);

                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess(id);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        });
    }

    public void getAllSavedRoutes(LoadRoutesCallback callback) {
        executorService.execute(() -> {
            try {
                List<SavedRouteEntity> routes = savedRouteDao.getAllSavedRoutes();

                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess(routes);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        });
    }

    public void getSavedRouteById(long routeId, LoadRouteCallback callback) {
        executorService.execute(() -> {
            try {
                SavedRouteEntity route = savedRouteDao.getSavedRouteById(routeId);

                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess(route);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        });
    }

    public void deleteRouteById(long routeId, SimpleCallback callback) {
        executorService.execute(() -> {
            try {
                savedRouteDao.deleteById(routeId);

                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        });
    }

    public void updateLiked(long routeId, boolean liked, SimpleCallback callback) {
        executorService.execute(() -> {
            try {
                savedRouteDao.updateLiked(routeId, liked);

                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        });
    }
}