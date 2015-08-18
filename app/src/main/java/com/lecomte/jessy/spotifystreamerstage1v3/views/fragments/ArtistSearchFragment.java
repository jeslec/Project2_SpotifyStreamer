package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.controlers.SearchResultAdapter;
import com.lecomte.jessy.spotifystreamerstage1v3.models.ArtistInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.AudioPlayerService;
import com.lecomte.jessy.spotifystreamerstage1v3.other.tasks.SearchArtistTask;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.MainActivity;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.NowPlayingActivity;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.SettingsActivity;

/**
 * Created by Jessy on 2015-06-23.
 */
public class ArtistSearchFragment extends ListFragment {

    private final String TAG = getClass().getSimpleName();
    SearchResultAdapter mSearchResultAdapter;

   /* // Data required by this fragment upon creation
    private static final String ARG_ARTIST_ID =
            "com.lecomte.jessy.spotifystreamerstage1v3.arg.ArtistId";
    private static final String ARG_ARTIST_NAME =
            "com.lecomte.jessy.spotifystreamerstage1v3.arg.ArtistName";
*/
    // TODO: Rename and change types of parameters
    /*private String mArtistId;
    private String mArtistName;
*/
    private OnFragmentInteractionListener mListener;
    private boolean mIsTrackPlaying = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ArtistSearchFragment newInstance(String param1, String param2) {
        ArtistSearchFragment fragment = new ArtistSearchFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_ARTIST_ID, param1);
        args.putString(ARG_ARTIST_NAME, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    public ArtistSearchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        // Tell fragment manager to call this fragment's onCreateOptionsMenu()
        setHasOptionsMenu(true);

        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/

        // Set up the adapter to display search results sent to us by search fragment
        mSearchResultAdapter = new SearchResultAdapter(getActivity());
        setListAdapter(mSearchResultAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_result, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        if (!Utils.isInternetAvailable()) {
            Utils.showToast(R.string.TopTracks_noInternet);
            return;
        }

        // Send the selected artist to the activity so it can take the appropriate action
        ArtistInfo artistInfo = (ArtistInfo)getListAdapter().getItem(position);
        mListener.onArtistSelected(artistInfo);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + TAG + ".OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateSearchResult(String query){
        new SearchArtistTask(mSearchResultAdapter, this).execute(query);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);

        public void onArtistSelected(ArtistInfo artist);
    }

    public void showProgressBar() {
        ProgressBar progress = (ProgressBar)getActivity()
                .findViewById(R.id.SearchResult_progressBar);
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
    }

    public void hideProgressBar() {
        ProgressBar progress = (ProgressBar)getActivity()
                .findViewById(R.id.SearchResult_progressBar);
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_artist_search_fragment, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        // Make sure to use android.support.v7.widget.SearchView and not android.widget.SearchView
        // or else the app will crash during run-time
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_item_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Utils.log(TAG, "onOptionsItemSelected() - Item: " + item.getTitle());

        switch (item.getItemId()) {
            case R.id.menu_item_preferences:
                Utils.log(TAG, "onOptionsItemSelected() - Display preferences dialog...");
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.menu_item_now_playing:
                Utils.log(TAG, "onOptionsItemSelected() - Show Now Playing view...");
                /*Intent nowPlayingIntent = new Intent(getActivity(), NowPlayingActivity.class);
                startActivity(nowPlayingIntent);*/

                // TODO: Tell the MainActivity to load the NowPlaying fragment in his layout
                if (App.isTwoPaneLayout()) {

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setAction(NowPlayingFragment.ACTION_SHOW_PLAYER);
                    startActivity(intent);

                    /*Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra(TopTracksActivity.EXTRA_ARTIST_NAME, mArtistName);
                    intent.putParcelableArrayListExtra(TopTracksActivity.EXTRA_TRACK_LIST,
                            trackInfoList);
                    intent.putExtra(TopTracksActivity.EXTRA_TRACK_INDEX, position);
                    intent.setAction(TopTracksActivity.ACTION_LOAD_PLAYLIST_PLAY_TRACK);
                    startActivity(intent);*/
                }

                // Start the NowPlaying screen as a fullscreen activity
                else {
                    // TODO: Check if I should send an extras to NowPlaying
                    Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
                    intent.setAction(NowPlayingFragment.ACTION_SHOW_PLAYER);
                    startActivity(intent);
                    /*Intent nowPlayingIntent = new Intent(getActivity(), NowPlayingActivity.class);
                    nowPlayingIntent.putExtra(TopTracksActivity.EXTRA_ARTIST_NAME, mArtistName);
                    nowPlayingIntent.putParcelableArrayListExtra(TopTracksActivity.EXTRA_TRACK_LIST,
                            trackInfoList);
                    nowPlayingIntent.putExtra(TopTracksActivity.EXTRA_TRACK_INDEX, position);
                    startActivity(nowPlayingIntent);*/
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem nowPlayingItem = menu.findItem(R.id.menu_item_now_playing);

        // Show the NowPlaying icon only if the audio service is running
        if (Utils.isServiceRunning(AudioPlayerService.class)) {
            nowPlayingItem.setVisible(true);
        } else {
            nowPlayingItem.setVisible(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO: Determine if it's the best place to put this
        // Force update of the menu when coming from NowPlaying view
        // Required so the NowPlaying button gets displayed in the ActionBar
        getActivity().invalidateOptionsMenu();
    }
}


