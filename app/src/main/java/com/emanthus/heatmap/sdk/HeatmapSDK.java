package com.emanthus.heatmap.sdk;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.room.Room;

import com.emanthus.heatmap.sdk.database.EventDatabase;
import com.emanthus.heatmap.sdk.database.HeatmapEvent;
import com.emanthus.heatmap.sdk.models.HeatmapData;
import com.emanthus.heatmap.sdk.network.NetworkClient;
import com.emanthus.heatmapdemoapp.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HeatmapSDK {
    private static final String TAG = "HeatmapSDK";
    private static volatile HeatmapSDK instance;
    private static final String HEATMAP_VIEW_TAG = "heatmap_renderer_view_tag";

    private final EventDatabase database;
    private final NetworkClient networkClient;
    private final ExecutorService executorService;
    private final ActivityLifecycleManager lifecycleManager;

    private String currentUserId;
    private String authToken;
    private boolean isInitialized = false;
    private final Context applicationContext;

    private HeatmapSDK(Application application, String apiBaseUrl) {
        this.database = Room.databaseBuilder(application, EventDatabase.class, "heatmap-db").build();
        this.networkClient = new NetworkClient(apiBaseUrl);
        this.executorService = Executors.newSingleThreadExecutor();
        this.lifecycleManager = new ActivityLifecycleManager(this);
        application.registerActivityLifecycleCallbacks(lifecycleManager);
        this.applicationContext = application.getApplicationContext();
    }

    /**
     * Initializes the SDK. This must be called once, typically in your Application class.
     * @param application The application instance.
     */
    public static void initialize(Application application , String apiBaseUrl) {
        if (instance == null) {
            synchronized (HeatmapSDK.class) {
                if (instance == null) {
                    instance = new HeatmapSDK(application, apiBaseUrl);
                    instance.isInitialized = true;
                    Log.d(TAG, "Heatmap SDK Initialized successfully with URL: "+ apiBaseUrl );
                }
            }
        }
    }

    /**
     * @return The singleton instance of the SDK.
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    public static HeatmapSDK getInstance() {
        if (instance == null) {
            throw new IllegalStateException("HeatmapSDK must be initialized in your Application class before use.");
        }
        return instance;
    }

    /**
     * Associates all subsequent events with a specific user.
     * Call this method when a user logs in.
     * @param userId The unique identifier for the user.
     * @param token The authentication token for server communication.
     */
    public void identifyUser(String userId, String token) {
        this.currentUserId = userId;
        this.authToken = token;
        Log.d(TAG, "User identified: " + userId);
        flushEvents();
    }

    /**
     * Clears user identification. Call this when a user logs out.
     */
    public void clearUser() {
        Log.d(TAG, "User cleared. Events will be tracked anonymously.");
        this.currentUserId = null;
        this.authToken = null;
    }

    /**
     * Records a single interaction event.
     * @param x The x-coordinate of the event.
     * @param y The y-coordinate of the event.
     * @param screenName The name of the screen where the event occurred.
     */
    void recordEvent(String eventType, float x, float y, float intensity, String screenName) {
        if (!isInitialized) {
            Log.w(TAG, "SDK not initialized. Skipping event recording.");
            return;
        }
        if (screenName == null || screenName.isEmpty()) {
            Log.w(TAG, "Screen name is null or empty. Skipping event.");
            return;
        }

        final HeatmapEvent event = new HeatmapEvent(
                System.currentTimeMillis(),
                eventType,
                x,
                y,
                intensity,
                screenName,
                currentUserId
        );

        executorService.execute(() -> {
            database.eventDao().insert(event);
            Log.d(TAG, "Event Recorded: " + eventType + " on " + screenName + " at (" + x + "," + y + ")");
        });
    }

    /**
     * Overloaded method to record a swipe event (start → end).
     * @param startX The starting x-coordinate.
     * @param startY The starting y-coordinate.
     * @param endX   The ending x-coordinate.
     * @param endY   The ending y-coordinate.
     * @param screenName The screen where the swipe occurred.
     */
    void recordEvent(String eventType, float startX, float startY, float endX, float endY, String screenName) {
        if (!isInitialized) {
            Log.w(TAG, "SDK not initialized. Skipping swipe event recording.");
            return;
        }
        if (screenName == null || screenName.isEmpty()) {
            Log.w(TAG, "Screen name is null or empty. Skipping swipe event.");
            return;
        }

        final HeatmapEvent event = new HeatmapEvent(
                System.currentTimeMillis(),
                eventType,
                startX,
                startY,
                endX,
                endY,
                screenName,
                currentUserId
        );

        executorService.execute(() -> {
            database.eventDao().insert(event);
            Log.d(TAG, "Swipe Recorded: from (" + startX + "," + startY + ") to (" + endX + "," + endY + ") on " + screenName);
        });
    }


    /**
     * Triggers an asynchronous upload of all locally stored events to the server.
     */
    public void flushEvents() {
        executorService.execute(() -> {
            List<HeatmapEvent> events = database.eventDao().getAll();
            if (events.isEmpty()) {
                Log.d(TAG, "No events to flush.");
                return;
            }
            Log.d(TAG, "Flushing " + events.size() + " events.");
            networkClient.uploadEvents(events, currentUserId,authToken, new NetworkClient.Callback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Events uploaded successfully. Events:" + events );
                    executorService.execute(() -> database.eventDao().delete(events));
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to upload events", e);
                }
            });
        });
    }

    /**
     * Fetches heatmap data and renders it as an overlay on the provided root view.
     * @param rootView The root view group to draw the heatmap on.
     */
    public void showHeatmap(ViewGroup rootView) {
        Context context = rootView.getContext();
        String screenName = context.getClass().getSimpleName();

        View oldHeatmap = rootView.findViewWithTag(HEATMAP_VIEW_TAG);
        if (oldHeatmap != null) {
            rootView.removeView(oldHeatmap);
            Log.d(TAG, "Removed stale heatmap view.");
        }

        networkClient.fetchHeatmapData(screenName, currentUserId, authToken,applicationContext, new NetworkClient.DataCallback<HeatmapData>() {
            @Override
            public void onSuccess(HeatmapData data) {
                if (data != null && !data.getPoints().isEmpty()) {
                    HeatmapRendererView heatmapView = new HeatmapRendererView(context, data);
                    rootView.addView(heatmapView);
                    Log.d(TAG, "Heatmap rendered for screen: " + screenName);

                    // After rendering, capture and upload
                    captureAndUploadScreenshot(rootView);
                } else {
                    Log.d(TAG, "No heatmap data available for screen: " + screenName);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch heatmap data", e);
            }
        });
    }

    private void captureAndUploadScreenshot(ViewGroup rootView) {

        rootView.post(() -> {
            Bitmap screenshot = ScreenshotModule.capture(rootView);
            if (screenshot != null) {
                Log.d(TAG, "Screenshot captured.");

                networkClient.uploadScreenshot(screenshot, rootView.getContext().getClass().getSimpleName(), currentUserId, authToken, new NetworkClient.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG,  screenshot+" Screenshot uploaded successfully.");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Screenshot upload failed", e);
                    }
                });
            } else {
                Log.e(TAG, "Failed to capture screenshot.");
            }
        });
    }

    /**
     * Captures a screenshot of the current view, overlays heatmap data on it in memory,
     * and uploads the result, all without showing the heatmap to the user.
     * @param rootView The root view group to capture.
     */
    public void captureHeatmapScreenshot(ViewGroup rootView) {
        Context context = rootView.getContext();
        String screenName = context.getClass().getSimpleName();

        View oldHeatmap = rootView.findViewWithTag(HEATMAP_VIEW_TAG);
        if (oldHeatmap != null) {
            rootView.removeView(oldHeatmap);
            Log.d(TAG, "Removed stale heatmap view.");
        }

        networkClient.fetchHeatmapData(screenName, currentUserId, authToken, context, new NetworkClient.DataCallback<HeatmapData>() {
            @Override
            public void onSuccess(HeatmapData data) {
                if (data == null || data.getPoints().isEmpty()) {
                    Log.d(TAG, "No heatmap data available to generate screenshot for screen: " + screenName);
                    return;
                }

                Bitmap baseScreenshot = ScreenshotModule.capture(rootView);
                if (baseScreenshot == null) {
                    Log.e(TAG, "Failed to capture base screenshot.");
                    return;
                }

                Canvas canvas = new Canvas(baseScreenshot);

                HeatmapRendererView heatmapView = new HeatmapRendererView(context, data);
                heatmapView.measure(
                        View.MeasureSpec.makeMeasureSpec(rootView.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(rootView.getHeight(), View.MeasureSpec.EXACTLY)
                );
                heatmapView.layout(0, 0, heatmapView.getMeasuredWidth(), heatmapView.getMeasuredHeight());

                heatmapView.draw(canvas);
                Log.d(TAG, "Heatmap drawn onto screenshot in memory.");

//                android.app.Activity activity = (android.app.Activity) rootView.getContext();
//                android.widget.ImageView debugView = activity.findViewById(R.id.debug_screenshot_view);
//                if (debugView != null) {
//                    debugView.setImageBitmap(baseScreenshot);
//                    debugView.setVisibility(View.VISIBLE);
//                    Log.d(TAG, "Screenshot displayed in debug ImageView.");
//                }

                networkClient.uploadScreenshot(baseScreenshot, screenName, currentUserId, authToken, new NetworkClient.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Heatmap screenshot uploaded successfully.");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Heatmap screenshot upload failed", e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch heatmap data for screenshot", e);
            }
        });
    }

}
