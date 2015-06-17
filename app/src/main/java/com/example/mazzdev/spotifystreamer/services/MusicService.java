package com.example.mazzdev.spotifystreamer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.mazzdev.spotifystreamer.Utility;
import com.example.mazzdev.spotifystreamer.activities.PlayActivity;
import com.example.mazzdev.spotifystreamer.models.TrackItem;

import java.util.ArrayList;

/**
 * Created by Matteo on 11/06/2015.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;
    private ArrayList<TrackItem> mTrackItemList;
    private int mTrackPosition;
    private final IBinder mMusicBinder = new MusicBinder();
    private boolean mIsPrepared = false;

    private static final int NOTIFICATION_ID = 1;
    public static final String BROADCAST_MEDIA_PLAYER_PREPARED = "MEDIA_PLAYER_PREPARED";
    public static final String BROADCAST_PLAYBACK_COMPLETED = "MEDIA_PLAYBACK_COMPLETED";

    @Override
    public void onCreate(){
        super.onCreate();
        mTrackPosition = 0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return true;
    }

    public void playTrack(){
        mIsPrepared = false;
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mTrackItemList.get(mTrackPosition).getPreviewURL());
        } catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mIsPrepared = false;
        mp.reset();
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v("MUSIC PLAYER", "Playback Error");
        mIsPrepared = false;
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsPrepared = true;
        mp.start();
        runAsForeground();
        Utility.sendBroadcast(this, BROADCAST_MEDIA_PLAYER_PREPARED);
    }

    private void runAsForeground(){
        Intent notificationIntent = new Intent(this, PlayActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText("RUNNING")
                .setContentIntent(pendingIntent).build();

        startForeground(NOTIFICATION_ID, notification);

    }

    /**
     * Getters and setters.
     */
    public void setTrackItemList(ArrayList<TrackItem> trackItemList) {
        mTrackItemList = trackItemList;
    }

    public ArrayList<TrackItem> getTrackItemList() {
        return mTrackItemList;
    }

    public void setTrackPosition(int trackPosition) {
        mTrackPosition = trackPosition;
    }

    public int getTrackPosition() {
        return mTrackPosition;
    }

    public boolean getIsPrepared() {
        return mIsPrepared;
    }

    /**
    * Playback control methods.
    */
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void pausePlayer() {
        mMediaPlayer.pause();
    }

    public void seekTo(int position) {
        mMediaPlayer.seekTo(position);
    }

    public void start() {
        mMediaPlayer.start();
    }

    public void playPrev() {
        Utility.sendBroadcast(this, BROADCAST_PLAYBACK_COMPLETED);
        mTrackPosition--;
        if(mTrackPosition < 0) {
            mTrackPosition = mTrackItemList.size() - 1;
        }
        playTrack();
    }

    public void playNext() {
        Utility.sendBroadcast(this, BROADCAST_PLAYBACK_COMPLETED);
        mTrackPosition ++;
        if (mTrackPosition >= mTrackItemList.size()) {
            mTrackPosition = 0;
        }
        playTrack();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

}
