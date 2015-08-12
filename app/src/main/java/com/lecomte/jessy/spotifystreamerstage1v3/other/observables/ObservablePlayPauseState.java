package com.lecomte.jessy.spotifystreamerstage1v3.other.observables;

import java.util.Observable;

/**
 * Created by Jessy on 2015-08-11.
 */
// Keep play/pause state and keep listeners informed
public class ObservablePlayPauseState extends Observable
{
    private boolean mIsPlayState = false;

    public ObservablePlayPauseState()
    {
        mIsPlayState = false;
    }

    public ObservablePlayPauseState(boolean isPlayState)
    {
        mIsPlayState = isPlayState;
    }

    public void setIsPlayState(boolean isPlayState)
    {
        mIsPlayState = isPlayState;
        setChanged();
        notifyObservers();
    }

    public boolean isPlayState()
    {
        return mIsPlayState;
    }
}
