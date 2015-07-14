package com.lecomte.jessy.spotifystreamerstage1v3.other.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.controlers.SearchResultAdapter;
import com.lecomte.jessy.spotifystreamerstage1v3.models.ArtistInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Spotify;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.SearchResultFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Jessy on 2015-07-13.
 */

public class SearchArtistTask extends AsyncTask<String, Void, ArtistsPager> {

    private final String TAG = getClass().getSimpleName();
    //private ProgressDialog mProgressDialog;
    private String mSearchTerm;
    SearchResultAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    private SearchResultFragment mSearchResultFragment;

    public SearchArtistTask(SearchResultAdapter adapter, SearchResultFragment fragment) {

        mAdapter = adapter;
        mSearchResultFragment = fragment;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mSearchResultFragment.showProgressBar();

    }

    @Override
    protected void onPostExecute(ArtistsPager artistsPager) {

        super.onPostExecute(artistsPager);

        // The activity can be null if it is thrown out by Android while task is running!
        if (mSearchResultFragment != null && mSearchResultFragment.getActivity() != null) {
            mSearchResultFragment.hideProgressBar();
            mSearchResultFragment = null;
        }


        Utils.log(TAG, "OnPostExecute() - [Searched term: " + mSearchTerm + "]" +
                " [Artists found count: " + artistsPager.artists.items.size() + "]");

        // If there is no search result, we can't display anything
        if (artistsPager.artists.items.isEmpty()) {
            Utils.showToast(R.string.no_artist_found);
            Utils.log(TAG, "OnPostExecute() - no artist found");
        }

        // Extract only the data we need so we don't use extra memory to store useless data
        List<ArtistInfo> myArtistInfoList = new ArrayList<ArtistInfo>();

        for (int i=0; i<artistsPager.artists.items.size(); i++) {
            Artist artist = artistsPager.artists.items.get(i);
            String imageUrl = new String("");

            // If images are present, extract the Url and dimensions of the smallest one
            // TODO: Optimize this part - Cut down on number of variables, etc.
            if (artist.images != null && !artist.images.isEmpty()) {
                int imageCount = artist.images.size();
                int lastImageIndex = imageCount - 1;
                Image image = artist.images.get(lastImageIndex);
                imageUrl = image.url;
                int width = image.width;
                int height = image.height;

                //Utils.log(TAG, String.format("Artist[%2d]: %3d x %3d", i, width, height));
            }

            myArtistInfoList.add(new ArtistInfo(artist.id, artist.name, artist.popularity, imageUrl));
        }

        // Sort artists suggestions from most popular to least popular
        // In suggestions dropdown, most popular artists will appear at top
        //http://stackoverflow.com/questions/9109890/android-java-how-to-sort-a-list-of-objects-by-a-certain-value-within-the-object#13821611
        Collections.sort(myArtistInfoList, new Comparator<ArtistInfo>() {
            @Override
            public int compare(ArtistInfo lhs, ArtistInfo rhs) {

                if (lhs.getPopularity() < rhs.getPopularity())
                    return 1;

                if (lhs.getPopularity() > rhs.getPopularity())
                    return -1;

                return 0;
            }
        });

        //Utils.log(TAG, "FetchArtistsTask.onPostExecute() - Done sorting artists list");

        // Send the list of artists to the activity so it can update its fragments
        mAdapter.clear();
        mAdapter.addAll(myArtistInfoList);
        mAdapter.notifyDataSetChanged();

        Utils.logLoop(TAG, "getFilter() - Sorted Artists[", "]: ", myArtistInfoList);
    }

    @Override
    protected ArtistsPager doInBackground(String... params) {

        mSearchTerm = params[0];

        ArtistsPager artistsPager = Spotify.searchArtists(mSearchTerm);

        return artistsPager;
    }
}

