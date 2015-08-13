package com.lecomte.jessy.spotifystreamerstage1v3.models;

/**
 * Created by Jessy on 2015-08-13.
 */
public class AudioPlayerStates {

    private boolean mIsPlaying;

    public AudioPlayerStates(boolean isPlaying) {
        mIsPlaying = isPlaying;
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }
}
