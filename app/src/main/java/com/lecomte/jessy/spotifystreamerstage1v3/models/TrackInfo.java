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
    private String mAlbumImageUrl;
    private Integer mTrackPopularity;
    private String mTrackPreviewUrl;
    private long mTrackDuration; // in milliseconds

    public TrackInfo(String trackName, String albumName, String albumImageUrl,
                     Integer trackPopularity, String id, String trackPreviewUrl,
                     long trackDuration) {
        mId = id;
        mTrackName = trackName;
        mAlbumName = albumName;
        mAlbumImageUrl = albumImageUrl;
        mTrackPopularity = trackPopularity;
        mTrackPreviewUrl = trackPreviewUrl;
        mTrackDuration = trackDuration;
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

    public String getAlbumImageUrl() {
        return mAlbumImageUrl;
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

        return String.format("%2d", getTrackPopularity()) + "% " +
                String.format("%1$-" + maxCharsTrack + "s", trackName) + " " +
                String.format("%1$-" + maxCharsAlbum + "s", albumName) + " " +
                getAlbumImageUrl() + " " + mTrackPreviewUrl + " " + mTrackDuration;
    }
}
