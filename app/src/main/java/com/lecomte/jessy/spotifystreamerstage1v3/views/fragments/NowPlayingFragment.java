package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer.PlayerFragmentCommunication;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.SafeIndex;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.TopTracksActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
    private ArrayList<TrackInfo> mTrackInfoList = new ArrayList<TrackInfo>();
    private int mTrackIndex = 0;
    private SafeIndex mTrackListIndex;
    private TextView mTrackTextView;
    private TextView mAlbumTextView;
    private ImageView mAlbumImageView;

    //int This is  how we send data to the fragment
    public static NowPlayingFragment newInstance(TrackInfo trackInfo, String artistName) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_TRACK_INFO, trackInfo);
        args.putString(EXTRA_ARTIST_NAME, artistName);

        NowPlayingFragment fragment = new NowPlayingFragment();
        fragment.setArguments(args);

        return fragment;
    }

    void displayTrackInfo(TrackInfo track) {
        mTrackTextView.setText(track.getTrackName());
        mAlbumTextView.setText(track.getAlbumName());

        if (track.getAlbumBigImageUrl().isEmpty()) {
            mAlbumImageView.setImageResource(R.drawable.noimage);
        } else {
            Picasso.with(getActivity()).load(track.getAlbumBigImageUrl()).into(mAlbumImageView);
        }
    }

    // See section: Showing a Dialog Fullscreen or as an Embedded Fragment from
    // http://developer.android.com/guide/topics/ui/dialogs.html

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TrackInfo trackInfo;
        String artistName;
        Intent intent = getActivity().getIntent();

        View v = inflater.inflate(R.layout.fragment_now_playing, container, false);

        TextView artistTextView = (TextView)v.findViewById(R.id.NowPlaying_artistName);
        mTrackTextView = (TextView)v.findViewById(R.id.NowPlaying_trackName);
        mAlbumTextView = (TextView)v.findViewById(R.id.NowPlaying_albumName);
        mElapsedTimeTextView = (TextView)v.findViewById(R.id.NowPlaying_elapsedTime);
        mTotalTimeTextView = (TextView)v.findViewById(R.id.NowPlaying_totalTime);

        mAlbumImageView = (ImageView)v.findViewById(R.id.NowPlaying_albumImage);

        // MediaPlayer controller buttons
        ImageButton prevTrackButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonPrevious);
        mPlayButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonPlay);
        ImageButton nextTrackButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonNext);

        mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
        prevTrackButton.setImageResource(android.R.drawable.ic_media_previous);
        nextTrackButton.setImageResource(android.R.drawable.ic_media_next);

        mSeekBar = (SeekBar)v.findViewById(R.id.NowPlaying_seekBar);

        // Get artist/track data that was attached to this fragment when it was created

        //
        if (intent != null) {
            //trackInfo = (TrackInfo)intent.getParcelableExtra(TopTracksActivity.EXTRA_TRACK_INFO);
            mTrackInfoList = intent.getParcelableArrayListExtra(TopTracksActivity.EXTRA_TRACK_INFO_LIST);
            //mTrackIndex = intent.getIntExtra(TopTracksActivity.EXTRA_TRACK_INDEX, 0);
            mTrackListIndex = new SafeIndex(intent.getIntExtra(TopTracksActivity.EXTRA_TRACK_INDEX,
                                        0), mTrackInfoList.size() - 1);
            trackInfo = mTrackInfoList.get(mTrackIndex);
            artistName = intent.getStringExtra(TopTracksActivity.EXTRA_ARTIST_NAME);
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
        displayTrackInfo(trackInfo);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle player between 2 actions: play and pause
                if (mAudioPlayer.isPlaying()) {
                    mAudioPlayer.pause();
                    mPlayButton.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mAudioPlayer.resume();
                    mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });

        prevTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackInfo trackInfo = mTrackInfoList.get(mTrackListIndex.getPrevious());
                //Utils.showToast("Previous track index: " + trackIndex);
                String trackUrl = trackInfo.getTrackPreviewUrl();
                mAudioPlayer.play(trackUrl);
                displayTrackInfo(trackInfo);
                Log.d(TAG, "prevTrackButton.onClickListener() - Track index: " +
                        mTrackListIndex.get());
            }
        });

        nextTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackInfo trackInfo = mTrackInfoList.get(mTrackListIndex.getNext());
                //int trackIndex = mTrackListIndex.getNext();
                //Utils.showToast("Next track index: " + trackIndex);
                String trackUrl = trackInfo.getTrackPreviewUrl();
                mAudioPlayer.play(trackUrl);
                displayTrackInfo(trackInfo);
                Log.d(TAG, "nextTrackButton.onClickListener() - Track index: " +
                        mTrackListIndex.get());
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //mAudioPlayer.pause();
                    mAudioPlayer.seekTo(progress);
                    //mAudioPlayer.resume();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

    // Start updating the seek bar and the seek bar text values at regular intervals
    // We use 2 different intervals because the seek bar needs to be updated much more often
    // (at a perceivable real-time rate >= 25 fps) than the text values which change every second
    @Override
    public void onReceiveTrackDuration(int duration) {
        // Cancel all previous runnables
        stopSeekBarUpdates();

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

    // Stop updating seek bar and text values
    public void stopSeekBarUpdates() {
        Log.d(TAG, "stopSeekBarUpdates()");
        mSeekBarHandler.removeCallbacks(mUpdateSeekBarRunnable);
        mSeekBarTextHandler.removeCallbacks(mUpdateSeekBarTextRunnable);
    }

    // This is called when the track is done playing
    public void onTrackCompleted() {
        Log.d(TAG, "PlayerFragmentCommunication.onTrackCompleted()");

        // Reset seek bar & seek bar text values and our media controller buttons
        stopSeekBarUpdates();
        mSeekBar.setProgress(0);
        mAudioPlayer.seekTo(0);
        mPlayButton.setImageResource(android.R.drawable.ic_media_play);
        mElapsedTimeTextView.setText("00:00");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy()");
        stopSeekBarUpdates();
        mAudioPlayer.stop();
    }
}
