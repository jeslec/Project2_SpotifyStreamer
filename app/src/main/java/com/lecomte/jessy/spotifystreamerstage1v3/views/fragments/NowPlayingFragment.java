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
import android.support.v4.app.NavUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.NowPlayingFragmentData;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.AudioPlayerService;
import com.lecomte.jessy.spotifystreamerstage1v3.other.observables.ObservablePlayPauseState;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.TopTracksActivity;
import com.squareup.picasso.Picasso;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jessy on 2015-07-20.
 */
public class NowPlayingFragment extends DialogFragment implements ServiceConnection,
        AudioPlayer.Callback,
        Observer {

    //**********************************************************************************************
    // CONSTANTS
    //**********************************************************************************************

    private final String TAG = getClass().getSimpleName();
    public static final String EXTRA_FRAGMENT_DATA = "com.lecomte.jessy.spotifystreamerstage1v3.fragmentData";;
    /*static final String EXTRA_TRACK_INFO = "com.lecomte.jessy.spotifystreamerstage1v3.trackInfo";
    static final String EXTRA_ARTIST_NAME = "com.lecomte.jessy.spotifystreamerstage1v3.artistName";*/
    static final int SEEK_BAR_UPDATE_INTERVAL = 40; // milliseconds
    static final int SEEK_BAR_TEXT_UPDATE_INTERVAL = 1000; // milliseconds
    public static final String ACTION_LOAD_PLAYLIST_PLAY_TRACK =
            "com.lecomte.jessy.spotifystreamerstage1v3.action.loadPlaylistPlayTrack";
    public static final String ACTION_SHOW_PLAYER =
            "com.lecomte.jessy.spotifystreamerstage1v3.action.showPlayer";
    public static final String ACTION_PLAY_TRACK =
            "com.lecomte.jessy.spotifystreamerstage1v3.action.playTrack";

    //**********************************************************************************************
    // VARIABLES
    //**********************************************************************************************

    //**** [Primitive] ****
    private int mSeekBarProgress = 0;
    // True: means NowPlaying was loaded from TopTracks
    // False: NowPlaying was loaded from notification or recently opened apps drawer
    //private boolean mIsFromTopTracks = false;

    //**** [Widgets] ****
    private ImageButton mShareButton;
    private TextView mElapsedTimeTextView;
    private TextView mTotalTimeTextView;
    private TextView mArtistTextView;
    private TextView mTrackTextView;
    private TextView mAlbumTextView;
    private ImageView mAlbumImageView;
    private SeekBar mSeekBar;
    private ImageButton mPlayButton;
    private ImageButton mPrevTrackButton;
    private ImageButton mNextTrackButton;

    //**** [Other] ****
    private Handler mSeekBarHandler = new Handler();
    private Handler mSeekBarTextHandler = new Handler();
    private Runnable mUpdateSeekBarRunnable;
    private Runnable mUpdateSeekBarTextRunnable;
    private String mTrackUrl = "";
    private AudioPlayerService mAudioService;
    NowPlayingFragmentData mFragmentData = new NowPlayingFragmentData();

    //**********************************************************************************************
    // FRAMEWORK METHODS
    //**********************************************************************************************

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Required to get action bar back button to do something useful (go back to previous view)
        setHasOptionsMenu(true);

        // Make the dialog modal so it does not accept input outside the dialog area
        // TODO: Change theme for one that doesn't have a transparent background
        setStyle(STYLE_NO_FRAME, 0);
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
        //mIsFromTopTracks = true;
        getActivity().startService(new Intent(getActivity(), AudioPlayerService.class));

        getFragmentData();

        View v = inflater.inflate(R.layout.fragment_now_playing, container, false);

        getWidgets(v);
        setWidgetsListeners();
        setWidgetsProperties();
        disableWidgets();

        return v;
    }

    // The system calls this only when creating the layout in a dialog
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Utils.log(TAG, "onCreateDialog()");

        // Required to get action bar back button to do something useful (go back to previous view)
        //setHasOptionsMenu(true);

        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.log(TAG, "onPause()");
        if (mAudioService != null) {
            getActivity().unbindService(this);

            // Make sure service cannot send us anything once we are disconnected

            // Don't receive play/pause state
            mAudioService.getPlayer().removePlayPauseStateObserver(this);

            mAudioService.removeListener(this);
            mAudioService = null;
            Utils.log(TAG, "onPause() - AudioPlayerService: UNBINDED");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.log(TAG, "onResume()");

        // Connect to audio service so we can send it requests (play, pause, etc.)
        Intent bindIntent = new Intent(getActivity(), AudioPlayerService.class);
        getActivity().bindService(bindIntent, this, Activity.BIND_AUTO_CREATE);
        Utils.log(TAG, "onResume() - AudioPlayerService: BINDED");
    }

    @Override
    public void onDestroy() {
        Utils.log(TAG, "onDestroy()");
        super.onDestroy();
        stopSeekBarUpdates();
    }

    // Based on book Big Nerd Ranch Android: p.274-275
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // The left-pointing arrow located to the left of the action bar title
            case android.R.id.home:
                // This activity's parent must be specified in meta-data section of manifest
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    // Go back to the previous (parent) view
                    //NavUtils.navigateUpFromSameTask(getActivity());

                    // TODO: Find a way not to hardcode the class name (TopTracks)
                    // Did this because navigateUpFromSameTask calls onCreate() of
                    // TopTracksActivity(). This way, onCreate does not get called
                    // Or find a way to add flags to navigateUpTo() or similar method
                    Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.navigateUpTo(getActivity(), intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //**********************************************************************************************
    // API INTERFACE IMPLEMENTATION METHODS
    //**********************************************************************************************

    //**** [Interface: ServiceConnection] ****

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Utils.log(TAG, "onServiceConnected() - AudioPlayerService: CONNECTED");

        // Get notified when play/pause state of media player changes
        // Get notified when track is done playing (reached the end)
        mAudioService = ((AudioPlayerService.LocalBinder)service).getService();
        mAudioService.getPlayer().addPlayPauseStateObserver(this);
        mAudioService.getPlayer().addListener(this);

        // New playlist so therefore a new track as well
        if (isNewPlaylist()) {
            Utils.log(TAG, "onServiceConnected() - New playlist");
            if (mFragmentData != null) {
                mAudioService.getPlayer().setPlaylist(mFragmentData.getTrackList());
                mAudioService.getPlayer().play(mFragmentData.getTrackIndex());
            }
        }

        // Another track selection from the same playlist
        else if (isNewTrack()) {
            Utils.log(TAG, "onServiceConnected() - New track");
            mAudioService.getPlayer().play(mFragmentData.getTrackIndex());
        }

        updateWidgets(mAudioService.getPlayer().getTrackInfo());
        setPlayPauseButtonImage(mAudioService.getPlayer().isPlayState());
        enableWidgets();

        // Once action has been handled, reset intent action to default action
        // This action is received when fragment is started from notification or app history drawer
        getActivity().getIntent().setAction(NowPlayingFragment.ACTION_SHOW_PLAYER);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Utils.log(TAG, "onServiceConnected() - AudioPlayerService: DISCONNECTED");
        // Make sure service cannot send us anything once we are disconnected
        mAudioService.removeListener(this);
        mAudioService.getPlayer().removePlayPauseStateObserver(this);
        mAudioService = null;
    }

    //**********************************************************************************************
    // CUSTOM INTERFACE IMPLEMENTATION METHODS
    //**********************************************************************************************

    //**** [Interface: AudioPlayer.Callback] ****

    // Start updating the seek bar and the seek bar text values at regular intervals
    // We use 2 different intervals because the seek bar needs to be updated much more often
    // (at a perceivable real-time rate >= 25 fps) than the text values which change every second
    public void onReceiveTrackDuration(long duration) {
        // Cancel all previous runnables
        stopSeekBarUpdates();

        // Update seek bar progression
        mUpdateSeekBarRunnable = new Runnable() {
            @Override public void run() {
                if (mAudioService != null && mAudioService.getPlayer() != null
                        && mSeekBar != null) {
                    mSeekBar.setProgress(mAudioService.getPlayer().getCurrentPosition());
                }

                if (mSeekBarHandler != null) {
                    mSeekBarHandler.postDelayed(this, SEEK_BAR_UPDATE_INTERVAL);
                }
            }
        };

        // Update seek bar elapsed time in textView
        mUpdateSeekBarTextRunnable = new Runnable() {
            @Override public void run() {
                //Utils.log(TAG, "Runnable.run() - Current position: " + mAudioService.getPlayer().getCurrentPosition());
                if (mAudioService != null && mAudioService.getPlayer() != null
                        && mElapsedTimeTextView != null) {
                    Pair<Long, Long> minSecPair = Utils.msecToMinSec(mAudioService.getPlayer().getCurrentPosition());

                    mElapsedTimeTextView.setText(getResources()
                            .getString(R.string.NowPlaying_elapsedTime, minSecPair.first,
                                    minSecPair.second));
                }

                if (mSeekBarTextHandler != null) {
                    mSeekBarTextHandler.postDelayed(this, SEEK_BAR_TEXT_UPDATE_INTERVAL);
                }
            }
        };

        if (mSeekBar != null) {
            mSeekBar.setMax((int)duration);
        }


        Pair<Long, Long> minSecPair = Utils.msecToMinSec((int)duration);

        if (mTotalTimeTextView != null) {
            mTotalTimeTextView.setText(getResources()
                    .getString(R.string.NowPlaying_totalTime, minSecPair.first,
                            minSecPair.second));
        }

        // The runnable must be called once explicitly for it to be called automatically after
        // http://stackoverflow.com/questions/21929529/runnable-not-running-at-all-inside-fragment#21929571
        if (mSeekBarHandler != null) {
            mSeekBarHandler.post(mUpdateSeekBarRunnable);
        }

        if (mSeekBarTextHandler != null) {
            mSeekBarTextHandler.post(mUpdateSeekBarTextRunnable);
        }
    }

    // This is called when the track is done playing
    public void onTrackCompleted() {
        Utils.log(TAG, "onTrackCompleted()");

        // Reset seek bar & seek bar text values and our media controller buttons
        stopSeekBarUpdates();

        if (mSeekBar != null) {
            mSeekBar.setProgress(0);
        }

        //mPlayButton.setImageResource(android.R.drawable.ic_media_play);
        if (mElapsedTimeTextView != null) {
            mElapsedTimeTextView.setText("00:00");
        }

        /*if (mAudioService != null) {
            mAudioService.getPlayer().seekTo(0);
        }*/
    }

    @Override
    public void update(Observable observable, Object data) {
        ObservablePlayPauseState trackPlayingState = (ObservablePlayPauseState) observable;
        Utils.log(TAG, "PlayPauseStateObserver.update() - isTrackPlaying: " +
                trackPlayingState.isTrackPlaying());

        setPlayPauseButtonImage(trackPlayingState.isTrackPlaying());
    }

    //**********************************************************************************************
    // NON-FRAMEWORK METHODS
    //**********************************************************************************************

    private void getWidgets(View v) {
        mSeekBar = (SeekBar)v.findViewById(R.id.NowPlaying_seekBar);
        mArtistTextView = (TextView)v.findViewById(R.id.NowPlaying_artistName);
        mTrackTextView = (TextView)v.findViewById(R.id.NowPlaying_trackName);
        mAlbumTextView = (TextView)v.findViewById(R.id.NowPlaying_albumName);
        mElapsedTimeTextView = (TextView)v.findViewById(R.id.NowPlaying_elapsedTime);
        mTotalTimeTextView = (TextView)v.findViewById(R.id.NowPlaying_totalTime);
        mAlbumImageView = (ImageView)v.findViewById(R.id.NowPlaying_albumImage);
        mShareButton = (ImageButton)v.findViewById(R.id.NowPlaying_shareButton);
        mPrevTrackButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonPrevious);
        mPlayButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonPlay);
        mNextTrackButton = (ImageButton)v.findViewById(R.id.NowPlaying_buttonNext);
    }

    private void setWidgetsListeners() {

        if (mShareButton != null) {
            mShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String trackUrl = mAudioService.getPlayer().getTrackInfo().getTrackPreviewUrl();
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, trackUrl);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
            });
        }

        if (mPlayButton != null) {
            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.log(TAG, "Clicked on: play/pause button");
                    mAudioService.getPlayer().togglePlayPauseState();
                }
            });
        }

        if (mPrevTrackButton != null) {
            mPrevTrackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.log(TAG, "Clicked on: PREVIOUS");
                    mAudioService.getPlayer().playPrevious();
                    displayTrackInfo(mAudioService.getPlayer().getTrackInfo());
                }
            });
        }

        if (mNextTrackButton != null) {
            mNextTrackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.log(TAG, "Clicked on: NEXT");
                    mAudioService.getPlayer().playNext();
                    displayTrackInfo(mAudioService.getPlayer().getTrackInfo());
                }
            });
        }

        if (mSeekBar != null) {
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
        }
    }

    private void setWidgetsProperties() {
        if (mPlayButton != null) {
            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
        }

        if (mPrevTrackButton != null) {
            mPrevTrackButton.setImageResource(android.R.drawable.ic_media_previous);
        }

        if (mNextTrackButton != null) {
            mNextTrackButton.setImageResource(android.R.drawable.ic_media_next);
        }
    }

    private void enableWidgets() {
        setEnableWidgets(true);
    }

    private void disableWidgets() {
        setEnableWidgets(false);
    }

    private void setEnableWidgets(boolean bEnable) {
        if (mPlayButton != null) {
            mPlayButton.setEnabled(bEnable);
        }

        if (mPrevTrackButton != null) {
            mPrevTrackButton.setEnabled(bEnable);
        }

        if (mNextTrackButton != null) {
            mNextTrackButton.setEnabled(bEnable);
        }

        if (mSeekBar != null) {
            mSeekBar.setEnabled(bEnable);
        }

        if (mShareButton != null) {
            mShareButton.setEnabled(bEnable);
        }
    }

    private void updateWidgets(TrackInfo track) {
        Utils.log(TAG, "updateWidgets() - Track duration: " + track.getTrackDuration());
        displayTrackInfo(track);

        // Track duration is 0 until the first onReceiveTrackDuration() is received
        // When fragment is started from notification, the track length is valid so we set it
        if (track.getTrackDuration() > 0) {
            onReceiveTrackDuration(track.getTrackDuration());
        }
    }

    private void setPlayPauseButtonImage(boolean trackIsPlaying) {
        if (mPlayButton ==  null) {
            return;
        }

        // Set as pause button
        if (trackIsPlaying) {
            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
            Utils.log(TAG, "setPlayPauseButtonImage() - Button set to: PAUSE");
        }

        else {
            mPlayButton.setImageResource(android.R.drawable.ic_media_play);
            Utils.log(TAG, "setPlayPauseButtonImage() - Button set to: PLAY");
        }
    }

    // Stop updating seek bar and text values
    public void stopSeekBarUpdates() {
        Utils.log(TAG, "stopSeekBarUpdates()");
        if (mSeekBarHandler != null) {
            mSeekBarHandler.removeCallbacks(mUpdateSeekBarRunnable);
        }

        if (mSeekBarHandler != null) {
            mSeekBarHandler.removeCallbacks(mUpdateSeekBarRunnable);
        }
    }

    private void  pausePlayer() {
        mAudioService.getPlayer().pause();
    }

    private void resumePlayer() {
        mAudioService.getPlayer().resume();
    }

    private boolean isNewPlaylist() {
        Intent intent = getActivity().getIntent();

        if (intent.getAction() == ACTION_LOAD_PLAYLIST_PLAY_TRACK) {
            return true;
        }
        return false;
    }

    private boolean isNewTrack() {
        Intent intent = getActivity().getIntent();

        if (intent.getAction() == ACTION_PLAY_TRACK) {
            return true;
        }
        return false;
    }

    private void getFragmentData() {

        Intent intent = getActivity().getIntent();

        // Fragment was "started" with newInstance(): this happens in a 2-pane layout
        /*if (getActivity().getIntent().getAction() == NowPlayingFragment.ACTION_LOAD_PLAYLIST_PLAY_TRACK) {
            final Bundle args = getArguments();
            ArrayList<TrackInfo> trackList = args.getParcelableArrayList(TopTracksActivity.EXTRA_TRACK_LIST);
            mFragmentData.setTrackList(trackList);
            mFragmentData.setTrackIndex(args.getInt(TopTracksActivity.EXTRA_TRACK_INDEX, 0));
        }*/

        // Fragment was "started" with an intent: this happens in a single-pane layout
        //else {
            mFragmentData = intent.getParcelableExtra(EXTRA_FRAGMENT_DATA);
        //}

        if (mFragmentData != null) {
            Utils.log(TAG, "getFragmentData() - [TrackList size: "
                    + (mFragmentData.getTrackList() == null ? "null" : mFragmentData.getTrackList().size() + "] ")
                    + "[Track index: " + mFragmentData.getTrackIndex() + "]");
        }
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

    //int This is  how we send data to the fragment
    public static NowPlayingFragment newInstance(NowPlayingFragmentData data) {
        Bundle args = new Bundle();
        args.putParcelable(NowPlayingFragment.EXTRA_FRAGMENT_DATA, data);
        NowPlayingFragment fragment = new NowPlayingFragment();
        fragment.setArguments(args);
        return fragment;
    }
}