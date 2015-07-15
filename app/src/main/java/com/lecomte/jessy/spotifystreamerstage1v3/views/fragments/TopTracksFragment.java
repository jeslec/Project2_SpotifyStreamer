package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;


import android.app.Activity;
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

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.controlers.TopTracksAdapter;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.tasks.GetTopTracksTask;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
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

    public TopTracksFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Required to get action bar back button to do something useful (go back to prev view)
        setHasOptionsMenu(true);

        mArtistId = getActivity().getIntent().getStringExtra(TopTracksActivity.EXTRA_ARTIST_ID);
        mArtistName = getActivity().getIntent().getStringExtra(TopTracksActivity.EXTRA_ARTIST_NAME);
        Utils.log(TAG, "onCreate() - Intent extras received: [artistId: "
                + mArtistId + "] " + "[artistName: " + mArtistName + "]");

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

        actionBar.setDisplayHomeAsUpEnabled(true);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!mArtistId.equals(mPreviousArtistId)) {
            // Get top tracks of this artist
            if (Utils.isInternetAvailable()) {
                new GetTopTracksTask(mTopTracksAdapter, this).execute(mArtistId);
                mPreviousArtistId = mArtistId;
            }
            else {
                Utils.showToast(R.string.no_internet_no_tracks);
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TrackInfo trackInfo = (TrackInfo) getListAdapter().getItem(position);
        String msg = getResources().getString(R.string.track_playing, trackInfo.getTrackName(),
                trackInfo.getAlbumName());
        Utils.showToast(msg);
    }

    // Making the ProgressBar work...
    // Creating 2 methods in fragment and passing fragment to AsyncTask idea was obtained here:
    //http://www.mobiledeveloperguide.com/android/using-asynctask-and-fragments.html
    public void showProgressBar() {
        ProgressBar progress = (ProgressBar)getActivity()
                .findViewById(R.id.TopTracksFragment_ProgressBar);
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
    }

    public void hideProgressBar() {
        ProgressBar progress = (ProgressBar)getActivity()
                .findViewById(R.id.TopTracksFragment_ProgressBar);
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
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
}


