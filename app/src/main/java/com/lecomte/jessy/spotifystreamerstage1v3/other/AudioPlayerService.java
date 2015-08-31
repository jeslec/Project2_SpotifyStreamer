package com.lecomte.jessy.spotifystreamerstage1v3.other;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.observables.ObservablePlayPauseState;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.MainActivity;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.NowPlayingActivity;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.TopTracksActivity;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.NowPlayingFragment;
import com.squareup.picasso.Picasso;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jessy on 2015-07-24.
 */
public class AudioPlayerService extends Service implements AudioPlayer.Callback,
        Observer {

    private static final String TAG = "AudioPlayerService";
    private static final long STOP_SERVICE_DELAY = 1000; // milliseconds
    private RemoteViews mNotificationRemoteView;
    private PendingIntent mPausePendingIntent;
    private PendingIntent mResumePendingIntent;
    private static Handler mStopServiceHandler = new Handler();
    private static Runnable mStopServiceRunnable;

    // Responses this service will send to the client
    public static final String RESPONSE_TRACK_PREPARED =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.response.TRACK_PREPARED";

    // Data this service will send to the client in his response
    public static final String EXTRA_TRACK_DURATION =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.extra.TRACK_DURATION";

    public static final String ACTION_START_FOREGROUND =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.START_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.STOP_FOREGROUND";

    private static final String ACTION_PLAY_NEXT_TRACK =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.PLAY_NEXT_TRACK";

    private static final String ACTION_PLAY_PREVIOUS_TRACK =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.PLAY_PREV_TRACK";

    private static final String ACTION_PAUSE =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.PAUSE";

    private static final String ACTION_RESUME =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.RESUME";

    public static final String ACTION_SHOW_NOTIFICATION =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.SHOW_NOTIFICATION";

    public static final String ACTION_HIDE_NOTIFICATION =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.HIDE_NOTIFICATION";

    public static final String ACTION_STOP_SERVICE =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.STOP_SERVICE";

    public static final String ACTION_CANCEL_TIMER =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.CANCEL_TIMER";

    private static final int NOTIFICATION_ID_AUDIO_SERVICE = 1000;

    private LocalBinder mLocalBinder = null;
    private AudioPlayer mAudioPlayer = null;
    private NotificationManager mNotificationManager;
    private boolean mIsForeground = false;

    public AudioPlayerService() {
        mAudioPlayer = new AudioPlayer();
        mLocalBinder = new LocalBinder();

        // Get notified when play/pause state of media player changes
        getPlayer().addPlayPauseStateObserver(this);

        mAudioPlayer.addListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    public AudioPlayer getPlayer() {
        return mAudioPlayer;
    }

    @Override
    public void onTrackCompleted() {
        Utils.log(TAG, "onTrackCompleted()");
        buildCustomNotification();
    }

    @Override
    public void onReceiveTrackDuration(long duration) {
        Utils.log(TAG, "onReceiveTrackDuration()");
        buildCustomNotification();
    }

    private void updateNotificationPlayPauseButton() {

        if (mNotificationRemoteView == null) {
            Utils.log(TAG, "updateNotificationPlayPauseButton() - mNotificationRemoteView is null!");
            return;
        }

        // Put "pause" button icon and set pending intent to call when button is pressed
        if (getPlayer().isPlaying()) {
            Utils.log(TAG, "updateNotificationPlayPauseButton() - Track playing, setting play/pause button to: PAUSE");
            mNotificationRemoteView.setInt(R.id.notification_buttonPlay, "setBackgroundResource",
                    android.R.drawable.ic_media_pause);
            mNotificationRemoteView.setOnClickPendingIntent(R.id.notification_buttonPlay,
                    mPausePendingIntent);

        } else {
            Utils.log(TAG, "updateNotificationPlayPauseButton() - Track NOT playing, setting play/pause button to: PLAY");
            // Set RemoteView widget background
            // http://stackoverflow.com/questions/6201410/how-to-change-widget-layout-background-programatically#14669011
            mNotificationRemoteView.setInt(R.id.notification_buttonPlay, "setBackgroundResource",
                    android.R.drawable.ic_media_play);
            // Set an onClick event for a widget located in a remoteView
            // http://stackoverflow.com/questions/22585696/android-notification-with-remoteviews-having-activity-associated-with-remotevi#22585875
            mNotificationRemoteView.setOnClickPendingIntent(R.id.notification_buttonPlay,
                    mResumePendingIntent);
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        ObservablePlayPauseState observablePlayPauseState = (ObservablePlayPauseState) observable;
        Utils.log(TAG, "PlayPauseStateObserver.update() - Track is playing: " +
                observablePlayPauseState.isTrackPlaying());

        buildCustomNotification();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AudioPlayerService.this;
        }
    }

    public void addListener(AudioPlayer.Callback callback) {
        if (mAudioPlayer != null) {
            mAudioPlayer.addListener(callback);
        }
    }

    private Notification buildCustomNotification() {

        // Don't build a notification if notifications are disabled in the app settings
        if (!App.isNotificationEnabled()) {
            return null;
        }

        // Get currently playing track info (or last played)
        TrackInfo track = mAudioPlayer.getTrackInfo();

        mNotificationRemoteView = new RemoteViews(getPackageName(), R.layout.notification_player);

        // NowPlaying: Either start it as a fullscreen activity or as dialog
        // 2-pane layout: dialog; 1-pane layout: fullscreen activity
        Intent intent = new Intent(this, NowPlayingActivity.class);

        // Tell the MainActivity to load the NowPlaying fragment in its layout
        if (App.isTwoPaneLayout()) {
            intent.setClass(this, MainActivity.class);
            intent.setAction(NowPlayingFragment.ACTION_SHOW_PLAYER_NOTIFICATION_CASE);
            //intent.setAction(NowPlayingFragment.ACTION_SHOW_PLAYER);
        }

        /*Intent topTracksIntent = new Intent(this, TopTracksActivity.class);
        topTracksIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);*/

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Since topTracks results are not saved to a DB, there is no point returning to empty view
        // after back button is pressed from NowPlaying so we load the MainActivity instead
        /*TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this)
            //.addNextIntent(new Intent(this, MainActivity.class))
            //.addNextIntent(topTracksIntent)
            .addNextIntent(intent);

        PendingIntent pendingIntent = taskStackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);*/

        Utils.log(TAG, "buildCustomNotification() - PendingIntent class set to: "
                + (App.isTwoPaneLayout() ? "MainActivity" : "NowPlayingActivity"));

        // Load app when user clicks on album image
        mNotificationRemoteView.setOnClickPendingIntent(R.id.notification_imageAlbum,
                pendingIntent);

        // Previous track button intent
        Intent previousIntent = new Intent(this, AudioPlayerService.class);
        previousIntent.setAction(AudioPlayerService.ACTION_PLAY_PREVIOUS_TRACK);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        // Pause track button intent
        Intent pauseIntent = new Intent(this, AudioPlayerService.class);
        pauseIntent.setAction(AudioPlayerService.ACTION_PAUSE);
        mPausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        // Resume track button intent
        Intent resumeIntent = new Intent(this, AudioPlayerService.class);
        resumeIntent.setAction(AudioPlayerService.ACTION_RESUME);
        mResumePendingIntent = PendingIntent.getService(this, 0, resumeIntent, 0);

        // Next track button intent
        Intent nextIntent = new Intent(this, AudioPlayerService.class);
        nextIntent.setAction(AudioPlayerService.ACTION_PLAY_NEXT_TRACK);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        // Set onClick events for media control buttons: each button calls a pending intent
        mNotificationRemoteView.setOnClickPendingIntent(R.id.notification_buttonPrev,
                prevPendingIntent);
        mNotificationRemoteView.setOnClickPendingIntent(R.id.notification_buttonNext,
                nextPendingIntent);

        updateNotificationPlayPauseButton();

        // Set notification texts
        mNotificationRemoteView.setTextViewText(R.id.notification_textTrack, track.getTrackName());
        mNotificationRemoteView.setTextViewText(R.id.notification_textArtist, track.getArtistName());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContent(mNotificationRemoteView)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_audio_player)
                //.setLargeIcon() // TODO: set large icon
                //.setDeleteIntent() // TODO: determine what needs to be done when the notification is cleared
                .setContentTitle(track.getTrackName())
                .setContentText(track.getArtistName())
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true); // user cannot remove notification from the notification drawer

        Notification notification = builder.build();

        // Load image asynchronously for this notification (see lines 51-56 on page of link)
        // https://github.com/square/picasso/blob/master/picasso-sample/src/main/java/com/example/picasso/PicassoSampleAdapter.java
        // TODO: Make sure the image is not reloaded from server every time we update the notification
        Picasso.with(this).load(track.getAlbumSmallImageUrl())
                .resizeDimen(R.dimen.notification_icon_width_height,
                        R.dimen.notification_icon_width_height)
                .into(mNotificationRemoteView, R.id.notification_imageAlbum,
                        NOTIFICATION_ID_AUDIO_SERVICE, notification);

        return notification;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int returnCode = START_STICKY;

        if (intent == null || intent.getAction() == null) {
            Utils.log(TAG, "onStartCommand() - intent or action is null!");
            return returnCode;
        }

        String action = intent.getAction();

        if (action.equals(ACTION_START_FOREGROUND)) {
            if (App.isNotificationEnabled() && !mIsForeground) {
                startForeground(NOTIFICATION_ID_AUDIO_SERVICE, buildCustomNotification());
                mIsForeground = true;
                Utils.log(TAG, "onStartCommand() - Service set to: FOREGROUND");
            }
        }

        else if (action.equals(ACTION_STOP_FOREGROUND)) {
            if (mIsForeground) {
                stopForeground(true);
                mIsForeground = false;
                Utils.log(TAG, "onStartCommand() - Service set to: BACKGROUND");
            }
        }

        else if (action.equals(ACTION_PLAY_NEXT_TRACK)) {
            //Utils.log(TAG, "onStartCommand() - Action: ACTION_PLAY_NEXT_TRACK");
            Utils.log(TAG, "Notification - Clicked on: NEXT");
            mAudioPlayer.playNext();
            mNotificationManager.notify(NOTIFICATION_ID_AUDIO_SERVICE, buildCustomNotification());
        }

        else if (action.equals(ACTION_PLAY_PREVIOUS_TRACK)) {
            //Utils.log(TAG, "onStartCommand() - Action: ACTION_PLAY_PREVIOUS_TRACK");
            Utils.log(TAG, "Notification - Clicked on: PREVIOUS");
            mAudioPlayer.playPrevious();
            mNotificationManager.notify(NOTIFICATION_ID_AUDIO_SERVICE, buildCustomNotification());
        }

        else if (action.equals(ACTION_PAUSE)) {
            //Utils.log(TAG, "onStartCommand() - Action: ACTION_PAUSE");
            Utils.log(TAG, "Notification - Clicked on: PAUSE");
            mAudioPlayer.pause();
            mNotificationManager.notify(NOTIFICATION_ID_AUDIO_SERVICE, buildCustomNotification());
        }

        else if (action.equals(ACTION_RESUME)) {
            //Utils.log(TAG, "onStartCommand() - Action: ACTION_RESUME");
            Utils.log(TAG, "Notification - Clicked on: PLAY");
            mAudioPlayer.resume();
            mNotificationManager.notify(NOTIFICATION_ID_AUDIO_SERVICE, buildCustomNotification());
        }

        else if (action.equals(ACTION_SHOW_NOTIFICATION)) {
            Utils.log(TAG, "onStartCommand() - ACTION_SHOW_NOTIFICATION");
            mNotificationManager.notify(NOTIFICATION_ID_AUDIO_SERVICE, buildCustomNotification());
        }

        else if (action.equals(ACTION_HIDE_NOTIFICATION)) {
            Utils.log(TAG, "onStartCommand() - ACTION_HIDE_NOTIFICATION");
            mNotificationManager.cancel(NOTIFICATION_ID_AUDIO_SERVICE);
        }

        else if (action.equals(ACTION_STOP_SERVICE)) {
            if (mStopServiceHandler != null && mStopServiceRunnable != null) {
                Utils.log(TAG, "onStartCommand() - ACTION_STOP_SERVICE: starting timer...");
                mStopServiceHandler.postDelayed(mStopServiceRunnable, STOP_SERVICE_DELAY);
            }
        }

        else if (action.equals(ACTION_CANCEL_TIMER)) {
            if (mStopServiceHandler != null && mStopServiceRunnable != null) {
                Utils.log(TAG, "onStartCommand() - ACTION_CANCEL_TIMER: stopping timer...");
                mStopServiceHandler.removeCallbacks(mStopServiceRunnable);
            }
        }

        // This should never happen
        else {
            Utils.log(TAG, "onStartCommand() - Action: UNKNOWN");
        }
        return returnCode;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log(TAG, "onCreate()");
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        mStopServiceRunnable = new Runnable() {
            @Override
            public void run() {
                stopSelf();
                Utils.log(TAG, "Service stopped");
            }
        };


    }

    public void removeListener(AudioPlayer.Callback listener) {
        mAudioPlayer.removeListener(listener);
    }

    @Override
    public void onDestroy() {
        Utils.log(TAG, "onDestroy()");

        mAudioPlayer.removeListener(this);
        mAudioPlayer.deletePlayPauseStateObservers();

        // Stop playing track and destroy media player when service gets killed
        mAudioPlayer.stop();

        super.onDestroy();
    }

    //http://stackoverflow.com/questions/19568315/how-to-handle-code-when-app-is-killed-by-swiping-in-android#26882533
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Utils.log(TAG, "onTaskRemoved()");
        super.onTaskRemoved(rootIntent);

        // Remove notification: must remove service as foreground to delete the notification
        // http://stackoverflow.com/questions/15022990/how-to-cancel-notification-flag-foreground-service#15033994
        stopForeground(true);
        mNotificationManager.cancel(NOTIFICATION_ID_AUDIO_SERVICE);
        Utils.log(TAG, "onTaskRemoved() - Removed service from foreground state and deleted notification");

        // Stop service
        Utils.log(TAG, "onTaskRemoved() - Stopping service...");
        stopSelf();
    }
}