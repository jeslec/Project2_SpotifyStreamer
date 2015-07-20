package com.lecomte.jessy.spotifystreamerstage1v3.other.tasks;

import android.os.AsyncTask;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.controlers.SearchResultAdapter;
import com.lecomte.jessy.spotifystreamerstage1v3.models.ArtistInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Spotify;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.ArtistSearchFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Created by Jessy on 2015-07-13.
 */

public class SearchArtistTask extends AsyncTask<String, Void, ArtistsPager> {

    private final String TAG = getClass().getSimpleName();
    private String mSearchTerm;
    SearchResultAdapter mAdapter;
    private ArtistSearchFragment mArtistSearchFragment;

    public SearchArtistTask(SearchResultAdapter adapter, ArtistSearchFragment fragment) {
        mAdapter = adapter;
        mArtistSearchFragment = fragment;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mArtistSearchFragment.showProgressBar();
    }

    @Override
    protected void onPostExecute(ArtistsPager artistsPager) {
        super.onPostExecute(artistsPager);

        // The activity can be null if it is thrown out by Android while task is running!
        if (mArtistSearchFragment != null && mArtistSearchFragment.getActivity() != null) {
            mArtistSearchFragment.hideProgressBar();
            mArtistSearchFragment = null;
        }

        // Code review fix: this happens when Internet connection if OFF: no artists are returned
        if (artistsPager.artists == null) {
            return;
        }

        Utils.log(TAG, "OnPostExecute() - [Searched term: " + mSearchTerm + "]" +
                " [Artists found count: " + artistsPager.artists.items.size() + "]");

        // If there is no search result, we can't display anything
        if (artistsPager.artists.items.isEmpty()) {
            Utils.showToast(R.string.ArtistSearch_noArtistFound);
            Utils.log(TAG, "OnPostExecute() - no artist found");
        }

        // Extract only the data we need so we don't use extra memory to store useless data
        List<ArtistInfo> myArtistInfoList = new ArrayList<ArtistInfo>();

        for (int i=0; i<artistsPager.artists.items.size(); i++) {
            Artist artist = artistsPager.artists.items.get(i);
            String imageUrl = "";

            // If images are present, extract the Url of the smallest one (last image in list)
            if (artist.images != null && !artist.images.isEmpty()) {
                imageUrl = artist.images.get(artist.images.size() - 1).url;
            }

            myArtistInfoList.add(new ArtistInfo(artist.id, artist.name, artist.popularity, imageUrl));
        }

        // Sort artists from most popular (displayed at top of list) to least popular (bottom)
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

        // Send the list of artists to the activity so it can update its fragments
        mAdapter.clear();
        mAdapter.addAll(myArtistInfoList);
        mAdapter.notifyDataSetChanged();

        Utils.logLoop(TAG, "getFilter() - Sorted Artists[", "]: ", myArtistInfoList);
    }

    @Override
    protected ArtistsPager doInBackground(String... params) {
        ArtistsPager artistsPager = new ArtistsPager();
        mSearchTerm = params[0];
        return Spotify.searchArtists(mSearchTerm);
    }
}

