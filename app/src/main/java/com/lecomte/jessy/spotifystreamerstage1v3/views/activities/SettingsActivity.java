package com.lecomte.jessy.spotifystreamerstage1v3.views.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;

/**
 * Created by Jessy on 2015-08-04.
 */
// We extend from Activity and not PreferenceActivity to fix a visual bug in landscape mode
// http://stackoverflow.com/questions/21947003/preference-is-not-displayed-correctly-on-tablet#22179515
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            // Load default values into the dialog only if user has not chosen any values yet
            //PreferenceManager.setDefaultValues(App.getContext(), R.xml.preferences, false);
        }
    }
}
