package com.example.mazzdev.spotifystreamer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.activities.MainActivity;

import java.util.ArrayList;

/**
 * Created by Matteo on 11/06/2015.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;

    private ArrayList<String> mTrackURLList;
    private int mTrackPosition;

    private final IBinder mMusicBinder = new MusicBinder();
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate(){
        super.onCreate();
        mTrackPosition=0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    public void setTrackURLList(ArrayList<String> trackURLList) {
        mTrackURLList = trackURLList;
    }

    public void setTrackPosition(int trackPosition) {
        mTrackPosition = trackPosition;
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
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    public void playTrack(){
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mTrackURLList.get(mTrackPosition));
        } catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset();
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v("MUSIC PLAYER", "Playback Error");
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
        notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification();
        notification.tickerText = "CIAO";
        //notification.icon = R.drawable.play0;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(getApplicationContext(), "MusicPlayerSample",
        "Playing: " + "songName", pendInt);

        startForeground(NOTIFICATION_ID, notification);

        // Broadcast intent to activity to let it know the media player has been prepared
        Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);
    }

    //playback methods
    public int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration(){
        return mMediaPlayer.getDuration();
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        mMediaPlayer.pause();
    }

    public void seek(int position){
        mMediaPlayer.seekTo(position);
    }

    public void start(){
        mMediaPlayer.start();
    }

    public void playPrev(){
        mTrackPosition--;
        if(mTrackPosition < 0) {
            mTrackPosition = mTrackURLList.size() - 1;
        }
        playTrack();
    }

    public void playNext(){
        mTrackPosition ++;
        if (mTrackPosition >= mTrackURLList.size()) {
            mTrackPosition = 0;
        }
        playTrack();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

}
