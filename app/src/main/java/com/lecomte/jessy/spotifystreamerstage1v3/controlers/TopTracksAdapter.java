package com.lecomte.jessy.spotifystreamerstage1v3.controlers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.TrackInfo;
import com.squareup.picasso.Picasso;

public class TopTracksAdapter extends ArrayAdapter<TrackInfo> {

    private Context mContext;

    public TopTracksAdapter(final Context context) {
        super(context, -1);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // If a view was not created already, create it.
        // Once created reuse it at each call to getView()
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_top_tracks_item, null);
        }
        // Get the data from the selected list item
        TrackInfo trackInfo = getItem(position);

        // Display the data in the widgets
        TextView trackNameTextView =
                (TextView)convertView.findViewById(R.id.TopTracks_trackName);
        TextView albumNameTextView =
                (TextView)convertView.findViewById(R.id.TopTracks_albumName);
        ImageView albumImageView =
                (ImageView)convertView.findViewById(R.id.TopTracks_albumImage);

        trackNameTextView.setText(trackInfo.getTrackName());
        albumNameTextView.setText(trackInfo.getAlbumName());

        if (trackInfo.getAlbumImageUrl().isEmpty()) {
            albumImageView.setImageResource(R.drawable.noimage);
        }

        else {
            Picasso.with(mContext).load(trackInfo.getAlbumImageUrl()).into(albumImageView);
        }

        return convertView;
    }
}
