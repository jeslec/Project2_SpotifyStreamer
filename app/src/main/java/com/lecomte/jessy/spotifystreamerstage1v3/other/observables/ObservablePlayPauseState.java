package com.lecomte.jessy.spotifystreamerstage1v3.other.observables;

import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.AudioPlayer;
import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;

import java.util.Observable;

/**
 * Created by Jessy on 2015-08-11.
 */
// Keep play/pause state and keep listeners informed
public class ObservablePlayPauseState extends Observable
{
    private static final String TAG = "ObservablePlayPauseState";
    private AudioPlayer.TrackPlayingState mState;

    public ObservablePlayPauseState()
    {
        mState = AudioPlayer.TrackPlayingState.TRACK_NOT_PLAYING;
    }

    public ObservablePlayPauseState(AudioPlayer.TrackPlayingState state)
    {
        mState = state;
    }

    public void setState(AudioPlayer.TrackPlayingState state)
    {
        Utils.log(TAG, "State set to: " + state.toString());
        mState = state;
        setChanged();
        notifyObservers();
    }

    public boolean isTrackPlaying()
    {
        return mState == AudioPlayer.TrackPlayingState.TRACK_PLAYING;
    }
}
