package com.lecomte.jessy.spotifystreamerstage1v3.other.utils;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;

import java.util.List;

/**
 * Created by Jessy on 2015-07-08.
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
}