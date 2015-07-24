package com.lecomte.jessy.spotifystreamerstage1v3.other.utils;

/**
 * Created by Jessy on 2015-07-23.
 */
public class SafeIndex {

    private int mMaxIndex;
    private int mIndex;

    public SafeIndex(int initialIndex, int maxIndex) {
        mIndex = initialIndex;
        mMaxIndex = maxIndex;
    }

    public int get() {
        return mIndex;
    }

    public int getNext() {
        return (mIndex = mIndex+1 > mMaxIndex? 0 : mIndex+1);
    }

    public int getPrevious() {
        return (mIndex = mIndex-1 < 0? mMaxIndex : mIndex-1);
    }
}
