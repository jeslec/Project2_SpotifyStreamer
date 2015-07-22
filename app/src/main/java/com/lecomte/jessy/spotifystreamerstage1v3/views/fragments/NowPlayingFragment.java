package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.MusicControler;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.TopTracksActivity;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by Jessy on 2015-07-20.
 */
public class NowPlayingFragment extends DialogFragment {

    private final String TAG = getClass().getSimpleName();
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

    // See section: Showing a Dialog Fullscreen or as an Embedded Fragment from
    // http://developer.android.com/guide/topics/ui/dialogs.html

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_now_playing, container, false);

        TextView artistTextView = (TextView)v.findViewById(R.id.NowPlaying_artistName);
        TextView trackTextView = (TextView)v.findViewById(R.id.NowPlaying_trackName);
        TextView albumTextView = (TextView)v.findViewById(R.id.NowPlaying_albumName);

        ImageView albumImageView = (ImageView)v.findViewById(R.id.NowPlaying_albumImage);

        ImageButton prevTrackButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonPrevious);
        ImageButton playButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonPlay);
        ImageButton nextTrackButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonNext);

        // Get artist/track data that was attached to this fragment when it was created

        TrackInfo trackInfo;
        String artistName;
        Intent intent = getActivity().getIntent();

        if (intent != null) {
            trackInfo = (TrackInfo)intent.getParcelableExtra(TopTracksActivity.EXTRA_TRACK_INFO);
            artistName = (String)intent.getStringExtra(TopTracksActivity.EXTRA_ARTIST_NAME);
            mTrackUrl = trackInfo.getTrackPreviewUrl();
        }

        else if (getArguments() != null) {
            trackInfo = (TrackInfo) getArguments().getParcelable(EXTRA_TRACK_INFO);
            artistName = (String)getArguments().getSerializable(EXTRA_ARTIST_NAME);
            mTrackUrl = trackInfo.getTrackPreviewUrl();
        }

        else {
            Log.d(TAG, "OnCreateView() - No extras and no arguments: something went terribly wrong!");
            return v;
        }

        artistTextView.setText(artistName);
        trackTextView.setText(trackInfo.getTrackName());
        albumTextView.setText(trackInfo.getAlbumName());

        if (trackInfo.getAlbumBigImageUrl().isEmpty()) {
            albumImageView.setImageResource(R.drawable.noimage);
        }

        else {
            Picasso.with(getActivity()).load(trackInfo.getAlbumBigImageUrl()).into(albumImageView);
        }

        prevTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showToast("Previous track");
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showToast("Play track");
            }
        });

        nextTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showToast("Next track");
            }
        });

        mAudioPlayer.play(mTrackUrl);

        return v;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
