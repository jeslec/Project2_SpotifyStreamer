package com.lecomte.jessy.spotifystreamerstage1v3.models;


import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;


/**
 * Created by Jessy on 2015-06-28.
 */
public class ArtistInfo {

    private String mName;
    private String mId;
    private Integer mPopularity;
    private String mImageUrl;

    public ArtistInfo(String id, String name, Integer popularity, String imageUrl) {
        mName = name;
        mId = id;
        mPopularity = popularity;
        mImageUrl = imageUrl;
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }

    public Integer getPopularity() {
        return mPopularity;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    @Override
    public String toString() {
        String name = getName();
        // Get max characters allowed for string from debug.xml
        final int maxChars = App.getRes().getInteger(R.integer.artist_name_column_width);

        // Limit string length, truncate and append "..." if size exceeds max length
        if (getName().length() > maxChars) {
            name = getName().substring(0, maxChars-3) + "...";
        }

        return String.format("%2d", getPopularity()) + "% " +
                String.format("%1$-" + maxChars + "s", name) + " " +
                getId() + " " +
                getImageUrl();
    }
}