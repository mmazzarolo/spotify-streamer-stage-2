package com.example.mazzdev.spotifystreamer.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.Utility;
import com.example.mazzdev.spotifystreamer.models.TrackItem;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
    public static final String BROADCAST_PLAYBACK_STATE_CHANGED = "PLAYBACK_STATE_CHANGED";
    public static final String INTENT_ACTION_PLAY = "ACTION_PLAY";
    public static final String INTENT_ACTION_PAUSE = "ACTION_PAUSE";
    public static final String INTENT_ACTION_NEXT = "ACTION_NEXT";
    public static final String INTENT_ACTION_PREV = "ACTION_PREV";
    public static final String INTENT_ACTION_STOP = "ACTION_STOP";

    @Override
    public void onCreate(){
        super.onCreate();
        mTrackPosition = 0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            handleIntent(intent);
        }
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            case INTENT_ACTION_PLAY:
                if (mIsPrepared) {
                    start();
                    buildNotification();
                }
                break;
            case INTENT_ACTION_PAUSE:
                if (mIsPrepared) {
                    pause();
                    buildNotification();
                }
                break;
            case INTENT_ACTION_PREV:
                playPrev();
                buildNotification();
                break;
            case INTENT_ACTION_NEXT:
                playNext();
                buildNotification();
                break;
            case INTENT_ACTION_STOP:
                stop();
                break;
        }
    }

    private void buildNotification() {
        // http://stackoverflow.com/questions/24465587/change-notifications-action-icon-dynamically

        // Initializing the media style (no text on notification buttons)
        NotificationCompat.MediaStyle mediaStyle = new NotificationCompat.MediaStyle();

        // Building the notification settings
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_media_play);
        builder.setContentTitle(getCurrentTrack().getTrackName());
        builder.setContentText(getCurrentTrack().getArtistName());
        builder.setLargeIcon(getLargeIcon());
        builder.setStyle(mediaStyle);

        // Adding the notification actions
        builder.addAction(createAction
                (android.R.drawable.ic_media_previous, "Previous", INTENT_ACTION_PREV));
        if (isPlaying()) {
            builder.addAction(createAction
                    (android.R.drawable.ic_media_pause, "Pause", INTENT_ACTION_PAUSE));
        } else {
            builder.addAction(createAction
                    (android.R.drawable.ic_media_play, "Play", INTENT_ACTION_PLAY));
        }
        builder.addAction(createAction
                (android.R.drawable.ic_media_next, "Next", INTENT_ACTION_NEXT));

        mediaStyle.setShowActionsInCompactView(0, 1, 2);

        // Setting the notification manager
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Getting the bitmap from Picasso
    // http://stackoverflow.com/questions/26888247/easiest-way-to-use-picasso-in-notification-icon
    private Bitmap getLargeIcon() {
        Bitmap bitmap = null;
        try {
            try {
                bitmap = new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        try {
                            return Picasso.with(getApplicationContext())
                                    .load(getCurrentTrack().getThumbnailSmallURL())
                                    .resize(200, 200)
                                    .get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            return bitmap;
        } else {
            return BitmapFactory.
                    decodeResource(getResources(), android.R.drawable.ic_lock_idle_alarm);
        }
    }

    private Action createAction (int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent =
                PendingIntent.getService(getApplicationContext(), NOTIFICATION_ID, intent, 0);
        return new Action.Builder(icon, title, pendingIntent).build();
    }

    public void initMusicPlayer(){
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    /*
     * Binding settings
     */
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

    /*
     * MediaPlayer implementation
     */
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
        buildNotification();
        Utility.sendBroadcast(this, BROADCAST_PLAYBACK_STATE_CHANGED);
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

    public TrackItem getCurrentTrack() {
        return mTrackItemList.get(mTrackPosition);
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

    public void pause() {
        mMediaPlayer.pause();
        Utility.sendBroadcast(this, BROADCAST_PLAYBACK_STATE_CHANGED);
    }

    public void seekTo(int position) {
        mMediaPlayer.seekTo(position);
        Utility.sendBroadcast(this, BROADCAST_PLAYBACK_STATE_CHANGED);
    }

    public void start() {
        mMediaPlayer.start();
        Utility.sendBroadcast(this, BROADCAST_PLAYBACK_STATE_CHANGED);
    }

    public void playPrev() {
        mTrackPosition--;
        if(mTrackPosition < 0) {
            mTrackPosition = mTrackItemList.size() - 1;
        }
        playTrack();
        Utility.sendBroadcast(this, BROADCAST_PLAYBACK_STATE_CHANGED);
    }

    public void playNext() {
        mTrackPosition ++;
        if (mTrackPosition >= mTrackItemList.size()) {
            mTrackPosition = 0;
        }
        playTrack();
        Utility.sendBroadcast(this, BROADCAST_PLAYBACK_STATE_CHANGED);
    }

    public void stop() {
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        mMediaPlayer.pause();
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        super.onDestroy();
    }
}

