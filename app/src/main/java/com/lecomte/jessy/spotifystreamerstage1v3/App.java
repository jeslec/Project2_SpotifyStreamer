package com.lecomte.jessy.spotifystreamerstage1v3;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.lecomte.jessy.spotifystreamerstage1v3.other.AudioPlayerService;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;

import java.util.ArrayList;

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
    private static boolean mIsInForeground = true;

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

                Utils.log(TAG, "ForegroundServiceRunnable - App in foreground: " + mIsInForeground);

                // TODO: Optimize this
                if (mIsInForeground) {
                    Intent intent = new Intent()
                            .setClass(getContext(), AudioPlayerService.class)
                            .setAction(AudioPlayerService.ACTION_STOP_FOREGROUND);
                    startService(intent);
                } else {
                    Intent intent = new Intent()
                            .setClass(getContext(), AudioPlayerService.class)
                            .setAction(AudioPlayerService.ACTION_START_FOREGROUND);
                    startService(intent);
                }
            }
        };
    }

    public static boolean isTwoPaneLayout() {
        return mIsTwoPaneLayout;
    }

    private static void setupRunnable() {
        mForegroundServiceHandler.removeCallbacks(mForegroundServiceRunnable);
        mForegroundServiceHandler.postDelayed(mForegroundServiceRunnable,
                UPDATE_FOREGROUND_SERVICE_INTERVAL);
    }

    // http://baroqueworksdev.blogspot.ca/2012/12/how-to-use-activitylifecyclecallbacks.html
    private static final class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

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
            Utils.log(TAG, "onActivityResumed() - Activity: " + activity);
            mIsInForeground = true;
            setupRunnable();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Utils.log(TAG, "onActivityPaused() - Activity: " + activity);
            mIsInForeground = false;
            setupRunnable();
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

