package com.emanthus.heatmap.sdk;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

public class ScreenshotModule {

    private static final String TAG = "ScreenshotModule";

    /**
     * Captures the contents of a view and returns it as a Bitmap.
     * @param view The view to capture.
     * @return A Bitmap of the view, or null if capturing fails.
     */
    public static Bitmap capture(View view) {
        if (view.getWidth() == 0 || view.getHeight() == 0) {
            Log.e(TAG, "View has no dimensions, cannot capture screenshot.");
            return null;
        }
        try {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error capturing view", e);
            return null;
        }
    }
}
