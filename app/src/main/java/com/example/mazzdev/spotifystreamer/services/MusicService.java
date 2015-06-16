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
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.activities.MainActivity;
import com.example.mazzdev.spotifystreamer.activities.PlayActivity;
import com.example.mazzdev.spotifystreamer.fragments.PlayFragment;
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
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate(){
        super.onCreate();
        mTrackPosition=0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

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
//        mMediaPlayer.stop();
//        mMediaPlayer.release();
        return true;
    }

    public void playTrack(){
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mTrackItemList.get(mTrackPosition).getPreviewURL());
        } catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset();
        playNext();
        // Broadcast intent to activity to let it know the media player has been prepared
        Intent onCompletedIntent = new Intent("MEDIA_PLAYER_COMPLETED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onCompletedIntent);
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
        runAsForeground();

        // Broadcast intent to activity to let it know the media player has been prepared
        Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);
    }

    private void runAsForeground(){
        Intent notificationIntent = new Intent(this, PlayActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText("RUNNING")
                .setContentIntent(pendingIntent).build();

        startForeground(NOTIFICATION_ID, notification);

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
            mTrackPosition = mTrackItemList.size() - 1;
        }
        playTrack();
    }

    public void playNext(){
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
