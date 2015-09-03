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
    private OnFragmentInteractionListener mListener;
    private boolean mIsTrackPlaying = false;

    public ArtistSearchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.log(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

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

    @Override
    public void onDestroy() {
        Utils.log(TAG, "onDestroy()");
        super.onDestroy();
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
    public void onPause() {
        Utils.log(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onResume() {
        Utils.log(TAG, "onResume()");
        super.onResume();

        // TODO: Determine if it's the best place to put this
        // Force update of the menu when coming from NowPlaying view
        // Required so the NowPlaying button gets displayed in the ActionBar
        getActivity().invalidateOptionsMenu();
    }
}


