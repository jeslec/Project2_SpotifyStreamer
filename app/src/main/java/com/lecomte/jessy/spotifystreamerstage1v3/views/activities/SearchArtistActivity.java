package com.lecomte.jessy.spotifystreamerstage1v3.views.activities;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.SearchResultFragment;

public class SearchArtistActivity extends AppCompatActivity implements
        SearchResultFragment.OnFragmentInteractionListener{

    private String mPreviousQuery;

    /* ALWAYS SET THESE 3 VALUES WHEN YOU RE-USE (COPY & PASTE) THIS FILE */

    // 1- This is R.layout.<file name of the layout hosting the fragment>
    private static final int ACTIVITY_LAYOUT = R.layout.activity_search_artist;

    // 2- This is R.id.<name of fragment container> from the activity file (set in step 1)
    private static final int[] FRAGMENT_CONTAINER_ARRAY = {
            R.id.search_result_fragment_container};

    // 3- Name of fragment file (<package_name>.<class name without .java>
    private static final String[] CLASS_NAME_ARRAY = {
            "com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.SearchResultFragment"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_artist);

        // Restore last query string
        if (savedInstanceState != null) {
            mPreviousQuery = savedInstanceState.getString("PreviousQueryString");
        }

        handleIntent(getIntent());

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

            //new SearchArtistAsyncTask().execute(query + "*");
            // Get the search results fragment
            SearchResultFragment searchResultFragment = (SearchResultFragment)
                    getSupportFragmentManager().findFragmentById(FRAGMENT_CONTAINER_ARRAY[0]);

            if (searchResultFragment != null) {
                // Send the query so fragment can download results from Spotify and display them
                searchResultFragment.updateSearchResult(query);
                mPreviousQuery = query;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_searchable, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Make sure to use android.support.v7.widget.SearchView and not android.widget.SearchView
        // or else the app will crash during run-time
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save last query string
        outState.putString("PreviousQueryString", mPreviousQuery);
    }

    /*@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore last query string
        mPreviousQuery = savedInstanceState.getString("PreviousQueryString");
    }*/
}
