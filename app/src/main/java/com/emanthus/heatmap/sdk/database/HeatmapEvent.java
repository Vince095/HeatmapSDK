package com.emanthus.heatmap.sdk.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class HeatmapEvent {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private long timestamp;
    private String eventType;
    private float x;
    private float y;
    private Float endX;
    private Float endY;
    private float intensity;
    private String screenName;
    private String userId;

    public HeatmapEvent(long timestamp, String eventType, float x, float y, float intensity, String screenName, String userId) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.x = x;
        this.y = y;
        this.screenName = screenName;
        this.userId = userId;
        this.intensity = intensity;
    }

    // Overloaded constructor for swipe events
    public HeatmapEvent(long timestamp, String eventType, float startX, float startY, float endX, float endY, String screenName, String userId) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.x = startX;
        this.y = startY;
        this.endX = endX;
        this.endY = endY;
        this.screenName = screenName;
        this.userId = userId;
        this.intensity = 0.8f;
    }

    // --- Getters & Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public Float getEndX() { return endX; }
    public void setEndX(Float endX) { this.endX = endX; }

    public Float getEndY() { return endY; }
    public void setEndY(Float endY) { this.endY = endY; }

    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = intensity; }

    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
