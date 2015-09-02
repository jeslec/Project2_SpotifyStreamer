package com.lecomte.jessy.spotifystreamerstage1v3.views.activities;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.ArtistInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.models.NowPlayingFragmentData;
import com.lecomte.jessy.spotifystreamerstage1v3.other.AudioPlayerService;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.ArtistSearchFragment;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.NowPlayingFragment;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.TopTracksFragment;

public class MainActivity extends AppCompatActivity implements
        ArtistSearchFragment.OnFragmentInteractionListener,
        TopTracksFragment.OnFragmentInteractionListener {

    private final String TAG = getClass().getSimpleName();
    private String mPreviousQuery;
    private ActionBar mActionBar;
    private boolean mCouldBeConfigurationChanged = false;
    private static final String DIALOG_MEDIA_PLAYER = "mediaPlayer";
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener;

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
        Utils.log(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        // Fragments will use this to modify (color, textsize, etc.) the action bar
        mActionBar = getSupportActionBar();

        setContentView(ACTIVITY_LAYOUT);

        // Restore last query string
        if (savedInstanceState != null) {
            mPreviousQuery = savedInstanceState.getString("PreviousQueryString");
        }

        // IMPORTANT: THIS MUST BE DONE BEFORE handleIntent()
        // If there's a stopService timer running, it means we just had a configuration change
        // So kill the timer so the service does not get stopped
        // Only call this when service is already running or else it will start the service!
        // Service could be running for 2 reasons: 1) configuration change or
        // 2) app was loaded from recent tasks
        if (Utils.isServiceRunning(AudioPlayerService.class)) {
            Intent cancelTimerIntent = new Intent(this, AudioPlayerService.class);
            cancelTimerIntent.setAction(AudioPlayerService.ACTION_CANCEL_TIMER);
            startService(cancelTimerIntent);
            mCouldBeConfigurationChanged = true;
        }

        handleIntent(getIntent());

        // This fragment must be present in all layouts
        addFragmentToLayout(FRAGMENT_CONTAINER_ARRAY[0], CLASS_NAME_ARRAY[0]);

        if (App.isTwoPaneLayout()) {
            Utils.log(TAG, "onCreate() - Layout configuration: 2-pane");

            // This fragment is present only in 2-pan layouts
            addFragmentToLayout(FRAGMENT_CONTAINER_ARRAY[1], CLASS_NAME_ARRAY[1]);
        }

        // React to changes made to the app settings
        mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                if (key.equals("preferences_notificationsEnabled") &&
                        Utils.isServiceRunning(AudioPlayerService.class)) {
                    boolean notificationsEnabled = prefs.getBoolean("preferences_notificationsEnabled", true);
                    Utils.log(TAG, "OnSharedPreferenceChangeListener() - Notifications enabled: " + notificationsEnabled);
                    Intent intent = new Intent(App.getContext(), AudioPlayerService.class);
                    String intentAction = AudioPlayerService.ACTION_HIDE_NOTIFICATION;

                    if (notificationsEnabled) {
                        intentAction = AudioPlayerService.ACTION_SHOW_NOTIFICATION;
                    }

                    intent.setAction(intentAction);
                    startService(intent);
                }
            }
        };

        PreferenceManager.getDefaultSharedPreferences(App.getContext())
                .registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);

        // Load default values into the dialog only if user has not chosen any values yet
        PreferenceManager.setDefaultValues(App.getContext(), R.xml.preferences, false);
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

    @Override
    protected void onPause() {
        Utils.log(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Utils.log(TAG, "onResume()");
        super.onResume();
    }

    void addNowPlayingFragment(boolean bWithExtras, boolean bAllowStateLoss) {
        NowPlayingFragment newFragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();

        if (bWithExtras) {
            NowPlayingFragmentData fragmentData = new NowPlayingFragmentData();
            fragmentData = intent.getParcelableExtra(NowPlayingFragment.EXTRA_FRAGMENT_DATA);
            newFragment = NowPlayingFragment.newInstance(fragmentData);
        } else {
            newFragment = NowPlayingFragment.newInstance();
        }

        NowPlayingFragment oldFragment = (NowPlayingFragment)fragmentManager
                .findFragmentByTag(DIALOG_MEDIA_PLAYER);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (oldFragment != null) {
            fragmentTransaction.remove(oldFragment);
        }

        fragmentTransaction.add(newFragment, DIALOG_MEDIA_PLAYER);

        if (bAllowStateLoss) {
            fragmentTransaction.commitAllowingStateLoss();
        } else {
            fragmentTransaction.commit();
        }
    }

    // Got idea of how to manage first-time creation of activity vs re-calling same activity that's
    // currently displayed. This happens when a search is performed in the SearchableActivity
    // http://stackoverflow.com/questions/5094222/android-return-search-query-to-current-activity#7170471
    // Flag android:launchMode="singleTop" must be specified for the searchable activity in manifest
    @Override
    protected void onNewIntent(Intent intent) {

        // Fixes illegalStateException which was forceing me to use commitWithStateLoss()
        // See docs for onNewIntent() to get more info about this problem
        super.onNewIntent(intent);

        setIntent(intent);
        handleIntent(intent);
        //Utils.log(TAG, "onNewIntent()");
    }

    private void handleIntent(Intent intent) {

        String intentAction = intent.getAction();

        if (intentAction == null) {
            Utils.log(TAG, "handleIntent() - Intent action is null!");
            return;
        }

        Utils.log(TAG, "handleIntent() - Intent action: "
                + intentAction.substring(intentAction.lastIndexOf(".") + 1));

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

        else if (App.isTwoPaneLayout()) {

            FragmentManager fragmentManager = getSupportFragmentManager();

            // Special case: app was launched from recent apps drawer. Overwrite intent action or
            // else we will get last intent action used before exiting the app
            if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) ==
                    Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
                if (PreferenceManager.getDefaultSharedPreferences(App.getContext())
                        .getBoolean("preferences_notificationsEnabled", true)) {
                    intentAction = NowPlayingFragment.ACTION_SHOW_PLAYER;
                } else {
                    intentAction = NowPlayingFragment.ACTION_SHOW_PLAYER_RECENT_APPS_CASE;
                }
                Utils.log(TAG, "handleIntent() - App was launched from recent tasks, overwriting intent action to: "
                        + intentAction.substring(intentAction.lastIndexOf(".") + 1));
            }

            // If app wasn't loaded from recent apps drawer then the only other way
            // mCouldBeConfigurationChanged can be true is if a configuration change occurred
            else if (mCouldBeConfigurationChanged) {
                Utils.log(TAG, "onHandleIntent() - Configuration changed");
                // Only load NowPlaying if it was visible before going to background (or exiting)
                if (fragmentManager.findFragmentByTag(DIALOG_MEDIA_PLAYER) != null) {
                    addNowPlayingFragment(false, false);
                }
            }

            // App not loaded from recent apps drawer or after a configuration change
            else {

                if (intentAction.equals(NowPlayingFragment.ACTION_LOAD_PLAYLIST_PLAY_TRACK) ||
                        intent.getAction().equals(NowPlayingFragment.ACTION_PLAY_TRACK)) {

                    addNowPlayingFragment(true, false);
                }

                else if (intentAction.equals(NowPlayingFragment.ACTION_SHOW_PLAYER)) {

                    addNowPlayingFragment(false, false);
                }

                else if (intentAction.equals(NowPlayingFragment.ACTION_SHOW_PLAYER_ICON_CASE)) {

                    addNowPlayingFragment(false, false);
                }

                else if (intentAction.equals(NowPlayingFragment.ACTION_SHOW_PLAYER_NOTIFICATION_CASE)) {

                    // Only load NowPlaying if it was visible before going to background
                    if (fragmentManager.findFragmentByTag(DIALOG_MEDIA_PLAYER) != null) {
                        addNowPlayingFragment(false, false);
                    }
                }
            }

            mCouldBeConfigurationChanged = false;
        }
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

    @Override
    protected void onDestroy() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        Utils.log(TAG, "onDestroy() - [Audio service running: "
                + Utils.isServiceRunning(AudioPlayerService.class) + "]"
                + " [Notifications enabled: "
                + prefs.getBoolean("preferences_notificationsEnabled", true) + "]");

        // Stop service if notifications are OFF (no way of controlling player is app not running)
        // TODO: Find a solution to stop the service (be careful with configuration changes)
        if (Utils.isServiceRunning(AudioPlayerService.class) &&
                !prefs.getBoolean("preferences_notificationsEnabled", true)) {
            Intent stopServiceIntent = new Intent(this, AudioPlayerService.class);
            stopServiceIntent.setAction(AudioPlayerService.ACTION_STOP_SERVICE);
            startService(stopServiceIntent);
            Utils.log(TAG, "onDestroy() - Sent action to service: ACTION_STOP_SERVICE");
        }

        prefs.unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Utils.log(TAG, "onCreate()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Make sure to use android.support.v7.widget.SearchView and not android.widget.SearchView
        // or else the app will crash during run-time
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_item_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem nowPlayingItem = menu.findItem(R.id.menu_item_now_playing);

        if (nowPlayingItem == null) {
            Utils.log(TAG, "onPrepareOptionsMenu() - nowPlayingItem is null!");
            return true;
        }

        // Show the NowPlaying icon only if the audio service is running
        if (Utils.isServiceRunning(AudioPlayerService.class)) {
            nowPlayingItem.setVisible(true);
            //Utils.log(TAG, "onPrepareOptionsMenu() - NowPlaying menu item set to: VISIBLE");
        } else {
            nowPlayingItem.setVisible(false);
            //Utils.log(TAG, "onPrepareOptionsMenu() - NowPlaying menu item set to: HIDDEN");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Utils.log(TAG, "onOptionsItemSelected() - Item: " + item.getTitle());

        switch (item.getItemId()) {
            case R.id.menu_item_preferences:
                Utils.log(TAG, "onOptionsItemSelected() - Display preferences dialog...");
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.menu_item_now_playing:
                Utils.log(TAG, "onOptionsItemSelected() - Show Now Playing view...");
                Intent intent = new Intent(this,
                        App.isTwoPaneLayout()? MainActivity.class: NowPlayingActivity.class);
                intent.setAction(NowPlayingFragment.ACTION_SHOW_PLAYER_ICON_CASE);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
