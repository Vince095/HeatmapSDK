package com.emanthus.heatmapdemoapp;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.emanthus.heatmap.sdk.HeatmapSDK;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HeatmapSDK.getInstance().identifyUser("2674", "2y10BwIu7SXx0UpDbI4NuH7GOxzTA15OqlVenWr8eSUTlMy6R2E9rHa");

        Button showHeatmapButton = findViewById(R.id.show_heatmap_button);
        ViewGroup rootView = findViewById(R.id.main);

        showHeatmapButton.setOnClickListener(v -> {
            HeatmapSDK.getInstance().captureHeatmapScreenshot(rootView);
                   // showHeatmap(rootView);
        });
    }
}