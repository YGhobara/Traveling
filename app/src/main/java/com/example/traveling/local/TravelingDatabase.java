package com.example.traveling.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {SavedRouteEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class TravelingDatabase extends RoomDatabase {

    private static volatile TravelingDatabase INSTANCE;

    public abstract SavedRouteDao savedRouteDao();

    public static TravelingDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TravelingDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TravelingDatabase.class,
                                    "traveling_database"
                            )
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}