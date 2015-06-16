package com.example.mazzdev.spotifystreamer.views;

import android.content.Context;
import android.widget.MediaController;

import com.example.mazzdev.spotifystreamer.services.MusicService;

/**
 * Created by Matteo on 16/06/2015.
 */
public class MediaControllerView extends MediaController
        implements MediaController.MediaPlayerControl {

    MusicService mMusicService;
    boolean mIsServiceBound;
    boolean mIsPaused;

    public MediaControllerView(Context context, boolean useFastForward) {
        super(context, useFastForward);
        this.setPrevNextListeners(v -> playNext(), v -> playPrev());
        this.setMediaPlayer(this);
        this.mIsServiceBound = false;
        this.mIsPaused = false;
    }

    private void playNext(){
        mMusicService.playNext();
        if (mIsPaused){
//            setController();
            mIsPaused = false;
        }
        this.show(0);
    }

    private void playPrev(){
        mMusicService.playPrev();
        if(mIsPaused){
//            setController();
            mIsPaused = false;
        }
        this.show(0);
    }

    public void setIsServiceBound(boolean isServiceBound) {
        this.mIsServiceBound = isServiceBound;
    }

    public void setMusicService(MusicService musicService) {
        this.mMusicService = musicService;
    }

    @Override
    public void start() {
        mMusicService.start();
    }

    @Override
    public void pause() {
        mIsPaused = true;
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
