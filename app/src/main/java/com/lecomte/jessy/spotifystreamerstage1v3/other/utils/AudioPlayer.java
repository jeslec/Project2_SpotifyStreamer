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

    public AudioPlayer(NowPlayingFragment fragmentClass) {
        mListener = (PlayerFragmentCommunication) fragmentClass;
        initializePlayer();
    }

   /* @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + TAG + ".OnFragmentInteractionListener");
        }
    }*/

    private void initializePlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mp) {
                                                mListener.onTrackCompleted();
                                                stop();
                                            }
                                        }
        );

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int duration = mp.getDuration();
                mListener.onReceiveTrackDuration(duration);
                Log.d(TAG, "onPrepared() - Track duration: " + duration);
                mp.start();
            }
        });
    }

    public int getCurrentPosition() {
        //Log.d(TAG, "Track position: " + mPlayer.getCurrentPosition());
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

    public interface PlayerFragmentCommunication {
        public void onReceiveTrackDuration(int duration);
        public void onTrackCompleted();
    }
}
