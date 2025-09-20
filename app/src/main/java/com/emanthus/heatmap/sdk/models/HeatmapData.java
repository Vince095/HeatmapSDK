package com.emanthus.heatmap.sdk.models;

import java.util.ArrayList;
import java.util.List;

public class HeatmapData {
    private String screenName;
    private final List<HeatmapSwipe> swipes;
    private final List<HeatmapDataPoint> points;

    public HeatmapData(String screenName, List<HeatmapDataPoint> points,  List<HeatmapSwipe> swipes) {
        this.screenName = screenName;
        this.points = points;
        this.swipes = swipes != null ? swipes : new ArrayList<>();
    }

    public String getScreenName() { return screenName; }
    public List<HeatmapDataPoint> getPoints() { return points; }
    public List<HeatmapSwipe> getSwipes() { return swipes; }
}
