package com.lecomte.jessy.spotifystreamerstage1v3.other;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;
import com.lecomte.jessy.spotifystreamerstage1v3.views.activities.MainActivity;

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
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.STOP_FOREGROUND";;

    private static final int NOTIFICATION_ID_FOREGROUND_SERVICE = 1000;



    //private AudioPlayer mAudioPlayer = new AudioPlayer(this);
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

    // Service-to-client communication
    // Get idea from book: Android Programming - Pushing the Limits, p.125-129
    public void setCallback(AudioPlayer.Callback callback) {
        if (mAudioPlayer != null) {
            mAudioPlayer.setCallback(callback);
        }
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

            // TEST: Get currently playing track info (or last played)
            //mAudioPlayer.getTrackInfo();

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(android.R.drawable.ic_menu_agenda)
                            .setContentTitle("My notification")
                            .setContentText("Hello World!");

            startForeground(NOTIFICATION_ID_FOREGROUND_SERVICE, mBuilder.build());
        }

        else if (action.equals(ACTION_STOP_FOREGROUND)) {
            stopForeground(true);
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

 /*// Notification (if you click on it) intent
            Intent notificationIntent = new Intent(this, MainActivity.class);
            //notificationIntent.setAction(ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);*/

            /*// Previous track button intent
            Intent previousIntent = new Intent(this, ForegroundService.class);
            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
            PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

            // Play track button intent
            Intent playIntent = new Intent(this, ForegroundService.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

            // Next track button intent
            Intent nextIntent = new Intent(this, ForegroundService.class);
            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
            PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.truiton_short);*/

           /* Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Track Name")
                    .setTicker("Track Name") // Jessy: don't know what that is
                    .setContentText("Album Name")
                    //.setSmallIcon(R.drawable.ic_launcher)
                    //.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    *//*.addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
                    .addAction(android.R.drawable.ic_media_play, "Play", playIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent)*//*
                    .build();*/

