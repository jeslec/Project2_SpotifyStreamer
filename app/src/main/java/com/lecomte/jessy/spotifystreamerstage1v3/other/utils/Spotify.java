package com.lecomte.jessy.spotifystreamerstage1v3.other.utils;

import android.util.Log;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

/**
 * Created by Jessy on 2015-07-13.
 */
public class Spotify {

    public static ArtistsPager searchArtists(String searchTerm) {
        ArtistsPager artistsPager = new ArtistsPager();

        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();

        //Utils.log(TAG, "FetchArtistsTask.doInBackground() - Getting artist list from Spotify for: " + mSearchTerm);

        try {
            artistsPager = spotify.searchArtists(searchTerm);
        } catch (RetrofitError e) {
            //Log.e(TAG, "doInBackground() - Error: " + e.getCause());
        }

        return artistsPager;
    }
}
