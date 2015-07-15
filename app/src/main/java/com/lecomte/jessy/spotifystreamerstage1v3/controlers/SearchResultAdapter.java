package com.lecomte.jessy.spotifystreamerstage1v3.controlers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lecomte.jessy.spotifystreamerstage1v3.App;
import com.lecomte.jessy.spotifystreamerstage1v3.R;
import com.lecomte.jessy.spotifystreamerstage1v3.models.ArtistInfo;
import com.squareup.picasso.Picasso;

/**
 * Created by Jessy on 2015-07-08.
 */
public class SearchResultAdapter extends ArrayAdapter<ArtistInfo> {

    private Context mContext;

    public SearchResultAdapter(final Context context) {
        super(context, -1);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // If a view was not created already, create it.
        // Once created reuse it at each call to getView()
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_search_result_item, null);
        }
        // Get the data from the selected list item
        ArtistInfo artistInfo = getItem(position);

        // Artist name
        TextView artistTextView = (TextView)convertView
                .findViewById(R.id.SearchResultFragment_ListViewItem_ArtistName);

        artistTextView.setText(artistInfo.getName());

        // Artist popularity
        TextView popularityTextView = (TextView)convertView
                .findViewById(R.id.SearchResultFragment_ListViewItem_ArtistPopularity);

        popularityTextView.setText(App.getRes().getString(R.string.artist_popularity,
                artistInfo.getPopularity()));

        // Artist image
        ImageView artistImageView = (ImageView)convertView
                .findViewById(R.id.SearchResultFragment_ListViewItem_ArtistImage);

        if (artistInfo.getImageUrl().isEmpty()) {
            artistImageView.setImageResource(R.drawable.noimage);
        }

        else {
            Picasso.with(mContext).load(artistInfo.getImageUrl()).into(artistImageView);
        }

        return convertView;
    }
}
