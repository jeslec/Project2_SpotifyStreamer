package com.lecomte.jessy.spotifystreamerstage1v3.views.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.lecomte.jessy.spotifystreamerstage1v3.R;

public class TopTracksActivity extends AppCompatActivity {

    public static final String EXTRA_ARTIST_ID = "com.lecomte.jessy.spotifystreamerstage1v3.ArtistId";

    /* ALWAYS SET THESE 3 VALUES WHEN YOU RE-USE (COPY & PASTE) THIS FILE */

    // 1- This is R.layout.<file name of the layout hosting the fragment>
    private static final int ACTIVITY_LAYOUT = R.layout.activity_top_tracks;

    // 2- This is R.id.<name of fragment container> from the activity file (set in step 1)
    private static final int[] FRAGMENT_CONTAINER_ARRAY = {
            R.id.top_tracks_fragment_container };

    // 3- Name of fragment file (<package_name>.<class name without .java>
    private static final String[] CLASS_NAME_ARRAY = {
            "com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.TopTracksFragment" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ACTIVITY_LAYOUT);

        FragmentManager fm = getSupportFragmentManager();

        for (int i=0; i<FRAGMENT_CONTAINER_ARRAY.length; i++) {

            Fragment fragment = fm.findFragmentById(FRAGMENT_CONTAINER_ARRAY[i]);

            if (fragment == null) {

                try {
                    Class<?> fragmentClass = Class.forName(CLASS_NAME_ARRAY[i]);
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                fm.beginTransaction()
                        .add(FRAGMENT_CONTAINER_ARRAY[i], fragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

