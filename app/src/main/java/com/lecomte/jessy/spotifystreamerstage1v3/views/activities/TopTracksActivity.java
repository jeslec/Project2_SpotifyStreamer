package com.lecomte.jessy.spotifystreamerstage1v3.views.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.other.AudioPlayerService;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.NowPlayingFragment;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.TopTracksFragment;


public class TopTracksActivity extends AppCompatActivity implements
        TopTracksFragment.OnFragmentInteractionListener {

    private final String TAG = getClass().getSimpleName();

    public static final String EXTRA_ARTIST_ID =
            "com.lecomte.jessy.spotifystreamerstage1v3.extra.EXTRA_ARTIST_ID";
    public static final String EXTRA_ARTIST_NAME =
            "com.lecomte.jessy.spotifystreamerstage1v3.extra.EXTRA_ARTIST_NAME";

    public static final String EXTRA_TRACK_INFO =
            "com.lecomte.jessy.spotifystreamerstage1v3.extra.EXTRA_TRACK_INFO";

    public static final String EXTRA_TRACK_LIST =
            "com.lecomte.jessy.spotifystreamerstage1v3.extra.EXTRA_TRACK_LIST";

    public static final String EXTRA_TRACK_INDEX =
            "com.lecomte.jessy.spotifystreamerstage1v3.action.EXTRA_TRACK_INDEX";

    private ActionBar mActionBar;

    /* ALWAYS SET THESE 3 VALUES WHEN YOU RE-USE (COPY & PASTE) THIS FILE */

    // 1- This is R.layout.<file name of the layout hosting the fragment>
    private static final int ACTIVITY_LAYOUT = R.layout.activity_top_tracks;

    // 2- This is R.id.<name of fragment container> from the activity file (set in step 1)
    private static final int[] FRAGMENT_CONTAINER_ARRAY = {
            R.id.toptracks_fragment_container};

    // 3- Name of fragment file (<package_name>.<class name without .java>
    private static final String[] CLASS_NAME_ARRAY = {
            "com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.TopTracksFragment" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The top tracks fragment will use this to set the action bar subtitle
        mActionBar = getSupportActionBar();

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
    public ActionBar getTheActionBar() {
        return mActionBar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks_activity, menu);

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem nowPlayingItem = menu.findItem(R.id.menu_item_now_playing);

        // Show the NowPlaying icon only if the audio service is running
        if (Utils.isServiceRunning(AudioPlayerService.class)) {
            nowPlayingItem.setVisible(true);
        } else {
            nowPlayingItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Utils.log(TAG, "onOptionsItemSelected() - Item: " + item.getTitle());

        switch (item.getItemId()) {
            case R.id.menu_item_now_playing:
                //Utils.log(TAG, "onOptionsItemSelected() - Show Now Playing view...");
                Intent intent = new Intent(this,
                        App.isTwoPaneLayout()? MainActivity.class: NowPlayingActivity.class);
                intent.setAction(NowPlayingFragment.ACTION_SHOW_PLAYER);
                startActivity(intent);
                return true;

            case R.id.menu_item_preferences:
                //Utils.log(TAG, "onOptionsItemSelected() - Display preferences dialog...");
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

