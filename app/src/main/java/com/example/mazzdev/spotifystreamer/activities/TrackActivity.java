package com.example.mazzdev.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.fragments.TrackFragment;


public class TrackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        if (savedInstanceState == null) {

            Bundle args = new Bundle();
            args.putString(TrackFragment.TRACK_SPOTIFY_ID,
                    getIntent().getStringExtra("EXTRA_SPOTIFY_ID"));
            args.putString(TrackFragment.TRACK_ARTIST_NAME,
                    getIntent().getStringExtra("EXTRA_ARTIST_NAME"));

            TrackFragment trackFragment = new TrackFragment();
            trackFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_container, trackFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_track, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // To the Settings menu
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
