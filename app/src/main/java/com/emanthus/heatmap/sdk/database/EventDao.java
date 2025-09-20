package com.emanthus.heatmap.sdk.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HeatmapEvent event);

    @Query("SELECT * FROM events ORDER BY timestamp ASC")
    List<HeatmapEvent> getAll();

    @Delete
    void delete(List<HeatmapEvent> events);

    @Query("DELETE FROM events")
    void clearAll();
}
