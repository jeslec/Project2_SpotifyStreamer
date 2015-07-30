package com.lecomte.jessy.spotifystreamerstage1v3.other;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.MainActivity;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.NowPlayingActivity;

import java.util.ArrayList;

/**
 * Created by Jessy on 2015-07-24.
 */
public class AudioPlayerService extends Service {

    private static final String TAG = "AudioPlayerService";

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

    private static final int NOTIFICATION_ID_AUDIO_PLAYER_SERVICE = 1000;

    private LocalBinder mLocalBinder = new LocalBinder();
    private AudioPlayer mAudioPlayer = new AudioPlayer();

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    public AudioPlayer getPlayer() {
        return mAudioPlayer;
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

    // Used for service-to-client communication
    // Get idea from book: Android Programming - Pushing the Limits, p.125-129
    public void setCallback(AudioPlayer.Callback callback) {
        if (mAudioPlayer != null) {
            mAudioPlayer.setCallback(callback);
        }
    }

    private Notification buildNotification() {
        // Get currently playing track info (or last played)
        TrackInfo track = mAudioPlayer.getTrackInfo();

        Intent notificationIntent = new Intent(this, NowPlayingActivity.class);
        //notificationIntent.setAction(ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Previous track button intent
        Intent previousIntent = new Intent(this, AudioPlayerService.class);
        previousIntent.setAction(AudioPlayerService.ACTION_PLAY_PREVIOUS_TRACK);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        // Pause track button intent
        Intent pauseIntent = new Intent(this, AudioPlayerService.class);
        pauseIntent.setAction(AudioPlayerService.ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        // Next track button intent
        Intent nextIntent = new Intent(this, AudioPlayerService.class);
        nextIntent.setAction(AudioPlayerService.ACTION_PLAY_NEXT_TRACK);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        // Use high priority so notification appears at top of notifications list and that
        // the control buttons are displayed by default (instead of having to expend notif.)
        // http://stackoverflow.com/questions/18249871/android-notification-buttons-not-showing-up
        return new NotificationCompat.Builder(this)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_audio_player)
                        .setContentTitle(track.getTrackName())
                        .setContentText(track.getArtistName())
                        .setPriority(Notification.PRIORITY_MAX)
                        .addAction(android.R.drawable.ic_media_previous, getResources()
                                        .getString(R.string.notification_action_play_prev),
                                prevPendingIntent)
                        .addAction(android.R.drawable.ic_media_pause, getResources()
                                        .getString(R.string.notification_action_pause),
                                pausePendingIntent)
                        .addAction(android.R.drawable.ic_media_next, getResources()
                                        .getString(R.string.notification_action_play_next),
                                nextPendingIntent).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.log(TAG, "onStartCommand()");
        int returnCode = START_STICKY;

        if (intent == null || intent.getAction() == null) {
            return returnCode;
        }

        String action = intent.getAction();

        if (action.equals(ACTION_START_FOREGROUND)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_START_FOREGROUND");
            startForeground(NOTIFICATION_ID_AUDIO_PLAYER_SERVICE, buildNotification());
        }

        else if (action.equals(ACTION_STOP_FOREGROUND)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_STOP_FOREGROUND");
            stopForeground(true);
        }

        else if (action.equals(ACTION_PLAY_PREVIOUS_TRACK)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_PLAY_PREVIOUS_TRACK");
            mAudioPlayer.playPrevious();
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID_AUDIO_PLAYER_SERVICE, buildNotification());
        }

        else if (action.equals(ACTION_PAUSE)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_PAUSE");
            mAudioPlayer.pause();
            // TODO: change icon to play
        }

        else if (action.equals(ACTION_PLAY_NEXT_TRACK)) {
            Utils.log(TAG, "onStartCommand() - Action: ACTION_PLAY_NEXT_TRACK");
            mAudioPlayer.playNext();
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID_AUDIO_PLAYER_SERVICE, buildNotification());
        }
        return returnCode;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log(TAG, "onCreate()");
    }

    @Override
    public void onDestroy() {
        Utils.log(TAG, "onDestroy() - Calling AudioPlayer.stop()...");

        // TEST: stop playing track and destroy media player when service gets killed
        mAudioPlayer.stop();

        super.onDestroy();
    }
}