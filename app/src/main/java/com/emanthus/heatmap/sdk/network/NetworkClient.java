package com.emanthus.heatmap.sdk.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.emanthus.heatmap.sdk.database.EventDatabase;
import com.emanthus.heatmap.sdk.database.HeatmapEvent;
import com.emanthus.heatmap.sdk.models.HeatmapData;
import com.emanthus.heatmap.sdk.models.HeatmapDataPoint;
import com.emanthus.heatmap.sdk.models.HeatmapSwipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetworkClient {

    private static final String TAG = "NetworkClient";
    private final String apiBaseUrl;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final OkHttpClient client = new OkHttpClient();

    public NetworkClient(String apiBaseUrl) {

        if (apiBaseUrl == null || apiBaseUrl.isEmpty()) {
            throw new IllegalArgumentException("API Base URL cannot be null or empty.");
        }
        this.apiBaseUrl = apiBaseUrl;
    }

    public interface Callback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }


    public void uploadEvents(List<HeatmapEvent> events,String userId ,String token, Callback callback) {
        Log.d(TAG, "Uploading " + events.size() + " events...");

        executorService.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("id", userId);
                payload.put("token", token);

                JSONArray eventsArray = getJsonArray(events);

                payload.put("events", eventsArray);

                RequestBody body = RequestBody.create(
                        payload.toString(),
                        MediaType.parse("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url( this.apiBaseUrl +"ingest-events")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Upload failed", e);
                        mainThreadHandler.post(() -> callback.onFailure(e));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            assert response.body() != null;
                            Log.d(TAG, "Upload successful: " + response.body().string());
                            mainThreadHandler.post(callback::onSuccess);
                        } else {
                            Log.e(TAG, "Server error: " + response.code());
                            mainThreadHandler.post(() -> callback.onFailure(
                                    new Exception("Server error: " + response.code())
                            ));
                        }
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                mainThreadHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    private static @NonNull JSONArray getJsonArray(List<HeatmapEvent> events) throws JSONException {
        JSONArray eventsArray = new JSONArray();
        for (int i = 0; i < events.size(); i++) {
            HeatmapEvent e = events.get(i);

            Date date = new Date(e.getTimestamp());

            Log.d(TAG, "Event: " + e.getEventType());
            JSONObject eventObj = new JSONObject();
            eventObj.put("screen_name", e.getScreenName());
            eventObj.put("event_type", e.getEventType());
            eventObj.put("coordinate_x", e.getX());
            eventObj.put("coordinate_y", e.getY());
            eventObj.put("timestamp", date);

            if (Objects.equals(e.getEventType(), "SWIPE_UP")|| Objects.equals(e.getEventType(), "SWIPE_DOWN") || Objects.equals(e.getEventType(), "SCROLL")){
                eventObj.put("end_x", e.getEndX());
                eventObj.put("end_y", e.getEndY());
                eventObj.put("intensity", e.getIntensity());
            }

            eventsArray.put(eventObj);
        }
        Log.d(TAG, "Events: " + eventsArray);
        return eventsArray;
    }


    public void fetchHeatmapData(String screenName,
                                 String userId,
                                 String authToken,
                                 Context context,
                                 DataCallback<HeatmapData> callback) {
        Log.d(TAG, "Simulating fetch for heatmap data for screen: " + screenName);
        mainThreadHandler.postDelayed(() -> {
            ArrayList<HeatmapDataPoint> points = new ArrayList<>();
            ArrayList<HeatmapSwipe> swipes = new ArrayList<>();

            EventDatabase database = EventDatabase.getInstance(context);
            executorService.execute(() -> {
                List<HeatmapEvent> events = database.eventDao().getAll();

                uploadEvents(events, userId ,authToken, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Mock upload successful. "+events.get(0).getUserId());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Mock upload failed.", e);
                    }
                });

                for (HeatmapEvent event : events) {
                    if (event.getEventType().startsWith("SWIPE")) {
                        HeatmapSwipe swipe = new HeatmapSwipe(
                                event.getX(), event.getY(),
                                event.getEndX(), event.getEndY(),
                                event.getIntensity()
                        );
                        swipes.add(swipe);
                    } else {
                        points.add(new HeatmapDataPoint(event.getX(), event.getY(), event.getIntensity()));
                    }
                }

                HeatmapData data = new HeatmapData(screenName, points, swipes);

                mainThreadHandler.post(() -> {
                    callback.onSuccess(data);
                });

            });

        }, 1000);
    }

    /**
     * Uploads the captured screenshot and metadata to the server.
     * @param bitmap The bitmap of the screenshot.
     * @param screenName The name of the screen captured.
     * @param userId The ID of the user (maps to 'id' field).
     * @param authToken The auth token (maps to 'token' field).
     * @param callback The callback to be invoked on the main thread.
     */
    public void uploadScreenshot(Bitmap bitmap, String screenName, String userId, String authToken, Callback callback) {
        if (userId == null || authToken == null) {
            callback.onFailure(new Exception("User ID or Auth Token is null. Cannot upload."));
            return;
        }
        Log.d(TAG, "Preparing to upload screenshot for screen: " + screenName);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", userId)
                .addFormDataPart("token", authToken)
                .addFormDataPart("screen_name", screenName)
                .addFormDataPart("screenshot", "screenshot_" + System.currentTimeMillis() + ".png",
                        RequestBody.create(byteArray, MediaType.parse("image/png")))
                .build();

        Request request = new Request.Builder()
                .url( this.apiBaseUrl +"upload-screenshot")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Screenshot upload failed.", e);
                mainThreadHandler.post(() -> callback.onFailure(e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // The response body must be closed to prevent resource leaks.
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful()) {
                        assert responseBody != null;
                        Log.d(TAG, "Screenshot uploaded successfully. Response: " + responseBody.string());
                        mainThreadHandler.post(callback::onSuccess);
                    } else {
                        assert responseBody != null;
                        String errorBody = responseBody.string();
                        Log.e(TAG, "Screenshot upload failed with code: " + response.code() + " Body: " + errorBody);
                        mainThreadHandler.post(() -> callback.onFailure(new IOException("Server responded with " + response.code() + ": " + errorBody)));
                    }
                }
            }
        });
    }
}
