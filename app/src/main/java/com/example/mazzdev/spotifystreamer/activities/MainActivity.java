package com.example.mazzdev.spotifystreamer.activities;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.fragments.MainFragment;
import com.example.mazzdev.spotifystreamer.fragments.PlayFragment;
import com.example.mazzdev.spotifystreamer.fragments.TrackFragment;
import com.example.mazzdev.spotifystreamer.models.ArtistItem;
import com.example.mazzdev.spotifystreamer.services.MusicService;


public class MainActivity extends AppCompatActivity implements MainFragment.Callback {

    private static final String TRACKFRAGMENT_TAG = "TFTAG";

    private boolean mHasTwoPanes;
    private MainFragment mainFragment;
    private MusicService mMusicService;
    private boolean mIsServiceBound = false;
    private Toast mToast;
    private String mSearched;

    private static final String ARTIST_SEARCHED_KEY = "ARTIST_SEARCHED";

    /**
     * Activity lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Is the app running on a tablet?
        mHasTwoPanes = findViewById(R.id.main_container) != null;

        // In two-pane mode, show the detail view in this activity by adding or replacing
        // the detail fragment using a fragment transaction
        if (mHasTwoPanes) {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.track_container, new TrackFragment(), TRACKFRAGMENT_TAG)
                        .commit();
            }
        }

        // Restoring the ListView (if the activity has been re-created)
        if (savedInstanceState != null) {
            mSearched = savedInstanceState.getString(ARTIST_SEARCHED_KEY);
        }

        // Get the main fragment of this activity
        mainFragment =
                (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_main);

        // Handling search intent
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mainFragment.searchArtist(query);
            mSearched = query;
        }
    }

    // Saving the current parcelable item list
    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        if (mSearched != null) {
            savedState.putString(ARTIST_SEARCHED_KEY, mSearched);
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
     * Managing the artist click
     */
    @Override
    public void onArtistItemSelected(ArtistItem artistItem) {
        // In single-pane mode, add the trackFragment to the container
        if (mHasTwoPanes) {
            Bundle args = new Bundle();
            args.putParcelable(TrackFragment.TRACK_ARTIST_ITEM_KEY, artistItem);

            TrackFragment fragment = new TrackFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.track_container, fragment, TRACKFRAGMENT_TAG)
                    .commit();
        // In two-pane mode, start TrackActivity
        } else {
            Intent intent = new Intent(this, TrackActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelable(TrackFragment.TRACK_ARTIST_ITEM_KEY, artistItem);
            intent.putExtras(extras);
            startActivity(intent);
        }
    }

    /**
     * Setting the Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Assumes current activity is the searchable activity
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//            searchView.setIconifiedByDefault(false);
            if (mSearched != null) {
                searchView.setQuery(mSearched, false);
            }
        }

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
            // Share the spotify external URL (if available)
            case R.id.menu_track_share:
                if (mIsServiceBound && !mMusicService.isEmpty()) {
                    String externalSpotifyURL =
                            mMusicService.getCurrentTrack().getExternalSpotifyURL();
                    if (externalSpotifyURL != null) {
                        shareString(externalSpotifyURL);
                    } else {
                        CharSequence text = getString(R.string.undefined_external_url);
                        showToast(text);
                    }
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
        String subject = "Spotify Streamer";
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
    private ServiceConnection mServiceConnection = new ServiceConnection() {
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
