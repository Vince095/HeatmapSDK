# HeatmapSDK

A lightweight, performance-conscious Android SDK for tracking user interactions (taps, swipes, scrolls) and generating insightful heatmap visualizations.  
This SDK is designed to be easily integrated into any existing Android application to provide a deeper understanding of user behavior on each screen.

---

## Features

- **Automatic Event Capture** – Silently records user taps, swipes, and scrolls across all Activities.  
- **Screen Tracking** – Automatically associates events with the correct screen (Activity name).  
- **Gesture Recognition** – Differentiates between simple taps and directional swipes (up, down, left, right).  
- **User Association** – All captured data is tied to a specific, authenticated user ID.  
- **Local Persistence** – Uses a local Room database to store events, preventing data loss if the user is offline.  
- **Efficient Batching** – Events are flushed to the server in batches to conserve network and battery resources.  
- **On-Demand Screenshot Generation** – Creates and uploads a screenshot with a heatmap overlay without ever showing the overlay to the end-user.  
- **Configurable Endpoint** – The backend API URL can be set dynamically during initialization.  

---

## Installation

The SDK is available through [JitPack](https://jitpack.io/).

### Step 1. Add JitPack repository  
In your **settings.gradle**:

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
### Step 2. Add the dependency

In your **app/build.gradle**:

```dependencies {
    // Heatmap SDK
    implementation 'com.github.Vince095:HeatmapSDK:1.0.0'

    // Required by the SDK for networking
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
}
```
### Usage
### 1. Initialization

Initialize the SDK once in your custom Application class:

```import com.emanthus.heatmap.sdk.HeatmapSDK;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // The base URL for your backend API
        String heatmapApiUrl = "https://your.server.com/api/heatmap/";

        HeatmapSDK.initialize(this, heatmapApiUrl);
    }
}
```
### 2. User Identification

### Associate analytics with a user:
```
// After login
HeatmapSDK.getInstance().identifyUser("user-unique-id-123", "user-auth-token-abc");

// On logout
HeatmapSDK.getInstance().clearUser();

3. Capture a Heatmap Screenshot

Generate and upload a heatmap screenshot of the current view:

Button captureButton = findViewById(R.id.capture_heatmap_button);
captureButton.setOnClickListener(v -> {
    ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
    HeatmapSDK.getInstance().captureHeatmapScreenshot(rootView);
});
```
