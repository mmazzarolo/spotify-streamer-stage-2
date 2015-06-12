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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.models.TrackItem;
import com.example.mazzdev.spotifystreamer.services.MusicService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayFragment extends Fragment implements MediaPlayerControl {


    private ArrayList<TrackItem> mTrackItemList;
    private int mPosition;
    private ArrayList<String> mTrackURLList;

    private MusicService mMusicService;
    private boolean mIsServiceBound =false;
    private MediaController mMediaController;
    private boolean paused=false, playbackPaused=false;
    private Intent playIntent;

    public static final String PLAY_TRACK_LIST_KEY = "PLAY_TRACK_LIST";
    public static final String PLAY_POSITION_KEY = "PLAY_POSITION";

    @InjectView(R.id.fragment_play_imageview) ImageView imageView;
    @InjectView(R.id.fragment_play_textview_artist) TextView textViewArtist;
    @InjectView(R.id.fragment_play_textview_album) TextView textViewAlbum;
    @InjectView(R.id.fragment_play_textview_track) TextView textViewTrack;
    @InjectView(R.id.fragment_play_button_next) Button buttonNext;
    @InjectView(R.id.fragment_play_button_prev) Button buttonPrev;
    @InjectView(R.id.fragment_play_button_play) Button buttonPlay;

    /**
     * Fragment lifecycle methods
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTrackURLList = new ArrayList<String>();

        Bundle args = getArguments();
        if (args != null) {
            mTrackItemList = args.getParcelableArrayList(PLAY_TRACK_LIST_KEY);
            for (TrackItem trackItem : mTrackItemList) {
                mTrackURLList.add(trackItem.getPreviewURL());
            }
            mPosition = args.getInt(PLAY_POSITION_KEY);
        }
        setController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_play, container, false);
        ButterKnife.inject(this, rootView);

        updateViewInfo(mPosition);

        return rootView;
    }

    public void updateViewInfo (int position) {
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
        if (playIntent == null) {
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
//        paused = true;
    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onPrepareReceiver,
                new IntentFilter("MEDIA_PLAYER_PREPARED"));
//        if(paused){
            setController();
//        mMediaController.show(0);
//        mMediaController.requestFocus();
//            paused=false;
//        }
    }

    @Override
    public void onStop() {
        mMediaController.hide();
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
            mMusicService.setTrackURLList(mTrackURLList);
            mMusicService.setTrackPosition(0);
            mMusicService.playTrack();
            mIsServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = false;
        }
    };

    // Broadcast receiver to determine when music player has been prepared
    private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            // When music player has been prepared, show controller
            mMediaController.show(0);
            mMediaController.requestFocus();
        }
    };

    public void trackPicked(int position){
        mMusicService.setTrackPosition(position);
        mMusicService.playTrack();
//        if (playbackPaused) {
//            setController();
//            playbackPaused = false;
//        }
//        mMediaController.show(0);
    }

    /**
     * Controller managements methods
     */
    private void setController(){
        if (mMediaController == null) mMediaController = new MediaController(getActivity());
//        mMediaController.setPrevNextListeners(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playNext();
//            }
//        }, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playPrev();
//            }
//        });
        mMediaController.setPrevNextListeners(v -> playNext(), v -> playPrev());
        mMediaController.setMediaPlayer(this);
        mMediaController.setAnchorView(getActivity().findViewById(R.id.fragment_play_linearlayout));
        mMediaController.setEnabled(true);
    }

    private void playNext(){
        mMusicService.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        mMediaController.show(0);
    }

    private void playPrev(){
        mMusicService.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        mMediaController.show(0);
    }

    /**
     * MediaController methods
     */
    @Override
    public void start() {
        mMusicService.start();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        mMusicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (mMusicService != null && mIsServiceBound && mMusicService.isPlaying()) {
            return mMusicService.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (mMusicService != null && mIsServiceBound && mMusicService.isPlaying()) {
            return mMusicService.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        mMusicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (mMusicService != null && mIsServiceBound) {
            return mMusicService.isPlaying();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}
