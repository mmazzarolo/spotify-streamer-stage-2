package com.example.mazzdev.spotifystreamer.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mazzdev.spotifystreamer.R;
import com.example.mazzdev.spotifystreamer.Utility;
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

    private ArrayList<ArtistItem> mArtistItemList = new ArrayList<>();
    private ArtistListAdapter mArtistListAdapter;
    private int mPosition = ListView.INVALID_POSITION;

    private static final String ARTIST_ITEM_LIST_KEY = "ARTIST_ITEM_LIST";
    private static final String ARTIST_POSITION_KEY = "ARTIST_POSITION";

    @InjectView(R.id.listview_artist) ListView listViewArtist;
    @InjectView(R.id.textview_message) TextView textViewMessage;
    @InjectView(R.id.progressbar_artist) ProgressBar progressBarArtist;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        void onArtistItemSelected(ArtistItem artistItem);
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restoring the ListView (if the activity has been re-created)
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(ARTIST_ITEM_LIST_KEY) &&
                savedInstanceState.containsKey(ARTIST_POSITION_KEY)) {
            mArtistItemList = savedInstanceState.getParcelableArrayList(ARTIST_ITEM_LIST_KEY);
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
        listViewArtist.post(() -> listViewArtist.smoothScrollToPosition(mPosition));
        listViewArtist.setOnItemClickListener(onArtistItemClickListener);
        listViewArtist.setEmptyView(textViewMessage);

        return rootView;
    }

    // When the user clicks on an item the activity starts a new intent.
    private OnItemClickListener onArtistItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            ArtistItem artistItem = mArtistListAdapter.getItem(position);
            ((Callback) getActivity()).onArtistItemSelected(artistItem);
            mPosition = position;
        }
    };

    // Saving the current parcelable item list
    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putParcelableArrayList(ARTIST_ITEM_LIST_KEY, mArtistItemList);
        savedState.putInt(ARTIST_POSITION_KEY, mPosition);
    }

    public void searchArtist(String artist) {
        mArtistListAdapter.clear();
        if (Utility.isNetworkAvailable(getActivity())) {
            FetchArtistTask artistTask = new FetchArtistTask();
            artistTask.execute(artist);
        } else {
            textViewMessage.setText(R.string.no_connection);
        }
    }

    // AsyncTask for the artist search
    public class FetchArtistTask extends AsyncTask<String, Void, ArtistsPager> {

        private final String LOG_TAG = FetchArtistTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            // Clearing the list, showing the progress bar and hiding the msg
            progressBarArtist.setVisibility(View.VISIBLE);
//            textViewMessage.setVisibility(View.GONE);
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
                    // Fetching every artist
                    for(Artist artist : results.artists.items) {
                        mArtistItemList.add(new ArtistItem(artist));
                    }
                    // Clearing the list AGAIN (this fixes the issue of the double call to
                    // onQueryTextSubmit using a phisical keyboard)
                    mArtistListAdapter.notifyDataSetChanged();
                } else {
                    // Artist not found: showing a msg
//                    textViewMessage.setVisibility(View.VISIBLE);
                    textViewMessage.setText(R.string.artist_not_found);
                }
            }
        }

    }

}
