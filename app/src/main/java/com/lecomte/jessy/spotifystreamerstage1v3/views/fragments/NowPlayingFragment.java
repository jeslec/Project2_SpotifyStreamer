package com.lecomte.jessy.spotifystreamerstage1v3.views.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.AudioPlayerService;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.TopTracksActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Jessy on 2015-07-20.
 */
public class NowPlayingFragment extends DialogFragment implements ServiceConnection,
        AudioPlayer.Callback {

    private final String TAG = getClass().getSimpleName();
    static final String EXTRA_TRACK_INFO = "com.lecomte.jessy.spotifystreamerstage1v3.trackInfo";
    static final String EXTRA_ARTIST_NAME = "com.lecomte.jessy.spotifystreamerstage1v3.artistName";
    static final int SEEK_BAR_UPDATE_INTERVAL = 40; // milliseconds
    static final int SEEK_BAR_TEXT_UPDATE_INTERVAL = 1000; // milliseconds

    private String mTrackUrl = "";
    private SeekBar mSeekBar;
    private Handler mSeekBarHandler = new Handler();
    private Handler mSeekBarTextHandler = new Handler();
    private Runnable mUpdateSeekBarRunnable;
    private Runnable mUpdateSeekBarTextRunnable;
    private TextView mElapsedTimeTextView;
    private TextView mTotalTimeTextView;
    private ImageButton mPlayButton;
    private ArrayList<TrackInfo> mTrackList = new ArrayList<TrackInfo>();
    private int mPlayListIndex = 0;
    private TextView mArtistTextView;
    private TextView mTrackTextView;
    private TextView mAlbumTextView;
    private ImageView mAlbumImageView;
    private int mSeekBarProgress = 0;
    private AudioPlayerService mAudioService;
    private boolean mIsNewPlayList = false;

    //int This is  how we send data to the fragment
    public static NowPlayingFragment newInstance(TrackInfo trackInfo) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_TRACK_INFO, trackInfo);
        NowPlayingFragment fragment = new NowPlayingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // Update UI with track info
    void displayTrackInfo(TrackInfo track) {
        mArtistTextView.setText(track.getArtistName());
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
        Utils.log(TAG, "onCreateView()");
        TrackInfo trackInfo;

        // TEST: this will tell us if we need to stop the player and reload the playlist
        // Added: 2015-07-26, 17h30
        mIsNewPlayList = true;
        //------------------

        Intent intent = getActivity().getIntent();

        getActivity().startService(new Intent(getActivity(), AudioPlayerService.class));

        View v = inflater.inflate(R.layout.fragment_now_playing, container, false);

        mArtistTextView = (TextView)v.findViewById(R.id.NowPlaying_artistName);
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

        // Fragment was "started" with an intent: this happens in a single-pane layout
        if (intent != null) {
            mTrackList = intent.getParcelableArrayListExtra(TopTracksActivity.EXTRA_TRACK_LIST);
            mPlayListIndex = intent.getIntExtra(TopTracksActivity.EXTRA_TRACK_INDEX, 0);

            // This happens when NowPlaying is called without setting any extras
            // Occurs when user selects this app's notification and this screen is launched
            if (mTrackList == null && mPlayListIndex == 0) {
                Utils.log(TAG, "onCreateView() - mTrackList & mPlayListIndex are null!");
            }
        }

        // Fragment was "started" with newInstance(): this happens in a double-pane layout
        else if (getArguments() != null) {
            final Bundle args = getArguments();
            mTrackList = args.getParcelableArrayList(TopTracksActivity.EXTRA_TRACK_LIST);
            mPlayListIndex = args.getInt(TopTracksActivity.EXTRA_TRACK_INDEX, 0);
        }

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle player between 2 actions: play and pause
                if (mAudioService.getPlayer().isPlaying()) {
                    pausePlayer();
                } else {
                    resumePlayer();
                }
            }
        });

        prevTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioService.getPlayer().playPrevious();
                displayTrackInfo(mAudioService.getPlayer().getTrackInfo());
            }
        });

        nextTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioService.getPlayer().playNext();
                displayTrackInfo(mAudioService.getPlayer().getTrackInfo());
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Utils.log(TAG, "seekBar.onProgressChanged()");
                    mSeekBarProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Utils.log(TAG, "seekBar.onStartTrackingTouch()");
                pausePlayer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Utils.log(TAG, "seekBar.onStopTrackingTouch()");
                mAudioService.getPlayer().seekTo(mSeekBarProgress);
                resumePlayer();
            }
        });

        return v;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Utils.log(TAG, "onCreateDialog()");
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
    public void onReceiveTrackDuration(int duration) {
        // Cancel all previous runnables
        stopSeekBarUpdates();

        // Update seek bar progression
        mUpdateSeekBarRunnable = new Runnable() {
            @Override public void run() {
                if (mAudioService != null && mAudioService.getPlayer() != null) {
                    mSeekBar.setProgress(mAudioService.getPlayer().getCurrentPosition());
                }
                mSeekBarHandler.postDelayed(this, SEEK_BAR_UPDATE_INTERVAL);
            }
        };

        // Update seek bar elapsed time in textView
        mUpdateSeekBarTextRunnable = new Runnable() {
            @Override public void run() {
                //Utils.log(TAG, "Runnable.run() - Current position: " + mAudioService.getPlayer().getCurrentPosition());
                if (mAudioService != null && mAudioService.getPlayer() != null) {
                    Pair<Long, Long> minSecPair = Utils.msecToMinSec(mAudioService.getPlayer().getCurrentPosition());

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
        Utils.log(TAG, "stopSeekBarUpdates()");
        mSeekBarHandler.removeCallbacks(mUpdateSeekBarRunnable);
        mSeekBarTextHandler.removeCallbacks(mUpdateSeekBarTextRunnable);
    }

    // This is called when the track is done playing
    public void onTrackCompleted() {
        Utils.log(TAG, "PlayerFragmentCommunication.onTrackCompleted()");

        // Reset seek bar & seek bar text values and our media controller buttons
        stopSeekBarUpdates();
        mSeekBar.setProgress(0);
        mAudioService.getPlayer().seekTo(0);
        mPlayButton.setImageResource(android.R.drawable.ic_media_play);
        mElapsedTimeTextView.setText("00:00");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log(TAG, "onDestroy()");
        stopSeekBarUpdates();
    }

    void pausePlayer() {
        mAudioService.getPlayer().pause();
        mPlayButton.setImageResource(android.R.drawable.ic_media_play);
    }

    void resumePlayer() {
        mAudioService.getPlayer().resume();
        mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
    }

    private boolean isNewPlaylist() {
        if (mTrackList.get(0).getArtistName().equals(mAudioService.getPlayer().getPlaylistId())) {
            return false;
        }
        return true;
    }

    private boolean isNewTrack() {
        if (mPlayListIndex == mAudioService.getPlayer().getPlaylistIndex()) {
            return false;
        }
        return true;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Utils.log(TAG, "onServiceConnected() - AudioPlayerService: CONNECTED");

        mAudioService = ((AudioPlayerService.LocalBinder) service).getService();

        // For service-to-client communication
        mAudioService.setCallback(this);

        // TEST: detect when NowPlaying is called from a notification (pendingIntent)
        if (mTrackList == null) {
            Utils.log(TAG, "onServiceConnected() - mTrackList is null!");
            mTrackList = mAudioService.getPlayer().getPlaylist();
            mPlayListIndex = mAudioService.getPlayer().getPlaylistIndex();
            int trackDuration = mAudioService.getPlayer().getTrackDuration();
            onReceiveTrackDuration(trackDuration);
        }

        else {
            // First playlist or a new playlist
            if (isNewPlaylist()) {
                mAudioService.getPlayer().setPlaylist(mTrackList);
                mAudioService.getPlayer().play(mPlayListIndex);
            }

            // Another track from the same playlist
            else if (isNewTrack()) {
                mAudioService.getPlayer().play(mPlayListIndex);
            }
        }
        displayTrackInfo(mAudioService.getPlayer().getTrackInfo());
        App.setNowPlayingViewCreated();
    }

    // BACKUP
    /*@Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Utils.log(TAG, "onServiceConnected() - AudioPlayerService: CONNECTED");

        mAudioService = ((AudioPlayerService.LocalBinder) service).getService();

        // For service-to-client communication
        mAudioService.setCallback(this);

        // Use Cases

        // 1- User has pressed on HOME then long-press HOME then the app
        //  Actions to take:
        //      A) Do nothing (keep existing playlist, keep playing currently playing track)
        if (mTrackList.get(0).getArtistName().equals(mAudioService.getPlayer().getPlaylistId()) &&
                mPlayListIndex == mAudioService.getPlayer().getPlaylistIndex()) {
            // Do nothing!
            return;
        }

        else {

            // 2- User has selected a track in another playlist
            //    Actions to take:
            //      A) Send new playlist to service
            //      B) Stop currently playing track
            //      C) Play the new track
            if (mAudioService.getPlayer().isPlaylistEmpty() ||
                    !mTrackList.get(0).getArtistName().equals(mAudioService.getPlayer().getPlaylistId())) {
                // Send playlist to service and send also the index of the track to play
                mAudioService.getPlayer().setPlaylist(mTrackList); // Send top tracks list to service
                Utils.log(TAG, "onServiceConnected() - Playlist sent to service");
            }

            // 3- User has selected another track in the same playlist
            //  Actions to take:
            //      A) Stop playing the current track
            //      B) Play the new track

            mAudioService.getPlayer().play(mPlayListIndex);
            displayTrackInfo(mAudioService.getPlayer().getTrackInfo());

            // We will only set service as foreground when this view has been shown at least once
            // TODO: Is there a better way?
            App.setNowPlayingViewCreated();

            // 4- User has pressed on HOME then drawer then the app notification
            //  Actions to take: ???

        }
    }*/

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // Make sure service cannot send us anything once we are disconnected
        mAudioService.setCallback(null);
        mAudioService = null;
        Utils.log(TAG, "onServiceConnected() - AudioPlayerService: DISCONNECTED");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAudioService != null) {
            getActivity().unbindService(this);

            //--- TEST: 2015-07-26, added 14h54
            // Make sure service cannot send us anything once we are disconnected
            mAudioService.setCallback(null);
            mAudioService = null;
            //---------------------------

            Utils.log(TAG, "onPause() - AudioPlayerService: UNBINDED");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent bindIntent = new Intent(getActivity(), AudioPlayerService.class);
        getActivity().bindService(bindIntent, this, Activity.BIND_AUTO_CREATE);
        Utils.log(TAG, "onResume() - AudioPlayerService: BINDED");
    }
}
