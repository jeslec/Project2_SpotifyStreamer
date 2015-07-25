package com.lecomte.jessy.spotifystreamerstage1v3.other;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;

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

    public void setCallback(AudioPlayer.Callback callback) {
        if (mAudioPlayer != null) {
            mAudioPlayer.setCallback(callback);
        }
    }
}
