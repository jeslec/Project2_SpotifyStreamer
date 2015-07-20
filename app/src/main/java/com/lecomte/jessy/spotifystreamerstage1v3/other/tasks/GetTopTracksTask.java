package com.lecomte.jessy.spotifystreamerstage1v3.other.tasks;

import android.os.AsyncTask;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.controlers.TopTracksAdapter;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Spotify;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.TopTracksFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Search Spotify's server for a list of artists based on the provided artist's name
 *
 * @param param1 Parameter 1.
 * @param param2 Parameter 2.
 * @return .
 */
public class GetTopTracksTask extends AsyncTask<String, Void, Tracks> {
    private final String TAG = getClass().getSimpleName();
    private TopTracksAdapter mAdapter;
    private TopTracksFragment mTopTracksFragment;

    public GetTopTracksTask(TopTracksAdapter adapter, TopTracksFragment fragment) {
        mAdapter = adapter;
        mTopTracksFragment = fragment;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mTopTracksFragment.showProgressBar();
    }

    @Override
    protected Tracks doInBackground(String... artistIdList) {
        Tracks tracks = new Tracks();
        String artistId = artistIdList[0];
        Map queryOptions = new HashMap();

        // TODO: Put region code (US, CA, etc.) in a Settings view
        queryOptions.put("country", new String("US"));
        Utils.log(TAG, "FetchTopTracksTask.doInBackground() - Getting top tracks from Spotify for artistId: " + artistId);

        tracks = Spotify.getArtistTopTrack(artistId, queryOptions);

        return tracks;
    }

    @Override
    protected void onPostExecute(Tracks top10Tracks) {

        super.onPostExecute(top10Tracks);

        // The activity can be null if it is thrown out by Android while task is running!
        if (mTopTracksFragment != null && mTopTracksFragment.getActivity() != null) {
            mTopTracksFragment.hideProgressBar();
            mTopTracksFragment = null;
        }
        
        Utils.log(TAG, "FetchTopTracksTask.OnPostExecute() - Tracks returned from Spotify: " +
                top10Tracks.tracks.size());

        // No tracks found for this artist
        if (top10Tracks.tracks.isEmpty()) {
            Utils.showToast(R.string.TopTracks_noTrackFound);
            Utils.log(TAG, "FetchTopTracksTask.OnPostExecute() - no tracks found");
        }

        // Extract only the data we need so we don't use extra memory to store useless data
        List<TrackInfo> myTrackList = new ArrayList<TrackInfo>();

        for (int i = 0; i < top10Tracks.tracks.size(); i++) {
            Track track = top10Tracks.tracks.get(i);

            // If images are present, extract the Url and dimensions of the smallest one
            if (track.album.images != null && !track.album.images.isEmpty()) {
                String bigImageUrl = "";
                String smallImageUrl = track.album.images.get(track.album.images.size() - 1).url;

                // Get the biggest image available
                // TODO: Optimize to get the image closest in size to imageView where we intend to display this image
                if (track.album.images.size() >= 2) {
                    bigImageUrl = track.album.images.get(0).url;
                }

                String artistName = track.artists.get(0).name;

                myTrackList.add(new TrackInfo(track.name, track.album.name, smallImageUrl,
                        bigImageUrl, track.popularity, track.id, track.preview_url,
                        track.duration_ms));
            }
        }

        // Sort tracks from highest to lowest popularity
        Collections.sort(myTrackList, new Comparator<TrackInfo>() {
            @Override
            public int compare(TrackInfo lhs, TrackInfo rhs) {

                if (lhs.getTrackPopularity() < rhs.getTrackPopularity())
                    return 1;

                if (lhs.getTrackPopularity() > rhs.getTrackPopularity())
                    return -1;

                return 0;
            }
        });

        Utils.logLoop(TAG, "getFilter() - Sorted Tracks[", "]: ", myTrackList);

        // Update the view layer
        mAdapter.clear();
        mAdapter.addAll(myTrackList);
        mAdapter.notifyDataSetChanged();
    }
}
