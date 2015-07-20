package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;

import java.io.IOException;

/**
 * Created by Jessy on 2015-07-20.
 */
public class NowPlayingFragment extends DialogFragment{

    static final String EXTRA_TRACK_INFO = "com.lecomte.jessy.spotifystreamerstage1v3.trackInfo";
    static final String EXTRA_ARTIST_NAME = "com.lecomte.jessy.spotifystreamerstage1v3.artistName";

    private String mTrackUrl = "";
    private AudioPlayer mAudioPlayer = new AudioPlayer();

    // This is  how we send data to the fragment
    public static NowPlayingFragment newInstance(TrackInfo trackInfo, String artistName) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_TRACK_INFO, trackInfo);
        args.putString(EXTRA_ARTIST_NAME, artistName);

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

        TrackInfo trackInfo = (TrackInfo)getArguments().getParcelable(EXTRA_TRACK_INFO);
        String artistName = (String)getArguments().getSerializable(EXTRA_ARTIST_NAME);
        mTrackUrl = trackInfo.getTrackPreviewUrl();

        artistTextView.setText(artistName);
        trackTextView.setText(trackInfo.getTrackName());
        albumTextView.setText(trackInfo.getAlbumName());

        // 
        mAudioPlayer.play(mTrackUrl);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.NowPlaying_dialogTitle)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
