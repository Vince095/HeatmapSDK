package com.emanthus.heatmap.sdk.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(entities = {HeatmapEvent.class}, version = 1)
public abstract class EventDatabase extends RoomDatabase {
    public abstract EventDao eventDao();

    private static EventDatabase INSTANCE;

    public static synchronized EventDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), EventDatabase.class, "heatmap-db")
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return INSTANCE;
    }

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add new columns with default values
            database.execSQL("ALTER TABLE events ADD COLUMN endX REAL");
            database.execSQL("ALTER TABLE events ADD COLUMN endY REAL");
            database.execSQL("ALTER TABLE events ADD COLUMN intensity REAL NOT NULL DEFAULT 1.0");
        }
    };
}
