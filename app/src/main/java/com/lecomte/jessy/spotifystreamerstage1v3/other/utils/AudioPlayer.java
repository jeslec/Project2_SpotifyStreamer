package com.lecomte.jessy.spotifystreamerstage1v3.other.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.views.fragments.NowPlayingFragment;

import java.io.IOException;

/**
 * Created by Jessy on 2015-07-20.
 */
public class AudioPlayer {
    private final String TAG = getClass().getSimpleName();
    private MediaPlayer mPlayer;
    private PlayerFragmentCommunication mListener;
    private int mTrackDuration;

    public AudioPlayer(PlayerFragmentCommunication fragmentClass) {
        mListener = (PlayerFragmentCommunication) fragmentClass;
        initializePlayer();
    }

    private void initializePlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mp) {
                                                mListener.onTrackCompleted();
                                            }
                                        }
        );

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mTrackDuration = mp.getDuration();
                mListener.onReceiveTrackDuration(mTrackDuration);
                Utils.log(TAG, "onPrepared() - Track duration: " + mTrackDuration);
                mp.start();
            }
        });
    }

    public int getCurrentPosition() {
        //Utils.log(TAG, "Track position: " + mPlayer.getCurrentPosition());
        return mPlayer.getCurrentPosition();
    }

    public void play(String audioFileUrl) {

        stop();
        initializePlayer();

        try {
            mPlayer.setDataSource(audioFileUrl);
            mPlayer.prepareAsync(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            Utils.log(TAG, R.string.AudioPlayer_debug_playerStopped);
        }
    }

    // This should only be called when player is in "Paused" state
    public void resume() {
        if (mPlayer != null) {

            // TEST: added 2015/07/23 16h41
            mListener.onReceiveTrackDuration(mTrackDuration);

            mPlayer.start();
        }
    }

    // After this, player will be in "Paused" state
    // Only valid actions after are: start() or stop()
    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        Utils.log(TAG, "isPlaying() - mPlayer is null!");
        return false;
    }

    public void seekTo(int progress) {
        if (mPlayer != null) {
            mPlayer.seekTo(progress);
        }
    }

    public interface PlayerFragmentCommunication {
        public void onReceiveTrackDuration(int duration);
        public void onTrackCompleted();
    }
}
