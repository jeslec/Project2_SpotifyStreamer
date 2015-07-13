package com.lecomte.jessy.spotifystreamerstage1v3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

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

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new String("allo");
    }
}

