package com.emanthus.heatmap.sdk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.emanthus.heatmap.sdk.models.HeatmapData;
import com.emanthus.heatmap.sdk.models.HeatmapDataPoint;
import com.emanthus.heatmap.sdk.models.HeatmapSwipe;

public class HeatmapRendererView extends View {

    private final HeatmapData heatmapData;
    private final Paint paint;

    // Gradient colors for tap/press points
    private static final int[] GRADIENT_COLORS = {
            Color.argb(0, 0, 0, 255),    // Transparent
            Color.argb(128, 0, 0, 255),  // Blue
            Color.argb(128, 0, 255, 0),  // Green
            Color.argb(200, 255, 255, 0),// Yellow
            Color.argb(255, 255, 0, 0)   // Red
    };
    private static final float[] GRADIENT_STOPS = {0.0f, 0.25f, 0.5f, 0.75f, 1.0f};
    private static final int POINT_RADIUS = 100;

    public HeatmapRendererView(Context context, HeatmapData data) {
        super(context);
        this.heatmapData = data;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (heatmapData == null) return;

        drawPoints(canvas);
        drawSwipes(canvas);
    }

    /** Draws tap/press points using radial gradient intensity. */
    private void drawPoints(Canvas canvas) {
        if (heatmapData.getPoints() == null || heatmapData.getPoints().isEmpty()) return;

        for (HeatmapDataPoint point : heatmapData.getPoints()) {
            float intensity = point.getIntensity();
            float radius = POINT_RADIUS * intensity;

            RadialGradient gradient = new RadialGradient(
                    point.getX(),
                    point.getY(),
                    radius,
                    GRADIENT_COLORS,
                    GRADIENT_STOPS,
                    Shader.TileMode.CLAMP
            );

            paint.setShader(gradient);
            canvas.drawCircle(point.getX(), point.getY(), radius, paint);
        }

        paint.setShader(null);
    }

    /** Draws swipe gestures as intensity-based lines. */
    private void drawSwipes(Canvas canvas) {
        if (heatmapData.getSwipes() == null || heatmapData.getSwipes().isEmpty()) return;

        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        for (HeatmapSwipe swipe : heatmapData.getSwipes()) {
            float startX = swipe.getStartX();
            float startY = swipe.getStartY();
            float endX = swipe.getEndX();
            float endY = swipe.getEndY();

            // Debug log to verify coordinates
            Log.d("HeatmapRendererView", "Drawing swipe: (" + startX + "," + startY + ") -> (" + endX + "," + endY + ")");

            int alpha = (int) (swipe.getIntensity() * 255);
            paint.setColor(Color.argb(alpha, 255, 69, 0)); // Orange-red
            paint.setStrokeWidth(10 * swipe.getIntensity());

            canvas.drawLine(startX, startY, endX, endY, paint);

            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(startX, startY, 10, paint);
            canvas.drawCircle(endX, endY, 10, paint);

            paint.setStyle(Paint.Style.STROKE);
        }
    }
}
