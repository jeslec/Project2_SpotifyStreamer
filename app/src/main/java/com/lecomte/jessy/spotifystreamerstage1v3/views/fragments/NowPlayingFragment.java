package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer.PlayerFragmentCommunication;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.MusicControler;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.TopTracksActivity;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jessy on 2015-07-20.
 */
public class NowPlayingFragment extends DialogFragment implements PlayerFragmentCommunication {

    private final String TAG = getClass().getSimpleName();
    static final String EXTRA_TRACK_INFO = "com.lecomte.jessy.spotifystreamerstage1v3.trackInfo";
    static final String EXTRA_ARTIST_NAME = "com.lecomte.jessy.spotifystreamerstage1v3.artistName";
    static final int SEEK_BAR_UPDATE_INTERVAL = 40; // milliseconds
    static final int SEEK_BAR_TEXT_UPDATE_INTERVAL = 1000; // milliseconds

    private String mTrackUrl = "";
    private AudioPlayer mAudioPlayer = new AudioPlayer(this);
    private SeekBar mSeekBar;
    private Handler mSeekBarHandler = new Handler();
    private Handler mSeekBarTextHandler = new Handler();
    Runnable mUpdateSeekBarRunnable;
    Runnable mUpdateSeekBarTextRunnable;
    private TextView mElapsedTimeTextView;
    private TextView mTotalTimeTextView;
    private ImageButton mPlayButton;

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
        TrackInfo trackInfo;
        String artistName;
        Intent intent = getActivity().getIntent();

        View v = inflater.inflate(R.layout.fragment_now_playing, container, false);

        TextView artistTextView = (TextView)v.findViewById(R.id.NowPlaying_artistName);
        TextView trackTextView = (TextView)v.findViewById(R.id.NowPlaying_trackName);
        TextView albumTextView = (TextView)v.findViewById(R.id.NowPlaying_albumName);
        mElapsedTimeTextView = (TextView)v.findViewById(R.id.NowPlaying_elapsedTime);
        mTotalTimeTextView = (TextView)v.findViewById(R.id.NowPlaying_totalTime);

        ImageView albumImageView = (ImageView)v.findViewById(R.id.NowPlaying_albumImage);

        // MediaPlayer controller buttons
        ImageButton prevTrackButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonPrevious);
        mPlayButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonPlay);
        ImageButton nextTrackButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonNext);

        mSeekBar = (SeekBar)v.findViewById(R.id.NowPlaying_seekBar);

        // Get artist/track data that was attached to this fragment when it was created

        //
        if (intent != null) {
            trackInfo = (TrackInfo)intent.getParcelableExtra(TopTracksActivity.EXTRA_TRACK_INFO);
            artistName = (String)intent.getStringExtra(TopTracksActivity.EXTRA_ARTIST_NAME);
            mTrackUrl = trackInfo.getTrackPreviewUrl();
        }

        //
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

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showToast("Pause track");

                // Toggle player between 2 actions: play and pause
                if (mAudioPlayer.isPlaying()) {
                    mAudioPlayer.pause();
                    mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    mAudioPlayer.resume();
                    mPlayButton.setImageResource(android.R.drawable.ic_media_play);
                }

            }
        });

        nextTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showToast("Next track");
            }
        });

        /*seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/

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

    // Start updating the seek bar and the seek bar text values at regular intervals
    // We use 2 different intervals because the seek bar needs to be updated much more often
    // (at a perceivable real-time rate >= 25 fps) than the text values which change every second
    @Override
    public void onReceiveTrackDuration(int duration) {

        // Update seek bar progression
        mUpdateSeekBarRunnable = new Runnable() {
            @Override public void run() {
                if (mAudioPlayer != null) {
                    mSeekBar.setProgress(mAudioPlayer.getCurrentPosition());
                }
                mSeekBarHandler.postDelayed(this, SEEK_BAR_UPDATE_INTERVAL);
            }
        };

        // Update seek bar elapsed time in textView
        mUpdateSeekBarTextRunnable = new Runnable() {
            @Override public void run() {
                //Log.d(TAG, "Runnable.run() - Current position: " + mAudioPlayer.getCurrentPosition());
                if (mAudioPlayer != null) {
                    Pair<Long, Long> minSecPair = Utils.msecToMinSec(mAudioPlayer.getCurrentPosition());

                    mElapsedTimeTextView.setText(getResources()
                            .getString(R.string.NowPlaying_elapsedTime, minSecPair.first,
                                    minSecPair.second));
                }
                mSeekBarTextHandler.postDelayed(this, SEEK_BAR_TEXT_UPDATE_INTERVAL);
            }
        };

        mSeekBar.setMax(duration);

        Pair<Long, Long> minSecPair = Utils.msecToMinSec(duration);

        mTotalTimeTextView.setText(getResources()
                .getString(R.string.NowPlaying_totalTime, minSecPair.first,
                        minSecPair.second));

        // The runnable must be called once explicitly for it to be called automatically after
        // http://stackoverflow.com/questions/21929529/runnable-not-running-at-all-inside-fragment#21929571
        mSeekBarHandler.post(mUpdateSeekBarRunnable);
        mSeekBarTextHandler.post(mUpdateSeekBarTextRunnable);
    }

    // This is called when the track is done playing
    public void onTrackCompleted() {
        Log.d(TAG, "PlayerFragmentCommunication.onTrackCompleted()");

        // Stop updating seek bar and text values
        mSeekBarHandler.removeCallbacks(mUpdateSeekBarRunnable);
        mSeekBarTextHandler.removeCallbacks(mUpdateSeekBarTextRunnable);

        // The media player makes call to onCompletion() even if it has not reached the full end
        // of the track. So this in turn calls our onTrackCompleted() and the elapsed time never
        // displays the real duration of the track when it has supposedly reached the end
        // My solution: set elapsed time to duration time
        // Is this a hack or a simple solution to an unsolvable issue?
        mElapsedTimeTextView.setText(mTotalTimeTextView.getText());
    }
}
