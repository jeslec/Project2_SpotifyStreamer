package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.R;

/**
 * Created by Jessy on 2015-07-20.
 */
public class NowPlayingFragment extends DialogFragment{

    // This is  how we send data to the fragment
    public static NowPlayingFragment newInstance() {
        Bundle args = new Bundle();
        //args.putSerializable(EXTRA_DATE, date);

        NowPlayingFragment fragment = new NowPlayingFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_now_playing, null);

        TextView artistTextView = (TextView)v.findViewById(R.id.NowPlaying_artistName);
        TextView trackTextView = (TextView)v.findViewById(R.id.NowPlaying_trackName);
        TextView albumTextView = (TextView)v.findViewById(R.id.NowPlaying_albumName);

        artistTextView.setText(R.string.NowPlaying_artistName);
        trackTextView.setText(R.string.NowPlaying_trackName);
        albumTextView.setText(R.string.NowPlaying_albumName);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.NowPlaying_dialogTitle)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
