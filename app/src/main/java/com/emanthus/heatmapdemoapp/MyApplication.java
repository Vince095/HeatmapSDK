package com.emanthus.heatmapdemoapp;

import android.app.Application;

import com.emanthus.heatmap.sdk.HeatmapSDK;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Initialize heatmap
        HeatmapSDK.initialize(this, "https://dev.e-manthus.com/api/user/heatmap/");
    }
}
