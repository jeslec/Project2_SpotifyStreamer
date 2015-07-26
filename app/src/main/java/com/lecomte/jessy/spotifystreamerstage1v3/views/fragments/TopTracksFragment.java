package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.controlers.TopTracksAdapter;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
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

    private final String TAG = getClass().getSimpleName();
    private ArrayList<TrackInfo> mTopTracks;
    private String mArtistId;
    private String mArtistName;
    private String mPreviousArtistId;
    private TopTracksAdapter mTopTracksAdapter;
    private OnFragmentInteractionListener mListener;

    // Data required by this fragment upon creation
    private static final String ARG_ARTIST_ID =
            "com.lecomte.jessy.spotifystreamerstage1v3.arg.artistId";
    private static final String ARG_ARTIST_NAME =
            "com.lecomte.jessy.spotifystreamerstage1v3.arg.artistName";

    public TopTracksFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TopTracksFragment newInstance(String artistId, String artistName) {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST_ID, artistId);
        args.putString(ARG_ARTIST_NAME, artistName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.log(TAG, "onCreate()");
        setRetainInstance(true);

        // Required to get action bar back button to do something useful (go back to previous view)
        setHasOptionsMenu(true);

        // Get params sent to this fragment upon creation, namely the artist id and name
         if (getArguments() != null) {
            mArtistId = getArguments().getString(ARG_ARTIST_ID);
            mArtistName = getArguments().getString(ARG_ARTIST_NAME);
        }

        else {
             mArtistId = getActivity().getIntent().getStringExtra(TopTracksActivity.EXTRA_ARTIST_ID);
             mArtistName = getActivity().getIntent().getStringExtra(TopTracksActivity.EXTRA_ARTIST_NAME);
             Utils.log(TAG, "onCreate() - Intent extras received: [artistId: "
                     + mArtistId + "] " + "[artistName: " + mArtistName + "]");
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

        // Use action bar arrow as navigation button
        actionBar.setDisplayHomeAsUpEnabled(true);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Utils.log(TAG, "onViewCreated()");

        if (mArtistId != null && !mArtistId.equals(mPreviousArtistId)) {
            // Get top tracks of this artist
            if (Utils.isInternetAvailable()) {
                new GetTopTracksTask(mTopTracksAdapter, this).execute(mArtistId);
                mPreviousArtistId = mArtistId;
            }
            else {
                Utils.showToast(R.string.TopTracks_noInternet);
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TrackInfo trackInfo = (TrackInfo) getListAdapter().getItem(position);

        // Put all tracks in a list so we can send it to the NowPlaying fragment
        int itemsCount = getListAdapter().getCount();
        ArrayList<TrackInfo> trackInfoList = new ArrayList<TrackInfo>(itemsCount);

        for (int i=0; i<itemsCount; i++) {
            trackInfoList.add((TrackInfo)getListAdapter().getItem(i));
        }

        // Tell the MainActivity to load the NowPlaying fragment in his layout
        if (App.isTwoPaneLayout()) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(TopTracksActivity.EXTRA_ARTIST_NAME, mArtistName);
            intent.putParcelableArrayListExtra(TopTracksActivity.EXTRA_TRACK_LIST,
                    trackInfoList);
            intent.putExtra(TopTracksActivity.EXTRA_TRACK_INDEX, position);
            intent.setAction(TopTracksActivity.CUSTOM_ACTION_SHOW_PLAYER);
            startActivity(intent);
        }

        // Start the NowPlaying screen as a fullscreen activity
        else {
            Intent nowPlayingIntent = new Intent(getActivity(), NowPlayingActivity.class);
            nowPlayingIntent.putExtra(TopTracksActivity.EXTRA_ARTIST_NAME, mArtistName);
            nowPlayingIntent.putParcelableArrayListExtra(TopTracksActivity.EXTRA_TRACK_LIST,
                    trackInfoList);
            nowPlayingIntent.putExtra(TopTracksActivity.EXTRA_TRACK_INDEX, position);
            startActivity(nowPlayingIntent);
        }
    }

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
    @Override
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
    }

    /*@Override
    public void onPause() {
        super.onPause();

        App.setIsVisible(false, TAG);
    }

    @Override
    public void onResume() {
        super.onResume();

        App.setIsVisible(true, TAG);
    }*/
}


