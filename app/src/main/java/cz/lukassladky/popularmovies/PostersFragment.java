package cz.lukassladky.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.Bind;
import cz.lukassladky.popularmovies.data.MoviesContract;
import cz.lukassladky.popularmovies.utils.Constants;
import cz.lukassladky.popularmovies.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */

public class PostersFragment extends Fragment implements FetchMoviesTask.FetchMovieListener, LoaderManager.LoaderCallbacks<Cursor> {


    private String LOG_TAG = PostersFragment.class.getSimpleName();

    public static final int MOVIES_LOADER = 0;

    private String sortingOrder;
    private ImageAdapter mImageAdapter;
    private ArrayList<Movie> mMoviesData;

    private static final String[] MOVIE_COLUMNS = {
            MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_THEMOVIEDB_ID,
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_POSTER_URL,
            MoviesContract.MoviesEntry.COLUMN_DESCRIPTION,
            MoviesContract.MoviesEntry.COLUMN_RELEASE,
            MoviesContract.MoviesEntry.COLUMN_RATING,
            //MoviesContract.MoviesEntry.COLUMN_IS_FAVOURITE
    };

    static final int COL__ID         = 0;
    static final int COL_API_ID      = 1;
    static final int COL_TITLE       = 2;
    static final int COL_POSTER      = 3;
    static final int COL_DESCRIPTION = 4;
    static final int COL_RELEASE     = 5;
    static final int COL_RATING      = 6;
    //static final int COL_IS_FAV      = 7;



    
    public PostersFragment() {
    }

    private void updateMoviesData() {
        String favoritesStr = getResources().getString(R.string.pref_posters_favorites);

        if (sortingOrder.equals(favoritesStr)) {
            showFavorites();
        } else {
            new FetchMoviesTask(this, mMoviesData).execute(sortingOrder);
        }

    }

    private void showFavorites() {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
    }

    @Override
    public void onDataFetched() {
        if (mImageAdapter != null) {
            mImageAdapter.notifyDataSetChanged();
        }
    }


    private void handleOfflineState() {
        if (sortingOrder.equals(getResources().getString(R.string.pref_posters_favorites))) {
            showFavorites();
        } else {
            Log.e(LOG_TAG, "Network is not available");
            CharSequence text = getString(R.string.network_not_available_message);
            showToast(text.toString());
        }
    }

    private String getPreferredSortingOrder() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_posters_sort_popularity));
    }

    public void showToast(String message) {
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Bind(R.id.gridview) GridView gridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View rootView = inflater.inflate(R.layout.fragment_posters, container, false);

        if(savedInstanceState != null){
            //get back your data and populate the adapter here

            mMoviesData = savedInstanceState.getParcelableArrayList(Constants.parcMoviesKey);
            sortingOrder = savedInstanceState.getString(Constants.parcSortKey);


        }else{
            mMoviesData  = new ArrayList<>();
            sortingOrder = getPreferredSortingOrder();
            if (Utility.isNetworkAvailable(getActivity())) {
                updateMoviesData();
            } else {
                handleOfflineState();
            }
        }


        mImageAdapter = new ImageAdapter(
                rootView.getContext(),
                mMoviesData);

        //ButterKnife.bind(this,rootView);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(mImageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                Movie item = mImageAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Constants.parcMovieObjKey, item);


                startActivity(intent);

            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume(); // Always call the superclass method first
        // Test Network

        String newSortingOrder = getPreferredSortingOrder();

        if (newSortingOrder != null && !newSortingOrder.equals(sortingOrder)) {
            sortingOrder = newSortingOrder;
            if (!Utility.isNetworkAvailable(getActivity())) {
                handleOfflineState();
                //remove old posters
                mMoviesData.clear();
                onDataFetched();
            } else {
                Log.d(LOG_TAG, "updating movies via API call");
                updateMoviesData();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(Constants.parcSortKey, sortingOrder);
        outState.putParcelableArrayList(Constants.parcMoviesKey, mMoviesData);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = MoviesContract.MoviesEntry.COLUMN_IS_FAVOURITE + " = ?";
        String[] selectionArgs = new String[] {"1"};
        String sortOrder = MoviesContract.MoviesEntry.COLUMN_TITLE + " ASC";

        mMoviesData.clear();
       return new CursorLoader(getActivity(),
                MoviesContract.MoviesEntry.CONTENT_URI,
                MOVIE_COLUMNS,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMoviesData.clear();
        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                Movie newMovie = new Movie(
                        cursor.getString(COL__ID),
                        cursor.getString(COL_API_ID),
                        cursor.getString(COL_TITLE),
                        cursor.getString(COL_POSTER),
                        cursor.getString(COL_DESCRIPTION),
                        cursor.getString(COL_RATING),
                        cursor.getString(COL_RELEASE),
                        false
                );
                mMoviesData.add(newMovie);
            }
        }
        onDataFetched();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}



