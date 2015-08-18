package com.lecomte.jessy.spotifystreamerstage1v3.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.lecomte.jessy.spotifystreamerstage1v3.other.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Jessy on 2015-08-13.
 */
public class NowPlayingFragmentData implements Parcelable {

    private static final String TAG = "NowPlayingFragmentData";
    private ArrayList<TrackInfo> mTrackList = new ArrayList<TrackInfo>();
    private int mTrackIndex = 0;

    public NowPlayingFragmentData() {
    }

    @Override
    public String toString() {
        return String.format("[TrackCount: %s] [TrackIndex: %s]", mTrackList.size(), mTrackIndex);
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

    protected NowPlayingFragmentData(Parcel in) {
        if (in.readByte() == 0x01) {
            mTrackList = new ArrayList<TrackInfo>();
            in.readList(mTrackList, TrackInfo.class.getClassLoader());
        } else {
            mTrackList = null;
        }
        mTrackIndex = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mTrackList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mTrackList);
        }
        dest.writeInt(mTrackIndex);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NowPlayingFragmentData> CREATOR = new Parcelable.Creator<NowPlayingFragmentData>() {
        @Override
        public NowPlayingFragmentData createFromParcel(Parcel in) {
            return new NowPlayingFragmentData(in);
        }

        @Override
        public NowPlayingFragmentData[] newArray(int size) {
            return new NowPlayingFragmentData[size];
        }
    };
}
