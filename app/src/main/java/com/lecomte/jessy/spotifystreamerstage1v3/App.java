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

    // Get the resources anywhere in my app
    public static Resources getRes() {
        return mContext.getResources();
    }

    public static Context getContext() { return mContext; }

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}

