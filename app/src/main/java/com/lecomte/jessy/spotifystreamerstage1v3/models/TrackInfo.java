package com.lecomte.jessy.spotifystreamerstage1v3.models;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;

/**
 * Created by Jessy on 2015-06-30.
 */
public class TrackInfo {
    private String mId;
    private String mTrackName;
    private String mAlbumName;
    private String mAlbumSmallImageUrl; // displayed in listView item
    private String mAlbumBigImageUrl; // displayed in NowPlaying dialog
    private Integer mTrackPopularity;
    private String mTrackPreviewUrl;
    private long mTrackDuration; // in milliseconds

    public TrackInfo(String trackName, String albumName, String albumSmallImageUrl,
                     String albumBigImageUrl, Integer trackPopularity, String id,
                     String trackPreviewUrl, long trackDuration) {
        mId = id;
        mTrackName = trackName;
        mAlbumName = albumName;
        mAlbumSmallImageUrl = albumSmallImageUrl;
        mTrackPopularity = trackPopularity;
        mTrackPreviewUrl = trackPreviewUrl;
        mTrackDuration = trackDuration;
        mAlbumBigImageUrl = albumBigImageUrl;
    }

    public String getId() {
        return mId;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public String getAlbumSmallImageUrl() {
        return mAlbumSmallImageUrl;
    }

    public Integer getTrackPopularity() {
        return mTrackPopularity;
    }

    public String getTrackPreviewUrl() {
        return mTrackPreviewUrl;
    }

    public long getTrackDuration() {
        return mTrackDuration;
    }

    public String getAlbumBigImageUrl() {
        return mAlbumBigImageUrl;
    }

    @Override
    public String toString() {
        String trackName = getTrackName();
        String albumName = getAlbumName();

        // From debug.xml, max characters for the track name & album name columns in Logcat
        final int maxCharsTrack = App.getRes().getInteger(R.integer.track_name_column_width);
        final int maxCharsAlbum = App.getRes().getInteger(R.integer.album_name_column_width);

        // Limit string length, truncate and append "..." if size exceeds max length
        if (getTrackName().length() > maxCharsTrack) {
            trackName = trackName.substring(0, maxCharsTrack-3) + "...";
        }

        // Limit string length, truncate and append "..." if size exceeds max length
        if (getAlbumName().length() > maxCharsAlbum) {
            albumName = albumName.substring(0, maxCharsAlbum-3) + "...";
        }

        // Convert track length from milliseconds to minutes:seconds
        long seconds = mTrackDuration / 1000;
        long minutes = seconds / 60;
        long secondsLeft = seconds - (minutes * 60);

        return String.format("%2d", getTrackPopularity()) + "% " +
                String.format("%1$-" + maxCharsTrack + "s", trackName) + " " +
                String.format("%1$-" + maxCharsAlbum + "s", albumName) + " " +
                getAlbumSmallImageUrl() + " " + getAlbumBigImageUrl() + " " +
                mTrackPreviewUrl + " " +
                String.format("%2d:%2d", minutes, secondsLeft);
    }
}
