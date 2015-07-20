package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.controlers.SearchResultAdapter;
import com.lecomte.jessy.spotifystreamerstage1v3.models.ArtistInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.tasks.SearchArtistTask;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;

/**
 * Created by Jessy on 2015-06-23.
 */
public class ArtistSearchFragment extends ListFragment {

    private final String TAG = getClass().getSimpleName();
    SearchResultAdapter mSearchResultAdapter;

   /* // Data required by this fragment upon creation
    private static final String ARG_ARTIST_ID =
            "com.lecomte.jessy.spotifystreamerstage1v3.arg.ArtistId";
    private static final String ARG_ARTIST_NAME =
            "com.lecomte.jessy.spotifystreamerstage1v3.arg.ArtistName";
*/
    // TODO: Rename and change types of parameters
    /*private String mArtistId;
    private String mArtistName;
*/
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ArtistSearchFragment newInstance(String param1, String param2) {
        ArtistSearchFragment fragment = new ArtistSearchFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_ARTIST_ID, param1);
        args.putString(ARG_ARTIST_NAME, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    public ArtistSearchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/

        // Set up the adapter to display search results sent to us by search fragment
        mSearchResultAdapter = new SearchResultAdapter(getActivity());
        setListAdapter(mSearchResultAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_result, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        if (!Utils.isInternetAvailable()) {
            Utils.showToast(R.string.TopTracks_noInternet);
            return;
        }

        // Get data associated with the selected item
        ArtistInfo artistInfo = (ArtistInfo)getListAdapter().getItem(position);

        mListener.onArtistSelected(artistInfo);

        // Send artist selection to new activity to display artist's top 10 songs
       /* Intent tracksIntent = new Intent(getActivity(), TopTracksActivity.class);
        tracksIntent.putExtra(TopTracksActivity.EXTRA_ARTIST_ID, artistInfo.getId());
        tracksIntent.putExtra(TopTracksActivity.EXTRA_ARTIST_NAME, artistInfo.getName());
        startActivity(tracksIntent);*/
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + TAG + ".OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateSearchResult(String query){
        new SearchArtistTask(mSearchResultAdapter, this).execute(query);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);

        public void onArtistSelected(ArtistInfo artist);
    }

    public void showProgressBar() {
        ProgressBar progress = (ProgressBar)getActivity()
                .findViewById(R.id.SearchResult_progressBar);
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);
    }

    public void hideProgressBar() {
        ProgressBar progress = (ProgressBar)getActivity()
                .findViewById(R.id.SearchResult_progressBar);
        progress.setVisibility(View.GONE);
    }

}


