package com.example.mazzdev.spotifystreamer.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.Utility;
import com.example.mazzdev.spotifystreamer.activities.MainActivity;
import com.example.mazzdev.spotifystreamer.activities.TrackActivity;
import com.example.mazzdev.spotifystreamer.adapters.TrackListAdapter;
import com.example.mazzdev.spotifystreamer.models.ArtistItem;
import com.example.mazzdev.spotifystreamer.models.TrackItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * MainFragment
 * Searches for a tracks top ten and display results in a ListView.
 * The tracks are searched in the onStart() method.
 */
public class TrackFragment extends Fragment {

    private ArrayList<TrackItem> mTrackItemList;
    private TrackListAdapter mTrackListAdapter;
    private String mArtistName;
    private String mArtistSpotifyID;
    private int mPosition;
    private boolean mHasSavedInstance = false;

    private static final String TRACK_ITEM_LIST_KEY = "TRACK_ITEM_LIST";
    private static final String TRACK_POSITION_KEY = "TRACK_POSITION";
    public static final String TRACK_ARTIST_ITEM_KEY = "TRACK_ARTIST_ITEM_KEY";

    @InjectView(R.id.progressbar_track) ProgressBar progressBarTrack;
    @InjectView(R.id.textview_track) TextView textViewTrack;
    @InjectView(R.id.listview_track) ListView listViewTrack;

    // If the activity has been re-created get the list back from saveInstanceState
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restoring the ListView (if the activity has been re-created)
        if (savedInstanceState == null || !savedInstanceState.containsKey(TRACK_ITEM_LIST_KEY)) {
            mTrackItemList = new ArrayList<TrackItem>();
        } else {
            mTrackItemList = savedInstanceState.getParcelableArrayList(TRACK_ITEM_LIST_KEY);
            mHasSavedInstance = true;
        }
        // Restoring the selected position (if the activity has been re-created)
        if (savedInstanceState == null || !savedInstanceState.containsKey(TRACK_POSITION_KEY)) {
            mPosition = ListView.INVALID_POSITION;
        } else {
            mPosition = savedInstanceState.getInt(TRACK_POSITION_KEY);
            mHasSavedInstance = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Getting intent bundle
        Bundle args = getArguments();
        if (args != null) {
            ArtistItem artistItem = args.getParcelable(TRACK_ARTIST_ITEM_KEY);
            mArtistSpotifyID = artistItem.getSpotifyId();
            mArtistName = artistItem.getName();
        }

        // Inflating the layout
        View rootView = inflater.inflate(R.layout.fragment_track, container, false);
        ButterKnife.inject(this, rootView);

        // Setting the list adapter
        mTrackListAdapter = new TrackListAdapter(
                getActivity(),
                R.layout.list_item_track,
                mTrackItemList);
        listViewTrack.setAdapter(mTrackListAdapter);

        listViewTrack.post(() -> listViewTrack.smoothScrollToPosition(mPosition));

        listViewTrack.setOnItemClickListener(onTrackItemClickListener);

        return rootView;
    }

    private OnItemClickListener onTrackItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            mPosition = position;
            boolean hasTwoPanes = getResources().getBoolean(R.bool.has_two_panes);

            // In two-pane mode, we are in the MainActivity activity
            if (hasTwoPanes) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity.isServiceBound()) {
                    mainActivity.getMusicService().setTrackItemList(mTrackItemList);
                    mainActivity.getMusicService().setTrackPosition(mPosition);
                    mainActivity.getMusicService().playTrack();
                }
            // In single-pane mode, we are in the TrackActivity activity
            } else {
                TrackActivity trackActivity = (TrackActivity) getActivity();
                if (trackActivity.isServiceBound()) {
                    trackActivity.getMusicService().setTrackItemList(mTrackItemList);
                    trackActivity.getMusicService().setTrackPosition(mPosition);
                    trackActivity.getMusicService().playTrack();
                }
            }

            // Show the now-playing fragment
            DialogFragment playFragment = new PlayFragment();
            playFragment.show(getActivity().getSupportFragmentManager(), "dialog");
        }
    };

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Setting the titlebar subtitle with the mArtistName
        // From: http://stackoverflow.com/a/18320838/4836602
        // AppCompatActivity is used instead of ActionBarActivity
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(mArtistName);
        if (mArtistSpotifyID != null && !mHasSavedInstance) {
            searchTracks(mArtistSpotifyID);
        }
    }

    // Saving the current parcelable item list
    @Override
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putParcelableArrayList(TRACK_ITEM_LIST_KEY, mTrackItemList);
        savedState.putInt(TRACK_POSITION_KEY, mPosition);
        super.onSaveInstanceState(savedState);
    }

    private void searchTracks(String artist) {
        FetchTrackTask fetchTrackTask = new FetchTrackTask();
        fetchTrackTask.execute(artist);
    }

    /* AsyncTask for the track search
    * Can be replaced with an asynchronous call of the API wrapper.
    * https://github.com/kaaes/spotify-web-api-android
    * AsyncTask is requested by the mocks:
    * "Fetch data from Spotify in the background using AsyncTask and The Spotify Web API Wrapper"
    */
    public class FetchTrackTask extends AsyncTask<String, Void, Tracks> {

        private final String LOG_TAG = FetchTrackTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            // Clearing the list, showing the progress bar and hiding the msg
            mTrackListAdapter.clear();
            progressBarTrack.setVisibility(View.VISIBLE);
            textViewTrack.setVisibility(View.GONE);
        }

        @Override
        protected Tracks doInBackground(String... params) {

            // If no valid input is available...
            if (params.length == 0 || "".equals(params[0])) {
                return null;
            }

            String track = params[0];

            // Starting a spotify web endpoint req
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            try {
                // Setting the country with the user selected country from the settings menu
                Map<String, Object> options = new HashMap<>();
                options.put("country", Utility.getPreferredCountry(getActivity()));
                return spotify.getArtistTopTrack(track, options);
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.e(LOG_TAG, spotifyError.getErrorDetails().message);
                spotifyError.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Tracks results) {
            // Hiding the progressbar
            progressBarTrack.setVisibility(View.GONE);
            if (results != null) {
                // If at least one track has been found
                if (!results.tracks.isEmpty()) {
                    // Fetching every track
                    for (Track track : results.tracks) {
                        mTrackItemList.add(new TrackItem(track));
                    }
                    mTrackListAdapter.notifyDataSetChanged();
                } else {
                    // Track not found: showing a msg
                    textViewTrack.setVisibility(View.VISIBLE);
                }
            }
        }

    }
}
