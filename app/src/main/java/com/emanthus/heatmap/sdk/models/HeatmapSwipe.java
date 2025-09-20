package com.emanthus.heatmap.sdk.models;

public class HeatmapSwipe {
    private final float startX;
    private final float startY;
    private final float endX;
    private final float endY;
    private final float intensity;

    public HeatmapSwipe(float startX, float startY, float endX, float endY, float intensity) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.intensity = intensity;
    }

    public float getStartX() { return startX; }
    public float getStartY() { return startY; }
    public float getEndX() { return endX; }
    public float getEndY() { return endY; }
    public float getIntensity() { return intensity; }
}
