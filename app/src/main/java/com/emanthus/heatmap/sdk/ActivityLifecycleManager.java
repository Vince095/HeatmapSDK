package com.emanthus.heatmap.sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ActivityLifecycleManager implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "ActivityLifecycle";
    private final HeatmapSDK sdkInstance;
    private String currentScreenName;

    public ActivityLifecycleManager(HeatmapSDK sdkInstance) {
        this.sdkInstance = sdkInstance;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentScreenName = activity.getClass().getSimpleName();
        Log.d(TAG, "Resumed: " + currentScreenName);

        // Attach touch listener to the root view of the activity
        View rootView = activity.getWindow().getDecorView().getRootView();
        if (rootView instanceof ViewGroup) {
            rootView.setOnTouchListener(new TouchEventInterceptor(activity.getApplicationContext(),sdkInstance, currentScreenName));
        } else {
            Log.e(TAG, "Root view is not a ViewGroup, cannot attach touch listener.");
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        String pausedScreen = activity.getClass().getSimpleName();
        Log.d(TAG, "Paused: " + pausedScreen);

        View rootView = activity.getWindow().getDecorView().getRootView();
        if (rootView != null) {
            rootView.setOnTouchListener(null);
        }
        currentScreenName = null;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    public String getCurrentScreenName() {
        return currentScreenName;
    }
}
