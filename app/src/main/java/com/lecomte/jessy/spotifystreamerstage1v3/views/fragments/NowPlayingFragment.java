package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.MusicControler;

import java.io.IOException;

/**
 * Created by Jessy on 2015-07-20.
 */
public class NowPlayingFragment extends DialogFragment /*implements MediaController.MediaPlayerControl*/ {

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

        // Get artist/track data that was attached to this fragment when it was created
        TrackInfo trackInfo = (TrackInfo)getArguments().getParcelable(EXTRA_TRACK_INFO);
        String artistName = (String)getArguments().getSerializable(EXTRA_ARTIST_NAME);
        mTrackUrl = trackInfo.getTrackPreviewUrl();

        artistTextView.setText(artistName);
        trackTextView.setText(trackInfo.getTrackName());
        albumTextView.setText(trackInfo.getAlbumName());

       /* MediaController mediaController = (MediaController)v.findViewById(R.id.mediaController);
        mediaController.setEnabled(true);
        //mediaController.setVisibility(View.VISIBLE);
        mediaController.setAnchorView(v);
        mediaController.setMediaPlayer(this);*/
        //mediaController.show();

        //
        mAudioPlayer.play(mTrackUrl);
       /* MediaController controller = new MediaController(getActivity());
        controller.setAnchorView(v);
        controller.setEnabled(true);
        controller.show();
*/
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.NowPlaying_dialogTitle)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    /*@Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = super.onCreateView(inflater, container, savedInstanceState);

        MediaController controller = new MediaController(getActivity());
        controller.setAnchorView(v);
        controller.setEnabled(true);
        controller.show();

        return v;
    }*/

    /*@Override
    public void start() {
        mAudioPlayer.play(mTrackUrl);
    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }*/
}
