package com.lecomte.jessy.spotifystreamerstage1v3.other;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;

import java.lang.ref.WeakReference;

/**
 * Created by Jessy on 2015-09-07.
 */
public class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final WeakReference<Context> mContext;

    public PreferenceChangeListener(Context context) {
        mContext = new WeakReference<Context>(context);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        if (key.equals("preferences_notificationsEnabled") &&
                Utils.isServiceRunning(AudioPlayerService.class)) {
            boolean notificationsEnabled = prefs.getBoolean("preferences_notificationsEnabled", true);
            //Utils.log(TAG, "OnSharedPreferenceChangeListener() - Notifications enabled: " + notificationsEnabled);
            Intent intent = new Intent(mContext.get(), AudioPlayerService.class);
            String intentAction = AudioPlayerService.ACTION_HIDE_NOTIFICATION;

            if (notificationsEnabled) {
                intentAction = AudioPlayerService.ACTION_SHOW_NOTIFICATION;
            }

            intent.setAction(intentAction);
            mContext.get().startService(intent);
        }
    }
}
