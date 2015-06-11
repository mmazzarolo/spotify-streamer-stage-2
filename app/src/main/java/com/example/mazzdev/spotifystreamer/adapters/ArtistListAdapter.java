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
import com.example.mazzdev.spotifystreamer.models.ArtistItem;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Matteo on 06/06/2015.
 */
public class ArtistListAdapter extends ArrayAdapter<ArtistItem> {

    private Context context;
    private int layoutResourceId;
    private List <ArtistItem> objects;

    public ArtistListAdapter(Context context, int layoutResourceId, List <ArtistItem> objects) {
        super(context, layoutResourceId, objects);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.objects = objects;
    }

    static class ViewHolder {

        @InjectView(R.id.list_item_artist_textview) TextView textView;
        @InjectView(R.id.list_item_artist_imageview) ImageView imageView;

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

        ArtistItem artistItem = objects.get(position);

        if (artistItem != null) {
            viewHolder.textView.setText(artistItem.getName());
            // Don't load the image again if it was already fetched
            if (viewHolder.imageView != null && artistItem.hasThumbnail()) {
                Picasso.with(getContext())
                        .load(artistItem.getThumbnailURL())
                        .resize(100, 100)
                        .centerCrop()
                        .into(viewHolder.imageView);
            }
        }

        return convertView;
    }

}