package cz.lukassladky.popularmovies;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import cz.lukassladky.popularmovies.data.MoviesContract;
import cz.lukassladky.popularmovies.utils.Constants;
import cz.lukassladky.popularmovies.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */

public class PostersFragment extends Fragment implements FetchMoviesTask.FetchMovieListener, LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Movie selectedMovie);
    }



    private String LOG_TAG = PostersFragment.class.getSimpleName();

    public static final int MOVIES_LOADER = 0;

    private String mSortingOrder;
    private ImageAdapter mImageAdapter;
    private ArrayList<Movie> mMoviesData;
    private int mPosition;

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
        if (mSortingOrder.equals(getResources().getString(R.string.pref_posters_favorites))) {
            showFavorites();
        } else {
            new FetchMoviesTask(this, mMoviesData).execute(mSortingOrder);
        }

    }

    private void showFavorites() {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
    }

    @Override
    public void onDataFetched() {
        if (mImageAdapter != null) {
            mImageAdapter.notifyDataSetChanged();
            //select first movie
            /*if (mTwoPane) {
                Movie item = mImageAdapter.getItem(0);
                if (item != null) {
                    ((Callback) getActivity()).onItemSelected(item);
                }
            }*/
        }
    }


    public void handleOfflineState() {
        if (mSortingOrder.equals(getResources().getString(R.string.pref_posters_favorites))) {
            showFavorites();
        } else {
            Log.e(LOG_TAG, "Network is not available");
            CharSequence text = getString(R.string.network_not_available_message);
            showToast(text.toString());
        }
    }
    
    public void showToast(String message) {
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }

    public void onSortingOrderChanged() {
        mSortingOrder = Utility.getPreferredSortingOrder(getActivity());
        if (!Utility.isNetworkAvailable(getActivity())) {
            handleOfflineState();
            //remove old posters
            mMoviesData.clear();
            onDataFetched();
        } else {
            updateMoviesData();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(savedInstanceState != null){
            //get back your data and populate the adapter here

            mMoviesData = savedInstanceState.getParcelableArrayList(Constants.PARC_MOVIES_ARRAY_KEY);
            mSortingOrder = savedInstanceState.getString(Constants.parcSortKey);


        }else{
            mMoviesData  = new ArrayList<>();
            mSortingOrder = Utility.getPreferredSortingOrder(getActivity());
            if (Utility.isNetworkAvailable(getActivity())) {
                updateMoviesData();
            } else {
                handleOfflineState();
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View rootView = inflater.inflate(R.layout.fragment_posters, container, false);

        mImageAdapter = new ImageAdapter(
                rootView.getContext(),
                mMoviesData);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(mImageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                Movie item = mImageAdapter.getItem(position);
                if (item != null) {
                    ((Callback) getActivity()).onItemSelected(item);
                }

                //mPosition = position;

            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(Constants.parcSortKey, mSortingOrder);
        outState.putParcelableArrayList(Constants.PARC_MOVIES_ARRAY_KEY, mMoviesData);

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



