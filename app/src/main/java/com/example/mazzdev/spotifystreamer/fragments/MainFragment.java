package com.example.mazzdev.spotifystreamer.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.adapters.ArtistListAdapter;
import com.example.mazzdev.spotifystreamer.models.ArtistItem;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * MainFragment
 * Searches for an artist and display results in a ListView.
 * The artist is searched when the user click the search button on the keyboard.
 */
public class MainFragment extends Fragment {

    private ArrayList<ArtistItem> mArtistItemList;
    private ArtistListAdapter mArtistListAdapter;
    private int mPosition;
    private String mArtist;

    private static final String ARTIST_ITEM_LIST_KEY = "ARTIST_ITEM_LIST";
    private static final String ARTIST_POSITION_KEY = "ARTIST_POSITION";

    @InjectView(R.id.listview_artist) ListView listViewArtist;
    @InjectView(R.id.textview_artist) TextView textViewArtist;
    @InjectView(R.id.progressbar_artist) ProgressBar progressBarArtist;
    @InjectView(R.id.searchview_artist) SearchView searchViewArtist;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
//        void onArtistItemSelected(String spotifyId, String artistName);
        void onArtistItemSelected(ArtistItem artistItem);

    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restoring the ListView (if the activity has been re-created)
        if (savedInstanceState == null || !savedInstanceState.containsKey(ARTIST_ITEM_LIST_KEY)) {
            mArtistItemList = new ArrayList<ArtistItem>();
        } else {
            mArtistItemList = savedInstanceState.getParcelableArrayList(ARTIST_ITEM_LIST_KEY);
        }
        // Restoring the selected position (if the activity has been re-created)
        if (savedInstanceState == null || !savedInstanceState.containsKey(ARTIST_POSITION_KEY)) {
            mPosition = ListView.INVALID_POSITION;
        } else {
            mPosition = savedInstanceState.getInt(ARTIST_POSITION_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflating the layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        // Setting the list adapter
        mArtistListAdapter = new ArtistListAdapter(
                getActivity(),
                R.layout.list_item_artist,
                mArtistItemList);

        listViewArtist.setAdapter(mArtistListAdapter);

        // Fix for smoothScrollToPosition suggested here:
        // http://stackoverflow.com/a/18133295/4836602
        listViewArtist.post(new Runnable() {
            @Override
            public void run() {
                listViewArtist.smoothScrollToPosition(mPosition);
            }
        });

        listViewArtist.setOnItemClickListener(onArtistItemClickListener);

        searchViewArtist.setOnQueryTextListener(onQueryTextListener);

        return rootView;
    }

    /*
    * When the user clicks on an item the activity starts a new intent.
    * The parameters of the intent are:
    * EXTRA_SPOTIFY_ID --> String with the spotifyID of the artist;
    * EXTRA_ARTIST_NAME --> String with the artist name (used in the titlebar only).
    */
    private OnItemClickListener onArtistItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            ArtistItem artistItem = mArtistListAdapter.getItem(position);
//            String artistName = mArtistListAdapter.getItem(position).getName();
//            ((Callback) getActivity()).onArtistItemSelected(spotifyId, artistName);
            ((Callback) getActivity()).onArtistItemSelected(artistItem);
            mPosition = position;
        }
    };

    // Starts a search for the artist when the search button is clicked
    // // Only one search every seond to avoid key-down & key-up
    private OnQueryTextListener onQueryTextListener = new OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mArtist = query;
            searchArtist(mArtist);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    // Saving the current parcelable item list
    @Override
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putParcelableArrayList(ARTIST_ITEM_LIST_KEY, mArtistItemList);
        savedState.putInt(ARTIST_POSITION_KEY, mPosition);
        super.onSaveInstanceState(savedState);
    }

    // since we read the location when we create the loader, all we need to do is restart things
    public void onCountryChanged() {
        searchArtist(mArtist);
    }

    private void searchArtist(String artist) {
        if (mArtist != null) {
            FetchArtistTask artistTask = new FetchArtistTask();
            artistTask.execute(artist);
        }
    }

    /* AsyncTask for the artist search
    * Can be replaced with an asynchronous call of the API wrapper.
    * https://github.com/kaaes/spotify-web-api-android
    * AsyncTask is requested by the mocks:
    * "Fetch data from Spotify in the background using AsyncTask and The Spotify Web API Wrapper"
    */
    public class FetchArtistTask extends AsyncTask<String, Void, ArtistsPager> {

        private final String LOG_TAG = FetchArtistTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            // Clearing the list, showing the progress bar and hiding the msg
            mArtistListAdapter.clear();
            progressBarArtist.setVisibility(View.VISIBLE);
            textViewArtist.setVisibility(View.GONE);
        }

        @Override
        protected ArtistsPager doInBackground(String... params) {

            // If no valid input is available...
            if (params.length == 0 || "".equals(params[0])) {
                return null;
            }

            String artist = params[0];

            // Starting a spotify web endpoint req
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            try {
                return spotify.searchArtists(artist);
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.e(LOG_TAG, spotifyError.getErrorDetails().message);
                spotifyError.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArtistsPager results) {
            // Hiding the progressbar
            progressBarArtist.setVisibility(View.GONE);
            if (results != null) {
                // If at least one artist is found
                if (results.artists.total > 0) {
                    // Hiding the keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchViewArtist.getWindowToken(), 0);
                    // Fetching every artist
                    for(Artist artist : results.artists.items) {
                        mArtistItemList.add(new ArtistItem(artist));
                    }
                    // Clearing the list AGAIN (this fixes the issue of the double call to
                    // onQueryTextSubmit using a phisical keyboard)
                    mArtistListAdapter.notifyDataSetChanged();
                } else {
                    // Artist not found: showing a msg
                    textViewArtist.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
