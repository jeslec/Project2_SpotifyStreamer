package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
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
    private String mPreviousArtistId;
    private TopTracksAdapter mTopTracksAdapter;

    public TopTracksFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mArtistId = getActivity().getIntent().getStringExtra(TopTracksActivity.EXTRA_ARTIST_ID);
        Utils.log(TAG, "TracksFragment.onCreate() - Received from SearchFragment, artistId: " + mArtistId);

        // Set up the adapter to display the top tracks for an artist
        mTopTracksAdapter = new TopTracksAdapter(getActivity());
        setListAdapter(mTopTracksAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!mArtistId.equals(mPreviousArtistId)) {
            // Get top tracks of this artist
            // TODO: Check if artist Id is null or empty before querying Spotify server
            new GetTopTracksTask(mTopTracksAdapter, this).execute(mArtistId);
            mPreviousArtistId = mArtistId;
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
}


