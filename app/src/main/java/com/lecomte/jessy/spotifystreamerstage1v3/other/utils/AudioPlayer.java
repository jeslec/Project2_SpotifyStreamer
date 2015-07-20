package com.lecomte.jessy.spotifystreamerstage1v3.other.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by Jessy on 2015-07-20.
 */
public class AudioPlayer {
    private MediaPlayer mPlayer;

    public AudioPlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void play(String audioFileUrl) {

        try {
            mPlayer.setDataSource(audioFileUrl);
            mPlayer.prepareAsync(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
