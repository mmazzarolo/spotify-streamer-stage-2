package com.example.mazzdev.spotifystreamer.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.models.TrackItem;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Matteo on 06/06/2015.
 */
public class TrackListAdapter extends ArrayAdapter<TrackItem> {

    Context context;
    int layoutResourceId;
    List <TrackItem> objects;

    public TrackListAdapter(Context context, int layoutResourceId, List<TrackItem> objects) {
        super(context, layoutResourceId, objects);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.objects = objects;
    }

    static class ViewHolder {

        @InjectView(R.id.list_item_track_textview_track) TextView textViewTrack;
        @InjectView(R.id.list_item_track_textview_album) TextView textViewAlbum;
        @InjectView(R.id.list_item_track_imageview) ImageView imageView;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            // Inflating the layout
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            // Setting viewholder
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            // Getting back info from the viewHolder
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TrackItem trackItem = objects.get(position);

        if (trackItem != null) {
            viewHolder.textViewTrack.setText(trackItem.getTrackName());
            viewHolder.textViewAlbum.setText(trackItem.getAlbumName());
            // Don't load the image again if it was already fetched
            if (viewHolder.imageView != null && trackItem.hasSmallThumbnail()) {
                Picasso.with(getContext())
                        .load(trackItem.getThumbnailSmallURL())
                        .resize(100, 100)
                        .centerCrop()
                        .into(viewHolder.imageView);
            }
        }

        return convertView;
    }

}