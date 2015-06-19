package com.example.mazzdev.spotifystreamer.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.models.TrackItem;
import com.example.mazzdev.spotifystreamer.services.MusicService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * PlayFragment
 * Shows and manages the playback of the tracks (by binding to MusicService).
 */
public class PlayFragment extends DialogFragment {

    private ArrayList<TrackItem> mTrackItemList;
    private int mPosition;
    private MusicService mMusicService;
    private Handler mSeekbarHandler = null;
    private boolean mIsServiceBound = false;
    private boolean mHasSavedInstance = false;

    public static final String PLAY_TRACK_LIST_KEY = "PLAY_TRACK_LIST";
    public static final String PLAY_POSITION_KEY = "PLAY_POSITION";
    public static final String PLAY_SAVE_STATE_KEY = "PLAY_SAVE_STATE";

    @InjectView(R.id.imageview) ImageView imageView;
    @InjectView(R.id.textview_artist) TextView textViewArtist;
    @InjectView(R.id.textview_album) TextView textViewAlbum;
    @InjectView(R.id.textview_track) TextView textViewTrack;
    @InjectView(R.id.textview_time_current) TextView textViewTimeCurrent;
    @InjectView(R.id.textview_time_max) TextView textViewTimeMax;
    @InjectView(R.id.button_play) Button buttonPlay;
    @InjectView(R.id.progressbar) ProgressBar progressBar;
    @InjectView(R.id.seekbar) SeekBar seekBar;

    /**
     * Fragment lifecycle methods
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Checking if the device has been rotated
        if (savedInstanceState != null && savedInstanceState.containsKey(PLAY_SAVE_STATE_KEY)) {
            mHasSavedInstance = true;
        }
        // Restoring the view info on creation
        Bundle args = getArguments();
        if (args != null) {
            mTrackItemList = args.getParcelableArrayList(PLAY_TRACK_LIST_KEY);
            mPosition = args.getInt(PLAY_POSITION_KEY);
        }
    }

    // Removing the fragmentDialog title
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating the layout and initializing ButterKnife
        View rootView = inflater.inflate(R.layout.fragment_play, container, false);
        ButterKnife.inject(this, rootView);
        updateView(mPosition);
        return rootView;
    }

    @OnClick(R.id.button_next)
    public void buttonNextClicked() {
            sendActionToService(MusicService.INTENT_ACTION_NEXT);
    }

    @OnClick(R.id.button_prev)
    public void buttonPrevClicked() {
            sendActionToService(MusicService.INTENT_ACTION_PREV);
    }

    @OnClick(R.id.button_play)
    public void buttonPlayClicked() {
        if (mMusicService.isPlaying()) {
            sendActionToService(MusicService.INTENT_ACTION_PAUSE);
        } else {
            sendActionToService(MusicService.INTENT_ACTION_PLAY);
        }
    }

    @OnClick(R.id.button_stop)
    public void buttonStopClicked() {

        if (mIsServiceBound && mMusicService.isPrepared()) {
            sendActionToService(MusicService.INTENT_ACTION_STOP);
            Dialog dialog = getDialog();
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    public void sendActionToService (String action) {
        Intent intent = new Intent(getActivity(), MusicService.class);
        intent.setAction(action);
        getActivity().startService(intent);
    }

    public void updateView (int position) {
        // Setting the track info
        textViewArtist.setText(mTrackItemList.get(position).getArtistName());
        textViewAlbum.setText(mTrackItemList.get(position).getAlbumName());
        textViewTrack.setText(mTrackItemList.get(position).getTrackName());
        if (imageView != null && mTrackItemList.get(position).hasLargeThumbnail()) {
            Picasso.with(getActivity())
                    .load(mTrackItemList.get(position).getThumbnailLargeURL())
                    .resize(500, 500)
                    .into(imageView);
        }
        // Setting the play/pause button
        if (mIsServiceBound && mMusicService.isPlaying()) {
            buttonPlay.setBackgroundResource(R.drawable.ic_pause_grey600_48dp);
        } else {
            buttonPlay.setBackgroundResource(R.drawable.ic_play_grey600_48dp);
        }
        // Setting the seekBar and progressBar
        if (mIsServiceBound && mMusicService.isPrepared()) {
            progressBar.setVisibility(View.GONE);
            if (mSeekbarHandler == null) {
                setSeekBar();
            }
        } else {
            progressBar.setVisibility(View.VISIBLE);
            if (mSeekbarHandler != null) {
                mSeekbarHandler.removeCallbacks(seekBarRunnable);
            }
            mSeekbarHandler = null;
        }
    }

    // Using onSaveInstanceState for saving a flag for the device rotation
    @Override
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putBoolean(PLAY_SAVE_STATE_KEY, true);
        super.onSaveInstanceState(savedState);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Binding the service
        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        // Setting the width of the dialog programmatically
        boolean hasTwoPanes = getResources().getBoolean(R.bool.has_two_panes);
        if (!hasTwoPanes) {
            Dialog dialog = getDialog();
            if (dialog != null) {
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setLayout(width, height);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).
                registerReceiver(mBroadcastReceiver,
                        new IntentFilter(MusicService.BROADCAST_PLAYBACK_STATE_CHANGED));
    }

    @Override
    public void onPause(){
        super.onPause();
        try {
            getActivity().unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
            mSeekbarHandler.removeCallbacks(seekBarRunnable);
            mSeekbarHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbinding the service
        getActivity().unbindService(mServiceConnection);
    }

    // Fix for a bug in onDestroyView of a FragmenDialog
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    /**
     * Setting the MusicService Binding
     */
    private ServiceConnection mServiceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Saving an istance of the binded service
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            mMusicService = binder.getService();
            mIsServiceBound = true;
            // If the device has not been rotated then start the clicked track
            if (!mHasSavedInstance){
                startPlaying();
            }
            mPosition = mMusicService.getTrackPosition();
            updateView(mPosition);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = false;
        }
    };

    private void startPlaying() {
        mMusicService.setTrackItemList(mTrackItemList);
        mMusicService.setTrackPosition(mPosition);
        mMusicService.playTrack();
    }

    /**
     * Setting the broadcast receiver to intercept broadcast msg from MusicService
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            mPosition = mMusicService.getTrackPosition();
            updateView(mPosition);
        }
    };

    /**
     * Management of the seekbar
     */
    private void setSeekBar() {
        seekBar.setMax(30);
        textViewTimeMax.setText("00:30");

        mSeekbarHandler = new Handler();
        if (getActivity() != null) {
            getActivity().runOnUiThread(seekBarRunnable);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMusicService != null && fromUser && mIsServiceBound) {
                    mMusicService.seekTo(progress * 1000);
                }
            }
        });
    }

    Runnable seekBarRunnable = new Runnable() {

        @Override
        public void run() {
            if (mIsServiceBound && mMusicService.isPlaying() &&
                    mMusicService.getCurrentPosition() < mMusicService.getDuration()) {
                int time = mMusicService.getCurrentPosition() / 1000;
                seekBar.setProgress(time);
                textViewTimeCurrent.setText("00:" + String.format("%02d", time));
            }
            mSeekbarHandler.postDelayed(this, 1000);
        }
    };


}
