package com.lecomte.jessy.spotifystreamerstage1v3.other;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;

/**
 * Created by Jessy on 2015-07-24.
 */
public class AudioPlayerService extends Service implements AudioPlayer.PlayerFragmentCommunication {

    private static final String TAG = "AudioPlayerService";

    // Actions this service can perform for its client
    public static final String ACTION_PLAY =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.PLAY";
    public static final String ACTION_STOP =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.STOP";
    public static final String ACTION_RESUME =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.RESUME";
    public static final String ACTION_PAUSE =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.action.PAUSE";

    // Responses this service will send to the client
    public static final String RESPONSE_TRACK_PREPARED =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.response.TRACK_PREPARED";

    // Data this service will send to the client in his response
    public static final String EXTRA_TRACK_DURATION =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.extra.TRACK_DURATION";

    //private AudioPlayer mAudioPlayer = new AudioPlayer(this);
    private LocalBinder mLocalBinder = new LocalBinder();
    private AudioPlayer mAudioPlayer = new AudioPlayer(this);

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    public AudioPlayer getPlayer() {
        return mAudioPlayer;
    }

    @Override
    public void onReceiveTrackDuration(int duration) {
        // Send the track duration, specified in milliseconds, to the client
        // http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager#8875292
        Intent intent = new Intent(RESPONSE_TRACK_PREPARED);
        intent.putExtra(EXTRA_TRACK_DURATION, duration);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onTrackCompleted() {

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

    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(ACTION_PLAY)) {
            /*mMediaPlayer = ... // initialize it here
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread*/
        }

        return START_STICKY;
    }

    public void printDummyLineToLogcat() {
        Utils.log(TAG, "~~~~~~~~~~~~~~~ THIS WAS PRINTED FROM AUDIO PLAYER SERVICE !!!!!!!!!!!");
    }
}
