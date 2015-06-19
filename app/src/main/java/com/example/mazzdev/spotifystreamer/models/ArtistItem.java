package com.example.mazzdev.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Matteo on 06/06/2015.
 */
public class ArtistItem implements Parcelable {

    private String name;
    private String spotifyId;
    private String thumbnailURL;

    public ArtistItem(Artist artist) {
        this.name = artist.name;
        this.spotifyId = artist.id;
        this.thumbnailURL = null;
        if (!artist.images.isEmpty()) {
            thumbnailURL = artist.images.get(0).url;
        }
    }

    private ArtistItem(Parcel in) {
        this.name = in.readString();
        this.spotifyId = in.readString();
        this.thumbnailURL = in.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public boolean hasThumbnail() {
        return thumbnailURL != null;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(spotifyId);
        out.writeString(thumbnailURL);
    }

    public static final Parcelable.Creator<ArtistItem> CREATOR = new Parcelable.Creator<ArtistItem>() {
        public ArtistItem createFromParcel(Parcel in) {
            return new ArtistItem(in);
        }

        public ArtistItem[] newArray(int size) {
            return new ArtistItem[size];
        }
    };

    public int describeContents() {
        return 0;
    }
}
