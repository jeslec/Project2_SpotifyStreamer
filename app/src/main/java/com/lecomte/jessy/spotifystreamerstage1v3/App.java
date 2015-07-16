package com.lecomte.jessy.spotifystreamerstage1v3;


import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

/**
 * Created by Jessy on 2015-07-08.
 *
 * This class is used to get access to resources anywhere in my app instead of having to pass a
 * context to all methods that need access to resources
 * http://stackoverflow.com/questions/4391720/how-can-i-get-a-resource-content-from-a-static-context/4391811#4391811
 */
public class App extends Application {

    private static Context mContext;
    private static boolean mIsTwoPaneLayout;

    // Get the resources anywhere in my app
    public static Resources getRes() {
        return mContext.getResources();
    }

    public static Context getContext() { return mContext; }

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        // Determine if the main activity has 1-pane or 2-pane layout
        boolean twoPanes = false;

        try {
            twoPanes = getRes().getBoolean(R.bool.has_two_panes);
        } catch (Resources.NotFoundException e) {
            // An exception means there is no value in file so it's a 1-pane layout
        }

        mIsTwoPaneLayout = twoPanes;
    }

    public static boolean isTwoPaneLayout() {
        return mIsTwoPaneLayout;
    }
}

