package com.example.traveling.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SavedRouteDao {

    @Insert
    long insert(SavedRouteEntity route);

    @Update
    void update(SavedRouteEntity route);

    @Delete
    void delete(SavedRouteEntity route);

    @Query("SELECT * FROM saved_routes ORDER BY createdAt DESC")
    List<SavedRouteEntity> getAllSavedRoutes();

    @Query("SELECT * FROM saved_routes WHERE id = :routeId LIMIT 1")
    SavedRouteEntity getSavedRouteById(long routeId);

    @Query("DELETE FROM saved_routes WHERE id = :routeId")
    void deleteById(long routeId);

    @Query("UPDATE saved_routes SET liked = :liked WHERE id = :routeId")
    void updateLiked(long routeId, boolean liked);
}