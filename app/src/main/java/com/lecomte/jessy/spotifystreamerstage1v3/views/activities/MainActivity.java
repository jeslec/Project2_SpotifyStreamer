package com.lecomte.jessy.spotifystreamerstage1v3.views.activities;


import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.ArtistInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.AudioPlayerService;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.ArtistSearchFragment;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.NowPlayingFragment;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.TopTracksFragment;

public class MainActivity extends AppCompatActivity implements
        ArtistSearchFragment.OnFragmentInteractionListener,
        TopTracksFragment.OnFragmentInteractionListener {

    private String mPreviousQuery;
    private ActionBar mActionBar;
    private static final String DIALOG_MEDIA_PLAYER = "mediaPlayer";

    /* ALWAYS SET THESE 3 VALUES WHEN YOU RE-USE (COPY & PASTE) THIS FILE */

    // 1- This is R.layout.<file name of the layout hosting the fragment>
    private static final int ACTIVITY_LAYOUT = R.layout.activity_main;

    // 2- This is R.id.<name of fragment container> from the activity file (set in step 1)
    private static final int[] FRAGMENT_CONTAINER_ARRAY = {
            R.id.main_fragment_container1,
            R.id.main_fragment_container2};

    // 3- Name of fragment file (<package_name>.<class name without .java>
    private static final String[] CLASS_NAME_ARRAY = {
            "com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.ArtistSearchFragment",
            "com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.TopTracksFragment"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fragments will use this to modify (color, textsize, etc.) the action bar
        mActionBar = getSupportActionBar();

        setContentView(ACTIVITY_LAYOUT);

        // Restore last query string
        if (savedInstanceState != null) {
            mPreviousQuery = savedInstanceState.getString("PreviousQueryString");
        }

        handleIntent(getIntent());

        // This fragment must be present in all layouts
        addFragmentToLayout(FRAGMENT_CONTAINER_ARRAY[0], CLASS_NAME_ARRAY[0]);

        if (App.isTwoPaneLayout()) {
            Utils.showToast("2-pane layout");

            // This fragment is present only in 2-pan layouts
            addFragmentToLayout(FRAGMENT_CONTAINER_ARRAY[1], CLASS_NAME_ARRAY[1]);
        }
    }

    private void addFragmentToLayout(int fragmentContainerId, String className) {

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(fragmentContainerId);

        if (fragment == null) {
            try {
                Class<?> fragmentClass = Class.forName(className);
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            fm.beginTransaction()
                    .add(fragmentContainerId, fragment)
                    .commit();
        }
    }

    // Got idea of how to manage first-time creation of activity vs re-calling same activity that's
    // currently displayed. This happens when a search is performed in the SearchableActivity
    // http://stackoverflow.com/questions/5094222/android-return-search-query-to-current-activity#7170471
    // Flag android:launchMode="singleTop" must be specified for the searchable activity in manifest
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            // Don't query server if we already have the results
            if (query.equals(mPreviousQuery)) {
                return;
            }

            // Get the search results fragment
            ArtistSearchFragment artistSearchFragment = (ArtistSearchFragment)
                    getSupportFragmentManager().findFragmentById(FRAGMENT_CONTAINER_ARRAY[0]);

            if (artistSearchFragment != null) {
                // Send the query so fragment can download results from Spotify and display them
                if (Utils.isInternetAvailable()) {
                    artistSearchFragment.updateSearchResult(query + "*");
                    mPreviousQuery = query;
                } else {
                    Utils.showToast(R.string.ArtistSearch_noInternet);
                }
            }
        }

        // Add NowPlaying activity/fragment loading code here
        else if (App.isTwoPaneLayout() &&
                intent.getAction().equals(TopTracksActivity.CUSTOM_ACTION_SHOW_PLAYER)) {

            // Get intent extras
            String artistName = intent.getStringExtra(TopTracksActivity.EXTRA_ARTIST_NAME);
            TrackInfo track = intent.getParcelableExtra(TopTracksActivity.EXTRA_TRACK_INFO);

            // Large layout: load NowPlaying fragment and show as a dialog
            FragmentManager fragmentManager = getSupportFragmentManager();
            NowPlayingFragment newFragment = NowPlayingFragment.newInstance(track, artistName);
            newFragment.show(fragmentManager, DIALOG_MEDIA_PLAYER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_artist, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Make sure to use android.support.v7.widget.SearchView and not android.widget.SearchView
        // or else the app will crash during run-time
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save last query string
        outState.putString("PreviousQueryString", mPreviousQuery);
    }

    @Override
    public ActionBar getTheActionBar() {
        return mActionBar;
    }

    public void onArtistSelected(ArtistInfo artist) {

        // Load TopTracks fragment in layout if it's a 2-pane layout or
        // start an activity that contains the TopTracks fragment if it's a single-pane layout

        // 2-pane layout: load TopTracks fragment in the layout's right pane
        if (App.isTwoPaneLayout()) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment oldTopTracks = fm.findFragmentById(FRAGMENT_CONTAINER_ARRAY[1]);
            Fragment newTopTracks = TopTracksFragment.newInstance(artist.getId(), artist.getName());

            if (oldTopTracks != null) {
                ft.remove(oldTopTracks);
            }

            ft.add(FRAGMENT_CONTAINER_ARRAY[1], newTopTracks);
            ft.commit();
        }

        // 1-pane layout: start an activity containing the TopTracks fragment
        else {
            Intent tracksIntent = new Intent(this, TopTracksActivity.class);
            tracksIntent.putExtra(TopTracksActivity.EXTRA_ARTIST_ID, artist.getId());
            tracksIntent.putExtra(TopTracksActivity.EXTRA_ARTIST_NAME, artist.getName());
            startActivity(tracksIntent);
        }
    }
}
