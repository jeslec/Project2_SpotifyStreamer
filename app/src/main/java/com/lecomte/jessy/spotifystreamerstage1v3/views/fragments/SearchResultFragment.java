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

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.controlers.SearchResultAdapter;
import com.lecomte.jessy.spotifystreamerstage1v3.other.tasks.SearchArtistTask;

/**
 * Created by Jessy on 2015-06-23.
 */
public class SearchResultFragment extends ListFragment {

    private final String TAG = getClass().getSimpleName();
    SearchResultAdapter mSearchResultAdapter;
    /*List<ArtistInfo> mArtistInfoList = new ArrayList<ArtistInfo>();
    List<SpotifyCommand> mSpotifyCommands = new LinkedList<SpotifyCommand>();*/

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
    public static SearchResultFragment newInstance(String param1, String param2) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SearchResultFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Set up the adapter to display search results sent to us by search fragment
        /*mSearchResultAdapter = new SearchResultAdapter(getActivity());
        setListAdapter(mSearchResultAdapter);*/

        // TODO: Determine if this should be done here (activity) or in fragment
        // Set up the adapter to display search results sent to us by search fragment
        mSearchResultAdapter = new SearchResultAdapter(getActivity());
        setListAdapter(mSearchResultAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // TODO: Change name of layout to R.layout.fragment_search_result
        View v = inflater.inflate(R.layout.fragment_search_result, container, false);

        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        /*// Get data associated with the selected item
        ArtistInfo artistInfo = (ArtistInfo)getListAdapter().getItem(position);

        // Send artist selection to new activity to display artist's top 10 songs
        Intent tracksIntent = new Intent(getActivity(), TracksActivity.class);
        tracksIntent.putExtra(TracksActivity.EXTRA_ARTIST_ID, artistInfo.getId());
        startActivity(tracksIntent);*/
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateSearchResult(String query){

        new SearchArtistTask(mSearchResultAdapter).execute(query + "*");

       /* // Commands have been issued
        if (!mSpotifyCommands.isEmpty()) {
            // Cancel unstarted commands if possible
            ListIterator<SpotifyCommand> listIterator = mSpotifyCommands.listIterator();

            while (listIterator.hasNext()) {
                SpotifyCommand command = listIterator.next();
                // TODO: Should I use true or false?
                command.getTask().cancel(false);
                Utils.log(TAG, "updateSearchResult() - Deleted command: " + command.toString());
                listIterator.remove();
            }

            Utils.log(TAG, "updateSearchResult() - Command(s) DELETED [size: " + mSpotifyCommands.size() + "]");
        }

        // Clear  results list if search term was cleared by user
        if (searchTerm.isEmpty()) {
            Utils.log(TAG, "updateSearchResult() - Search term empty, result list cleared (no query sent to Spotify)");
            mSearchResultAdapter.clear();
            mSearchResultAdapter.notifyDataSetChanged();
            return;
        }

        //
        SpotifyCommand command = new SpotifyCommand(searchTerm, new FetchArtistsTask(getActivity(), mSearchResultAdapter, searchTerm));
        mSpotifyCommands.add(command);
        Utils.log(TAG, "updateSearchResult() - Command CREATED: " + command.toString() + " [Size: " + mSpotifyCommands.size() + "]");

        command.execute();*/
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}


