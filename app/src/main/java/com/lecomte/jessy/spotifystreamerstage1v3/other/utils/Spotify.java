package com.lecomte.jessy.spotifystreamerstage1v3.other.utils;

import android.util.Log;

import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Created by Jessy on 2015-07-13.
 */
public class Spotify {

    public static ArtistsPager searchArtists(String searchTerm) {
        ArtistsPager artistsPager = new ArtistsPager();

        SpotifyApi spotifyApi = new SpotifyApi();
        SpotifyService spotifyService = spotifyApi.getService();

        try {
            artistsPager = spotifyService.searchArtists(searchTerm);
        } catch (RetrofitError e) {
            Log.e("Spotify", "searchArtists.doInBackground() - Error: " + e.getCause());
        }

        return artistsPager;
    }

    public static Tracks getArtistTopTrack(String artistId, Map<String, Object> queryOptions) {
        Tracks tracks = new Tracks();

        SpotifyApi spotifyApi = new SpotifyApi();
        SpotifyService spotifyService = spotifyApi.getService();

        try {
            tracks = spotifyService.getArtistTopTrack(artistId, queryOptions);
        } catch (RetrofitError e) {
            Log.e("Spotify", "getArtistTopTrack.doInBackground() - Error: " + e.getCause());
        }
        return tracks;
    }
}
