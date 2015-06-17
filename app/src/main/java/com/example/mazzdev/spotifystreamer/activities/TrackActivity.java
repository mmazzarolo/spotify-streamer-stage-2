package com.example.mazzdev.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.fragments.PlayFragment;
import com.example.mazzdev.spotifystreamer.fragments.TrackFragment;
import com.example.mazzdev.spotifystreamer.models.TrackItem;

import java.util.ArrayList;


public class TrackActivity extends AppCompatActivity implements TrackFragment.Callback {

    private boolean mTwoPane;

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

        mTwoPane = (findViewById(R.id.fragment_main) != null);
    }

    @Override
    public void onTrackItemSelected(ArrayList<TrackItem> trackItemList, int position) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelableArrayList(PlayFragment.PLAY_TRACK_LIST_KEY, trackItemList);
            args.putInt(PlayFragment.PLAY_POSITION_KEY, position);
            DialogFragment playFragment = new PlayFragment();
            playFragment.setArguments(args);

            playFragment.show(getSupportFragmentManager(), "dialog");

//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.track_container, fragment, TRACKFRAGMENT_TAG)
//                    .commit();
        } else {
            Intent intent = new Intent(this, PlayActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelableArrayList(PlayFragment.PLAY_TRACK_LIST_KEY, trackItemList);
            extras.putInt(PlayFragment.PLAY_POSITION_KEY, position);
            intent.putExtras(extras);
            startActivity(intent);
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
