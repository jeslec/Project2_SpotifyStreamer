package com.lecomte.jessy.spotifystreamerstage1v3.other.utils;

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

    public static void showToast(int stringId) {
        Toast toast = Toast.makeText(App.getContext(), stringId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, (int)App.getRes().getDimension(R.dimen.toast_offset_x),
                (int)App.getRes().getDimension(R.dimen.toast_offset_y));
        toast.show();
    }

    public static void showToast(String msg) {
        Toast toast = Toast.makeText(App.getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, (int) App.getRes().getDimension(R.dimen.toast_offset_x),
                (int) App.getRes().getDimension(R.dimen.toast_offset_y));
        toast.show();
    }

    public static void log(String tag, String msg) {
        if (App.getRes().getBoolean(R.bool.show_debug_strings)) {
            Log.d(tag, msg);
        }
    }

    public static void logLoop(String tag, String preIndex, String postIndex, List<?> list) {
        if (App.getRes().getBoolean(R.bool.show_debug_strings)) {
            for (int i = 0; i < list.size(); i++) {
                Log.d(tag, preIndex + String.format("%1$02d", i) +
                        postIndex + list.get(i).toString());
            }
        }
    }
}