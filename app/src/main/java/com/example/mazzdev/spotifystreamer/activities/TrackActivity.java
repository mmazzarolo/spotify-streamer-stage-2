package com.example.mazzdev.spotifystreamer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.fragments.PlayFragment;
import com.example.mazzdev.spotifystreamer.fragments.TrackFragment;
import com.example.mazzdev.spotifystreamer.services.MusicService;


public class TrackActivity extends AppCompatActivity {

    private MusicService mMusicService;
    private boolean mIsServiceBound = false;
    private Toast mToast;

    /**
     * Activity lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        if (savedInstanceState == null) {

            Bundle args = new Bundle();
            args.putParcelable(TrackFragment.TRACK_ARTIST_ITEM_KEY,
                    getIntent().getParcelableExtra(TrackFragment.TRACK_ARTIST_ITEM_KEY));

            TrackFragment trackFragment = new TrackFragment();
            trackFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_container, trackFragment)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Binding the service
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbinding the service
        unbindService(mServiceConnection);
    }

    /**
     * Setting the Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_track, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // Open the settings menu
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            // Share the track preview URL
            case R.id.menu_track_share:
                if (mIsServiceBound && !mMusicService.isEmpty()) {
                    shareString(mMusicService.getCurrentTrack().getPreviewURL());
                } else {
                    CharSequence text = getString(R.string.select_a_track);
                    showToast(text);
                }
                break;
            // Share the track URI
            case R.id.menu_uri_share:
                if (mIsServiceBound && !mMusicService.isEmpty()) {
                    shareString(mMusicService.getCurrentTrack().getPreviewURL());
                } else {
                    CharSequence text = getString(R.string.select_a_track);
                    showToast(text);
                }
                break;
            // Show to the now-playing fragment
            case R.id.menu_now_playing:
                if (mIsServiceBound && !mMusicService.isEmpty()) {
                    DialogFragment playFragment = new PlayFragment();
                    playFragment.show(getSupportFragmentManager(), "dialog");
                } else {
                    CharSequence text = getString(R.string.select_a_track);
                    showToast(text);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareString(String text) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String subject = "spotify-stramer-stage-2";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent,
                getResources().getString(R.string.share_title)));
    }

    private void showToast(CharSequence text) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        mToast.show();
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
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = false;
        }
    };

    public MusicService getMusicService() {
        return mMusicService;
    }

    public boolean isServiceBound() {
        return mIsServiceBound;
    }
}
