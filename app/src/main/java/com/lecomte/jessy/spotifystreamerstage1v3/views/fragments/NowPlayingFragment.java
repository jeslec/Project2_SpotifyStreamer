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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.NowPlayingFragmentData;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.AudioPlayerService;
import com.lecomte.jessy.spotifystreamerstage1v3.other.observables.ObservablePlayPauseState;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.TopTracksActivity;
import com.squareup.okhttp.internal.Util;
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
    static final int SEEK_BAR_UPDATE_INTERVAL = 40; // milliseconds
    static final int SEEK_BAR_TEXT_UPDATE_INTERVAL = 1000; // milliseconds
    public static final String ACTION_LOAD_PLAYLIST_PLAY_TRACK =
            "com.lecomte.jessy.spotifystreamerstage1v3.action.ACTION_LOAD_PLAYLIST_PLAY_TRACK";
    public static final String ACTION_SHOW_PLAYER =
            "com.lecomte.jessy.spotifystreamerstage1v3.action.ACTION_SHOW_PLAYER";
    public static final String ACTION_PLAY_TRACK =
            "com.lecomte.jessy.spotifystreamerstage1v3.action.ACTION_PLAY_TRACK";
    public static final String ACTION_SHOW_PLAYER_NOTIFICATION =
            "com.lecomte.jessy.spotifystreamerstage1v3.action.ACTION_SHOW_PLAYER_NOTIFICATION";

    //**********************************************************************************************
    // MEMBER VARIABLES
    //**********************************************************************************************

    //**** [Primitive] ****
    private int mSeekBarProgress = 0;
    private int mTrackEndThreshold = 0;
    private float mWidthMultiplier = 1;
    private float mHeightMultiplier = 1;
    private float mDimAmount = 0;

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
        Utils.log(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        // Maintain states between configuration changes (phone rotations, etc.)
        setRetainInstance(true);

        // Required to get action bar back button to do something useful (go back to previous view)
        setHasOptionsMenu(true);

        // Make the dialog modal so it does not accept input outside the dialog area
        // http://stackoverflow.com/questions/12322356/how-to-make-dialogfragment-modal
        setStyle(STYLE_NO_FRAME, 0);

        // Number of milliseconds from track end at which point we consider track finished
        // Used in special case where player is loaded from notification and track has ended
        TypedValue trackEndThreshold = new TypedValue();
        getResources().getValue(R.dimen.track_end_threshold, trackEndThreshold, false);
        mTrackEndThreshold = trackEndThreshold.data;

        // Extract float values from dimens.xml explained here:
        // http://stackoverflow.com/questions/3282390/add-floating-point-value-to-android-resources-values#8780360
        // This is done only if NowPlayingFragment is a dialog (as opposed to fullscreen activity)
        if (App.isTwoPaneLayout()) {
            TypedValue width = new TypedValue();
            TypedValue height = new TypedValue();
            TypedValue dim = new TypedValue();

            getResources().getValue(R.dimen.dialog_window_width, width, true);
            getResources().getValue(R.dimen.dialog_window_height, height, true);
            getResources().getValue(R.dimen.dim_behind_dialog_amount, dim, true);

            mWidthMultiplier = width.getFloat();
            mHeightMultiplier = height.getFloat();
            mDimAmount = dim.getFloat();
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

        if (Utils.isServiceRunning(AudioPlayerService.class)) {
            Utils.log(TAG, "onCreateView() - Audio service: already started");
        } else {
            ComponentName name = getActivity()
                    .startService(new Intent(getActivity(), AudioPlayerService.class));
            Utils.log(TAG, "onCreateView() - Audio service started: " + (name==null?"false":"true"));
        }

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

        // IMPORTANT: This removes fragment from layout and from back stack
        // Without it, I was getting weird bugs where after a device rotation, the
        // NowPlayingFragment would not be visible (although it was before the rotation)
        //dismiss();

        if (mAudioService != null) {
            Utils.log(TAG, "onPause() - Stopping seek bar updates...");
            stopSeekBarUpdates();

            // Don't receive play/pause state updates
            Utils.log(TAG, "onPause() - Removing play/pause state observer...");
            mAudioService.getPlayer().removePlayPauseStateObserver(this);

            // Don't receive onTrackCompleted events
            Utils.log(TAG, "onPause() - Removing track completed listener...");
            mAudioService.removeListener(this);

            Utils.log(TAG, "onPause() - Unbinding audio service...");
            getActivity().unbindService(this);
            mAudioService = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // TODO: Consider optimizing but be careful of configuration changes (phone rotations)
        // Resize dialog window (a dialog window is only used in a 2-pane configuration,
        // in a 1-pane configuration we use a fullscreen activity)
        // Resizing of window must be done in onStart() or onResume() as explained here:
        // http://w3facility.org/question/how-to-set-dialogfragments-width-and-height/?r=3#answer-21966763
        if (App.isTwoPaneLayout()) {
            Window dialogWindow = getDialog().getWindow();
            int dimFlag = WindowManager.LayoutParams.FLAG_DIM_BEHIND;

            // Get screen dimensions and other display metrics, code from:
            // http://developer.android.com/reference/android/util/DisplayMetrics.html
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            // Resize dialog window so it takes maximum advantage of each device's screen size
            dialogWindow.setLayout((int) (mWidthMultiplier * metrics.widthPixels),
                    (int)(mHeightMultiplier * metrics.heightPixels));

            // Dim behind this dialog (must be called after dialog is created and view is set)
            dialogWindow.setFlags(dimFlag, dimFlag);
            dialogWindow.setDimAmount(mDimAmount);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.log(TAG, "onResume()");

        // Connect to audio service so we can send it requests (play, pause, etc.)
        Intent bindIntent = new Intent(getActivity(), AudioPlayerService.class);
        boolean binded = getActivity().bindService(bindIntent, this, Activity.BIND_AUTO_CREATE);
        Utils.log(TAG, "onResume() - AudioPlayerService binded: " + binded);
    }

    // IMPORTANT: Don't waste your time with this method as it is not garanteed to be called
    @Override
    public void onDestroy() {
        Utils.log(TAG, "onDestroy()");
        super.onDestroy();
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Utils.log(TAG, "onAttach() - Fragment attached to activity: " + activity.getLocalClassName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Utils.log(TAG, "onDetach() - Fragment detached from activity");
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
                updateSeekBarProgress();

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

        if (mElapsedTimeTextView != null) {
            mElapsedTimeTextView.setText("00:00");
        }
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

    void updateSeekBarProgress() {
        if (mAudioService != null && mAudioService.getPlayer() != null && mSeekBar != null) {
            mSeekBar.setProgress(mAudioService.getPlayer().getCurrentPosition());
        }
    }

    private void updateWidgets(TrackInfo track) {
        Utils.log(TAG, "updateWidgets() - Track duration: " + track.getTrackDuration());
        displayTrackInfo(track);

        // Special case: track ended when app not visible and player is started from notification
        // In this case, we reset the seek bar and text values when app is reloaded
        if (!mAudioService.getPlayer().isPlaying() && track.getTrackDuration() > 0 &&
                mAudioService.getPlayer().getCurrentPosition() >
                        (track.getTrackDuration() - mTrackEndThreshold)) {
            onTrackCompleted();
            return;
        }

        // Track duration is 0 until the first onReceiveTrackDuration() is received
        // When fragment is started from notification, the track length is valid so we set it
        if (track.getTrackDuration() > 0) {
            onReceiveTrackDuration(track.getTrackDuration());
        }

        // This fixes issue where player is in paused state and NowPlaying is closed and reloaded
        // When paused, seek bar does not get continously updated so we need to update it once
        updateSeekBarProgress();
    }

    private void setPlayPauseButtonImage(boolean trackIsPlaying) {
        if (mPlayButton ==  null) {
            return;
        }

        // Set as pause button
        if (trackIsPlaying) {
            // Resume updating seek bar and related text values
            onReceiveTrackDuration(mAudioService.getPlayer().getTrackDuration());

            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
            Utils.log(TAG, "setPlayPauseButtonImage() - Button set to: PAUSE");
        }

        else {
            // Optimization: stop updating seek bar and related text values
            stopSeekBarUpdates();

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

        if (mSeekBarTextHandler != null) {
            mSeekBarTextHandler.removeCallbacks(mUpdateSeekBarTextRunnable);
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

        if (intent == null) {
            Utils.log(TAG, "isNewPlaylist() - Intent is null!");
            return false;
        }

        if (intent.getAction() == ACTION_LOAD_PLAYLIST_PLAY_TRACK) {
            return true;
        }
        return false;
    }

    private boolean isNewTrack() {
        Intent intent = getActivity().getIntent();

        if (intent == null) {
            Utils.log(TAG, "isNewTrack() - Intent is null!");
            return false;
        }

        if (intent.getAction() == ACTION_PLAY_TRACK) {
            return true;
        }
        return false;
    }

    private void getFragmentData() {

        Intent intent = getActivity().getIntent();
        mFragmentData = intent.getParcelableExtra(EXTRA_FRAGMENT_DATA);

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

    // Use this when the action requires data (ACTION_LOAD_PLAYLIST_PLAY_TRACK, ACTION_PLAY_TRACK)
    public static NowPlayingFragment newInstance(NowPlayingFragmentData data) {
        Bundle args = new Bundle();
        args.putParcelable(NowPlayingFragment.EXTRA_FRAGMENT_DATA, data);
        NowPlayingFragment fragment = new NowPlayingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // Use this when the action does not require any data (ACTION_SHOW_PLAYER)
    public static NowPlayingFragment newInstance() {
        NowPlayingFragment fragment = new NowPlayingFragment();
        return fragment;
    }

    // This prevents us from having multiple instances of the dialog running
    //http://www.jorgecoca.com/android-quick-tip-avoid-opening-multiple-dialogs-when-tapping-an-element/
    /*@Override
    public void show(FragmentManager manager, String tag) {
        Utils.log(TAG, "show()");
        if (manager.findFragmentByTag(tag) == null) {
            Utils.log(TAG, "show() - Fragment not found, adding it to layout now");
            super.show(manager, tag);
        }
    }*/
}