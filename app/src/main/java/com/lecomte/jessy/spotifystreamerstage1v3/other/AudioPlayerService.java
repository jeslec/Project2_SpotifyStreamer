package com.lecomte.jessy.spotifystreamerstage1v3.other;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;

/**
 * Created by Jessy on 2015-07-24.
 */
public class AudioPlayerService extends Service implements MediaPlayer.OnPreparedListener {

    // Actions this service can perform for its client
    private static final String ACTION_PLAY =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.actions.PLAY";
    private static final String ACTION_STOP =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.actions.STOP";
    private static final String ACTION_RESUME =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.actions.RESUME";
    private static final String ACTION_PAUSE =
            "com.lecomte.jessy.spotifystreamerstage1v3.audioPlayerService.actions.PAUSE";

    MediaPlayer mMediaPlayer = null;
    private LocalBinder mLocalBinder = new LocalBinder();
    private AudioPlayer mPlayer = null;

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    /** method for clients */
    public int getRandomNumber() {
        return 1; //mGenerator.nextInt(100);
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

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
    }
}
