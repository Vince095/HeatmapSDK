package com.emanthus.heatmap.sdk;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

public class TouchEventInterceptor implements View.OnTouchListener {

    private final HeatmapSDK sdkInstance;
    private final String screenName;
    private static final String TAG = "GestureInterceptor";
    private final GestureDetectorCompat gestureDetector;

    public TouchEventInterceptor(Context context, HeatmapSDK sdkInstance, String screenName) {
        this.sdkInstance = sdkInstance;
        this.screenName = screenName;
        this.gestureDetector = new GestureDetectorCompat(context, new GestureListener(sdkInstance, screenName));
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

//        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//            sdkInstance.recordEvent( "TOUCH",motionEvent.getX(), motionEvent.getY(), screenName);
//        }
        gestureDetector.onTouchEvent(motionEvent);

        return false;
    }

    /**
     * The listener that receives gesture callbacks from the GestureDetector.
     */
    private static class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD_VELOCITY = 100;
        private static final int SWIPE_MIN_DISTANCE = 120;

        private final HeatmapSDK sdkInstance;
        private final String screenName;

        GestureListener(HeatmapSDK sdk, String screenName) {
            this.sdkInstance = sdk;
            this.screenName = screenName;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            // Must return true here to indicate that we want to handle the gesture.
            return true;
        }

        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            sdkInstance.recordEvent("SCROLL", e1.getX(), e1.getY(), e2.getX(), e2.getY(), screenName);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            sdkInstance.recordEvent("TOUCH", e.getX(), e.getY(), e.getPressure() , screenName);
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            String swipeDirection = "";
            assert e1 != null;
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (diffX > 0) {
                        swipeDirection = "SWIPE_RIGHT";
                    } else {
                        swipeDirection = "SWIPE_LEFT";
                    }
                }
            } else {
                if (Math.abs(diffY) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    if (diffY > 0) {
                        swipeDirection = "SWIPE_DOWN";
                    } else {
                        swipeDirection = "SWIPE_UP";
                    }
                }
            }

            if (!swipeDirection.isEmpty()) {
                sdkInstance.recordEvent(swipeDirection, e1.getX(), e1.getY(), e2.getX(), e2.getY(), screenName);
                Log.d(TAG, "Swipe detected: " + swipeDirection);
                return true;
            }

            return false;
        }
    }
}
