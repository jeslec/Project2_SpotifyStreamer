package com.lecomte.jessy.spotifystreamerstage1v3.other.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.controlers.TopTracksAdapter;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Search Spotify's server for a list of artists based on the provided artist's name
 *
 * @param param1 Parameter 1.
 * @param param2 Parameter 2.
 * @return .
 */
public class FetchTopTracksTask extends AsyncTask<String, Void, Tracks> {

    private final String TAG = getClass().getSimpleName();
    private TopTracksAdapter mAdapter;

    public FetchTopTracksTask(TopTracksAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    protected Tracks doInBackground(String... artistIdList) {
        Tracks tracks = new Tracks();
        String artistId = artistIdList[0];
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();

        Map queryOptions = new HashMap();
        // TODO: Put region code (US, CA, etc.) in a Settings view
        queryOptions.put("country", new String("US"));
        Utils.log(TAG, "FetchTopTracksTask.doInBackground() - Getting top tracks from Spotify for artistId: " + artistId);

        try {
            tracks = spotify.getArtistTopTrack(artistId, queryOptions);
        } catch (RetrofitError e) {
            Log.e(TAG, "doInBackground() - Error: " + e.getCause());
        }
        return tracks;
    }

    @Override
    protected void onPostExecute(Tracks top10Tracks) {

        Utils.log(TAG, "FetchTopTracksTask.OnPostExecute() - Tracks returned from Spotify: " +
                top10Tracks.tracks.size());

        // No tracks found for this artist
        if (top10Tracks.tracks.isEmpty()) {
            Utils.showToast(R.string.no_track_found);
            Utils.log(TAG, "FetchTopTracksTask.OnPostExecute() - no tracks found");
        }

        // Extract only the data we need so we don't use extra memory to store useless data
        List<TrackInfo> myTrackList = new ArrayList<TrackInfo>();

        for (int i = 0; i < top10Tracks.tracks.size(); i++) {
            Track track = top10Tracks.tracks.get(i);

            // If images are present, extract the Url and dimensions of the smallest one
            if (track.album.images != null && !track.album.images.isEmpty()) {
                int imageCount = track.album.images.size();
                int lastImageIndex = imageCount - 1;
                Image albumImage = track.album.images.get(lastImageIndex);
                String albumImageUrl = albumImage.url;
                int width = albumImage.width;
                int height = albumImage.height;

                //Utils.log(TAG, String.format("Track[%2d]: %3d x %3d", i, width, height));

                myTrackList.add(new TrackInfo(track.id, track.name, track.album.name,
                        albumImageUrl, track.popularity));
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
