package com.lecomte.jessy.spotifystreamerstage1v3;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;

import com.lecomte.jessy.spotifystreamerstage1v3.other.AudioPlayerService;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;

/**
 * Created by Jessy on 2015-07-08.
 *
 * This class is used to get access to resources anywhere in my app instead of having to pass a
 * context to all methods that need access to resources
 * http://stackoverflow.com/questions/4391720/how-can-i-get-a-resource-content-from-a-static-context/4391811#4391811
 */
public class App extends Application {

    private static final String TAG = "App";
    private static Context mContext;
    private static boolean mIsTwoPaneLayout;
    private static Handler mForegroundServiceHandler = new Handler();
    private static Runnable mForegroundServiceRunnable;
    private static final int UPDATE_FOREGROUND_SERVICE_INTERVAL = 300; // milliseconds
    private static int mActivitiesRunning = 0;

    // Get the resources anywhere in my app
    public static Resources getRes() {
        return mContext.getResources();
    }

    public static Context getContext() { return mContext; }

    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
        mContext = getApplicationContext();

        // Determine if the main activity has 1-pane or 2-pane layout
        boolean twoPanes = false;

        try {
            twoPanes = getRes().getBoolean(R.bool.has_two_panes);
        } catch (Resources.NotFoundException e) {
            // An exception means there is no value in file so it's a 1-pane layout
        }

        mIsTwoPaneLayout = twoPanes;

        mForegroundServiceRunnable = new Runnable() {
            @Override public void run() {

                // If the service is running, it means the NowPlaying views has been created,
                // the audio service was created and a track started playing
                // We only want to set the service as foreground when the service has been used
                // If user exits app without having played a track, we don't want to set the audio
                // service as a foreground service
                if (Utils.isServiceRunning(AudioPlayerService.class)) {
                    // Default: assume App is in foreground, setting service as a background service
                    String intentAction = AudioPlayerService.ACTION_STOP_FOREGROUND;
                    Intent intent = new Intent(getContext(), AudioPlayerService.class);

                    // App is in background: set the audio player service as a foreground service
                    // to prevent OS from shutting down the service
                    if (mActivitiesRunning == 0) {
                        intentAction = AudioPlayerService.ACTION_START_FOREGROUND;
                    }

                    intent.setAction(intentAction);
                    startService(intent);
                    Utils.log(TAG, "Runnable.run() - Service set as: "
                            + intentAction.substring(intentAction.lastIndexOf(".") + 1));
                }
            }
        };
    }

    public static boolean isTwoPaneLayout() {
        return mIsTwoPaneLayout;
    }

    // Cancel any pending callbacks and set the next one at specified elapsed time
    private static void setupRunnable() {
        mForegroundServiceHandler.removeCallbacks(mForegroundServiceRunnable);
        mForegroundServiceHandler.postDelayed(mForegroundServiceRunnable,
                UPDATE_FOREGROUND_SERVICE_INTERVAL);
    }

    // http://baroqueworksdev.blogspot.ca/2012/12/how-to-use-activitylifecyclecallbacks.html
    private class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            //Utils.log(TAG, "onActivityCreated() - Activity: " + activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            //Utils.log(TAG, "onActivityStarted() - Activity: " + activity);
        }

        @Override
        public void onActivityResumed(Activity activity) {
            mActivitiesRunning++;
            Utils.log(TAG, "onActivityResumed() - Activities running count: " + mActivitiesRunning + " [Service running: " + Utils.isServiceRunning(AudioPlayerService.class) + "]");

            // App is in foreground: set the audio player service as a background service
            if (Utils.isServiceRunning(AudioPlayerService.class) && mActivitiesRunning > 0) {
                setupRunnable();
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            mActivitiesRunning--;
            Utils.log(TAG, "onActivityPaused() - Activities running count: " + mActivitiesRunning + " [Service running: " + Utils.isServiceRunning(AudioPlayerService.class) + "]");

            // App is in background: set the audio player service as a foreground service
            // to prevent OS from shutting down the service
            if (Utils.isServiceRunning(AudioPlayerService.class) && mActivitiesRunning == 0) {
                setupRunnable();
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            //Utils.log(TAG, "onActivityStopped() - Activity: " + activity);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}

