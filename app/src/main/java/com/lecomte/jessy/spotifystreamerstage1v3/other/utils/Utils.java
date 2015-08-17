package com.lecomte.jessy.spotifystreamerstage1v3.other.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.widget.Toast;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jessy on 2015-07-08.
 *
 *  Generic utility methods
 *
 *  These methods could be imported in any project and be used AS-IS, without any modification.
 *
 */
public class Utils {

    static private Toast mToast = null;
    static private final boolean SHOW_DEBUG = App.getRes().getBoolean(R.bool.show_debug_strings);

    public static void showToast(int stringId) {
        if (mToast == null) {
            mToast = Toast.makeText(App.getContext(), stringId, Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER, (int)App.getRes().getDimension(R.dimen.toast_offset_x),
                    (int)App.getRes().getDimension(R.dimen.toast_offset_y));
        }
        else {
            mToast.setText(stringId);
        }

        mToast.show();
    }

    public static void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(App.getContext(), msg, Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER, (int) App.getRes().getDimension(R.dimen.toast_offset_x),
                    (int) App.getRes().getDimension(R.dimen.toast_offset_y));
        }
        else {
            mToast.setText(msg);
        }

        mToast.show();
    }

    public static void log(String tag, String msg) {
        if (SHOW_DEBUG) {
            Log.d(tag, msg);
        }
    }

    // I could have used log(tag, msg) but it will run faster (1 less if) by duplicating code
    public static void log(String tag, int stringId) {
        if (SHOW_DEBUG) {
            Log.d(tag, App.getRes().getString(stringId));
        }
    }

    public static void logLoop(String tag, String preIndex, String postIndex, List<?> list) {
        if (SHOW_DEBUG) {
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                Log.d(tag, preIndex + String.format("%1$02d", i) +
                        postIndex + list.get(i).toString());
            }
        }
    }

    // Check if app has access to Internet either using Wifi or Mobile
    // Returns:
    //  -true: has access to Internet (wifi or mobile)
    //  -false: does not have access to Internet
    public static boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) App.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Converts milliseconds into minutes and seconds
    // Returns pair:
    // -First: minutes
    // -Second: seconds
    public static Pair<Long, Long> msecToMinSec(long millis) {
        return new Pair<Long, Long>(TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis));
    }

    // Apparently this is the best method to determine if your service is running
    // http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android#5921190
    public static boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) App.getContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}