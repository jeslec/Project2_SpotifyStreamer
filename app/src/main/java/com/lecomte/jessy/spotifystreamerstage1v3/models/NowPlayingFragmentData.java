package com.lecomte.jessy.spotifystreamerstage1v3.models;

import java.util.ArrayList;

/**
 * Created by Jessy on 2015-08-13.
 */
public class NowPlayingFragmentData {

    private ArrayList<TrackInfo> mTrackList = new ArrayList<TrackInfo>();
    private int mTrackIndex = 0;

    public NowPlayingFragmentData() {
    }

    public ArrayList<TrackInfo> getTrackList() {
        return mTrackList;
    }

    public void setTrackList(ArrayList<TrackInfo> trackList) {
        mTrackList = trackList;
    }

    public int getTrackIndex() {
        return mTrackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        mTrackIndex = trackIndex;
    }
}
