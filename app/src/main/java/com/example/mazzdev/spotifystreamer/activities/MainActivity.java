package com.example.mazzdev.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.Utility;
import com.example.mazzdev.spotifystreamer.fragments.MainFragment;
import com.example.mazzdev.spotifystreamer.fragments.TrackFragment;


public class MainActivity extends AppCompatActivity implements MainFragment.Callback {

    private static final String TRACKFRAGMENT_TAG = "TFTAG";

    private boolean mTwoPane;
    private String mCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.track_container) != null) {
            // The track_container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.track_container, new TrackFragment(), TRACKFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onArtistItemSelected(String spotifyId, String artistName) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putString(TrackFragment.TRACK_SPOTIFY_ID, spotifyId);
            args.putString(TrackFragment.TRACK_ARTIST_NAME, artistName);

            TrackFragment fragment = new TrackFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.track_container, fragment, TRACKFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, TrackActivity.class);
            Bundle extras = new Bundle();
            extras.putString("EXTRA_SPOTIFY_ID", spotifyId);
            extras.putString("EXTRA_ARTIST_NAME", artistName);
            intent.putExtras(extras);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String country = Utility.getPreferredCountry(this);
        // update the location in our second pane using the fragment manager
        if (country != null && !country.equals(mCountry)) {
            MainFragment mainFragment =
                    (MainFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_main);
            if ( null != mainFragment ) {
                mainFragment.onCountryChanged();
            }
            mCountry = country;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
