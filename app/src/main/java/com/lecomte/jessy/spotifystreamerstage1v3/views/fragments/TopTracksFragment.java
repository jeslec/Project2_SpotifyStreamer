package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
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
import com.lecomte.jessy.spotifystreamerstage1v3.controlers.TopTracksAdapter;
import com.lecomte.jessy.spotifystreamerstage1v3.models.NowPlayingFragmentData;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.AudioPlayerService;
import com.lecomte.jessy.spotifystreamerstage1v3.other.tasks.GetTopTracksTask;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.MainActivity;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.NowPlayingActivity;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.TopTracksActivity;

import java.util.ArrayList;

/**
 * Created by Jessy on 2015-06-23.
 */
public class TopTracksFragment extends ListFragment {

    //**********************************************************************************************
    // CONSTANTS
    //**********************************************************************************************

    private final String TAG = getClass().getSimpleName();
    // Data required by this fragment upon creation
    private static final String ARG_ARTIST_ID =
            "com.lecomte.jessy.spotifystreamerstage1v3.arg.ARG_ARTIST_ID";
    private static final String ARG_ARTIST_NAME =
            "com.lecomte.jessy.spotifystreamerstage1v3.arg.ARG_ARTIST_NAME";

    //**********************************************************************************************
    // VARIABLES
    //**********************************************************************************************

    //**** [Primitive] ****
    private boolean mIsNewTopTracksList = false;
    private int mPreviousTrackIndex = -1;

    //**** [Widgets] ****

    //**** [Other] ****
    private ArrayList<TrackInfo> mTopTracks;
    private String mArtistId;
    private String mArtistName;
    private String mPreviousArtistId;
    private TopTracksAdapter mTopTracksAdapter;
    private OnFragmentInteractionListener mListener;

    //**********************************************************************************************
    // CONSTRUCTORS
    //**********************************************************************************************

    public TopTracksFragment() {

    }

    //**********************************************************************************************
    // FRAMEWORK METHODS
    //**********************************************************************************************

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Utils.log(TAG, "onCreate()");
        setRetainInstance(true);

        // Required to get action bar back button to do something useful (go back to previous view)
        //setHasOptionsMenu(true);

        // Get params sent to this fragment upon creation, namely the artist id and name
         if (getArguments() != null) {
            mArtistId = getArguments().getString(ARG_ARTIST_ID);
            mArtistName = getArguments().getString(ARG_ARTIST_NAME);
        }

        else {
             mArtistId = getActivity().getIntent().getStringExtra(TopTracksActivity.EXTRA_ARTIST_ID);
             mArtistName = getActivity().getIntent().getStringExtra(TopTracksActivity.EXTRA_ARTIST_NAME);
             //Utils.log(TAG, "onCreate() - Intent extras received: [artistId: "
                     //+ mArtistId + "] " + "[artistName: " + mArtistName + "]");
         }

        // Set up the adapter to display the top tracks for an artist
        mTopTracksAdapter = new TopTracksAdapter(getActivity());
        setListAdapter(mTopTracksAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        // Put artist name under the action bar title
        ActionBar actionBar = mListener.getTheActionBar();
        actionBar.setSubtitle(mArtistName);

        // Use action bar arrow as navigation button, but only in 1-pane layouts
        // In 2-pane layout, there is only 1 activity with 2 fragments so no screen to go back to
        if (!App.isTwoPaneLayout()) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Utils.log(TAG, "onViewCreated()");

        if (mArtistId != null && !mArtistId.equals(mPreviousArtistId)) {
            // Get top tracks of this artist
            if (Utils.isInternetAvailable()) {
                new GetTopTracksTask(mTopTracksAdapter, this).execute(mArtistId);
                mPreviousArtistId = mArtistId;
                mIsNewTopTracksList = true;
            }
            else {
                Utils.showToast(R.string.TopTracks_noInternet);
            }
        }
    }

    @Override
    public void onPause() {
        //Utils.log(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onResume() {
        //Utils.log(TAG, "onResume()");
        super.onResume();

        // TODO: Determine if it's the best place to put this
        // Force update of the menu when coming from NowPlaying view
        // Required so the NowPlaying button gets displayed in the ActionBar
        getActivity().invalidateOptionsMenu();
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
        //Utils.log(TAG, "onDestroy()");
        super.onDestroy();
    }

    /*
    * ACTION_LOAD_PLAYLIST_PLAY_TRACK: user wants to play a track in a new playlist. This action
    *                                   requires the new playlist and the track index.
    *
    * ACTION_PLAY_TRACK: user wants to play a new track in the current playlist. This action
    *                   requires the track index.
    *
    * ACTION_SHOW_PLAYER: user selected the same track that's currently playing. This action
    *                       does not require any data.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        boolean isNewTrack = position != mPreviousTrackIndex? true : false;

        // 2-pane layout: ask MainActivity to load NowPlaying fragment in its layout
        // 1-pane layout: start a fullscreen activity with NowPlaying fragment inside
        Intent intent = new Intent(getActivity(),
                App.isTwoPaneLayout()? MainActivity.class : NowPlayingActivity.class);
        intent.setAction(NowPlayingFragment.ACTION_SHOW_PLAYER);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        NowPlayingFragmentData fragmentData = new NowPlayingFragmentData();
        fragmentData.setTrackIndex(position);

        if (mIsNewTopTracksList) {
            intent.setAction(NowPlayingFragment.ACTION_LOAD_PLAYLIST_PLAY_TRACK);

            // Put all tracks in a list so we can send it to the NowPlaying fragment
            final int itemsCount = getListAdapter().getCount();

            for (int i=0; i<itemsCount; i++) {
                fragmentData.getTrackList().add((TrackInfo)getListAdapter().getItem(i));
            }
        }

        else if (isNewTrack) {
            intent.setAction(NowPlayingFragment.ACTION_PLAY_TRACK);
        }

        //Utils.log(TAG, "onListItemClick() - Intent action set to: "
                //+ intent.getAction().substring(intent.getAction().lastIndexOf(".") + 1));

        intent.putExtra(NowPlayingFragment.EXTRA_FRAGMENT_DATA, fragmentData);
        startActivity(intent);

        // The current top tracks list has been processed
        mIsNewTopTracksList = false;
        mPreviousTrackIndex = position;
    }

    //**********************************************************************************************
    // NON-FRAMEWORK METHODS
    //**********************************************************************************************

    // Making the ProgressBar work...
    // Creating 2 methods in fragment and passing fragment to AsyncTask idea was obtained here:
    //http://www.mobiledeveloperguide.com/android/using-asynctask-and-fragments.html
    public void showProgressBar() {
        ProgressBar progress = (ProgressBar)getActivity()
                .findViewById(R.id.TopTracks_progressBar);
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
    }

    public void hideProgressBar() {
        ProgressBar progress = (ProgressBar)getActivity()
                .findViewById(R.id.TopTracks_progressBar);
        progress.setVisibility(View.GONE);
    }

    public static TopTracksFragment newInstance(String artistId, String artistName) {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST_ID, artistId);
        args.putString(ARG_ARTIST_NAME, artistName);
        fragment.setArguments(args);
        return fragment;
    }

    //**********************************************************************************************
    // INTERFACES
    //**********************************************************************************************

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
        public ActionBar getTheActionBar();
    }

    // Based on book Big Nerd Ranch Android: p.274-275
    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // The left-pointing arrow located to the left of the action bar title
            case android.R.id.home:
                // This activity's parent must be specified in meta-data section of manifest
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    // Go back to the previous (parent) view
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/
}


