package com.example.mazzdev.spotifystreamer.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.models.TrackItem;
import com.example.mazzdev.spotifystreamer.services.MusicService;
import com.example.mazzdev.spotifystreamer.views.MediaControllerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayFragment extends Fragment {


    private ArrayList<TrackItem> mTrackItemList;
    private int mPosition;

    private MusicService mMusicService;
    private MediaControllerView mMediaControllerView;
    private Intent playIntent;

    public static final String PLAY_TRACK_LIST_KEY = "PLAY_TRACK_LIST";
    public static final String PLAY_POSITION_KEY = "PLAY_POSITION";

    @InjectView(R.id.fragment_play_imageview) ImageView imageView;
    @InjectView(R.id.fragment_play_textview_artist) TextView textViewArtist;
    @InjectView(R.id.fragment_play_textview_album) TextView textViewAlbum;
    @InjectView(R.id.fragment_play_textview_track) TextView textViewTrack;
    @InjectView(R.id.fragment_play_linearlayout) LinearLayout linearLayout;
    /**
     * Fragment lifecycle methods
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mTrackItemList = args.getParcelableArrayList(PLAY_TRACK_LIST_KEY);
            mPosition = args.getInt(PLAY_POSITION_KEY);
        }
        setController();

        if (playIntent == null) {
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_play, container, false);
        ButterKnife.inject(this, rootView);

        updateViewInfo(mPosition);

        return rootView;
    }

    public void updateViewInfo(int position) {
        textViewArtist.setText(mTrackItemList.get(position).getArtistName());
        textViewAlbum.setText(mTrackItemList.get(position).getAlbumName());
        textViewTrack.setText(mTrackItemList.get(position).getTrackName());
        if (imageView != null && mTrackItemList.get(position).hasLargeThumbnail()) {
            Picasso.with(getActivity())
                    .load(mTrackItemList.get(position).getThumbnailLargeURL())
                    .into(imageView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause(){
        super.onPause();
        getActivity().unbindService(mServiceConnection);
        try {
            getActivity().unregisterReceiver(onPrepareReceiver);
            onPrepareReceiver = null;
        } catch (Exception e) {
            Log.e("AAAAAAAAAAAAA", e.getMessage());
        }
        mMediaControllerView.hide();
    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onPrepareReceiver,
                new IntentFilter("MEDIA_PLAYER_PREPARED"));
            setController();
    }

    @Override
    public void onStop() {
        mMediaControllerView.hide();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(playIntent);
        mMusicService=null;
        super.onDestroy();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            mMusicService = binder.getService();
            mMediaControllerView.setIsServiceBound(true);
            mMediaControllerView.setMusicService(mMusicService);
            startPlaying();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMediaControllerView.setIsServiceBound(false);
        }
    };

    private void startPlaying() {
        mMusicService.setTrackItemList(mTrackItemList);
        mMusicService.setTrackPosition(mPosition);
        mMusicService.playTrack();
    }

    // Broadcast receiver to determine when music player has been prepared
    private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            if (i.getAction().equals("MEDIA_PLAYER_PREPARED")) {
                Log.v("PlayFragment", "MEDIA_PLAYER_PREPARED");
                mPosition = mMusicService.getTrackPosition();
                setController();
                updateViewInfo(mPosition);
                mMediaControllerView.show(0);
                mMediaControllerView.requestFocus();
            }
        }
    };

    /**
     * Controller managements methods
     */
    private void setController(){
        if (mMediaControllerView == null) {
            mMediaControllerView = new MediaControllerView(getActivity(), false);
        }
        mMediaControllerView.setMusicService(mMusicService);
        mMediaControllerView.setAnchorView(linearLayout);
        mMediaControllerView.setEnabled(true);
    }
}
