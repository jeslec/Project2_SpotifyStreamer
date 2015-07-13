package com.lecomte.jessy.spotifystreamerstage1v3;

import android.util.Log;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

/**
 * Created by Jessy on 2015-07-13.
 */
public class Spotify {

    public static void searchArtists(String searchTerm) {
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();

        //Utils.log(TAG, "FetchArtistsTask.doInBackground() - Getting artist list from Spotify for: " + mSearchTerm);

        try {
            ArtistsPager artistPager = spotify.searchArtists(searchTerm);
        } catch (RetrofitError e) {
            //Log.e(TAG, "doInBackground() - Error: " + e.getCause());
        }
    }
}
