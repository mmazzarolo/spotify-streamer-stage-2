package com.example.mazzdev.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Matteo on 06/06/2015.
 */
public class TrackItem implements Parcelable {

    private String trackName;
    private String albumName;
    private String artistName;
    private String spotifyId;
    private String thumbnailSmallURL;
    private String thumbnailLargeURL;
    private String previewURL;
    private String externalSpotifyURL;

    public TrackItem(Track track) {
        this.trackName = track.name;
        this.albumName = track.album.name;
        this.artistName = track.artists.get(0).name;
        this.spotifyId = track.id;
        this.thumbnailSmallURL = null;
        this.thumbnailLargeURL = null;
        this.previewURL = track.preview_url;
        if (!track.album.images.isEmpty()) {
            thumbnailSmallURL = searchImage(track.album.images, 200);
            thumbnailLargeURL = searchImage(track.album.images, 640);
        }
        if (track.external_urls != null) {
            this.externalSpotifyURL = track.external_urls.get("spotify");
        }

    }

    /*
    * Assuming that (from the app mocks):
    * Album art thumbnail (large (640px for Now Playing screen) and small (200px for list items)).
    * If the image size does not exist in the API response,
    * you are free to choose whatever size is available.)
    *
    * And assuming that the images are sorted (from the Spotify API WEB help):
    * images (array of image objects )-> The cover art for the album in various sizes, widest first.
    *
    * This method returns the URL of the image with the width closest to @width.
    */
    protected String searchImage(List<Image> images, int width) {
        String imgURL = images.get(0).url;
        for (Image image : images) {
            if (image.width >= width) {
                imgURL = image.url;
            } else {
                return imgURL;
            }
        }
        return imgURL;
    }

    private TrackItem(Parcel in) {
        this.trackName = in.readString();
        this.albumName = in.readString();
        this.artistName = in.readString();
        this.spotifyId = in.readString();
        this.thumbnailSmallURL = in.readString();
        this.thumbnailLargeURL = in.readString();
        this.previewURL = in.readString();
        this.externalSpotifyURL = in.readString();
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public String getThumbnailSmallURL() {
        return thumbnailSmallURL;
    }

    public void setThumbnailSmallURL(String thumbnailSmallURL) {
        this.thumbnailSmallURL = thumbnailSmallURL;
    }

    public String getThumbnailLargeURL() {
        return thumbnailLargeURL;
    }

    public void setThumbnailLargeURL(String thumbnailLargeURL) {
        this.thumbnailLargeURL = thumbnailLargeURL;
    }

    public String getPreviewURL() {
        return previewURL;
    }

    public void setPreviewURL(String previewURL) {
        this.previewURL = previewURL;
    }

    public String getExternalSpotifyURL() {
        return externalSpotifyURL;
    }

    public void setExternalSpotifyURL(String externalSpotifyURL) {
        this.externalSpotifyURL = externalSpotifyURL;
    }

    public boolean hasSmallThumbnail() {
        return thumbnailSmallURL != null;
    }

    public boolean hasLargeThumbnail() {
        return thumbnailLargeURL != null;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(trackName);
        out.writeString(albumName);
        out.writeString(artistName);
        out.writeString(spotifyId);
        out.writeString(thumbnailSmallURL);
        out.writeString(thumbnailLargeURL);
        out.writeString(previewURL);
        out.writeString(externalSpotifyURL);
    }

    public static final Parcelable.Creator<TrackItem> CREATOR = new Parcelable.Creator<TrackItem>() {
        public TrackItem createFromParcel(Parcel in) {
            return new TrackItem(in);
        }

        public TrackItem[] newArray(int size) {
            return new TrackItem[size];
        }
    };

    public int describeContents() {
        return 0;
    }
}
