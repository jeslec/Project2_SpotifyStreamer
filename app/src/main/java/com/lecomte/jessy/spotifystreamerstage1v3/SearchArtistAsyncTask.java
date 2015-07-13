package com.lecomte.jessy.spotifystreamerstage1v3;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Spotify;

import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Created by Jessy on 2015-07-13.
 */

public class SearchArtistAsyncTask extends AsyncTask<String, String, String> {

    private ProgressDialog mProgressDialog;

    public SearchArtistAsyncTask() {
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected String doInBackground(String... params) {

        String searchTerm = params[0];

        ArtistsPager artistsPager = Spotify.searchArtists(searchTerm);

        return new String("allo");
    }
}

