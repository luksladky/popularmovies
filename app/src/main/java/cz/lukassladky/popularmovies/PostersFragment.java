package cz.lukassladky.popularmovies;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import cz.lukassladky.popularmovies.utils.Constants;
import cz.lukassladky.popularmovies.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */

public class PostersFragment extends Fragment {


    private String LOG_TAG = PostersFragment.class.getSimpleName();

    private String sortingOrder;
    private ImageAdapter mImageAdapter;
    private ArrayList<Movie> mMoviesData;
    
    public PostersFragment() {
    }

    private void updateMoviesData() {
        new FetchMoviesTask(getActivity(), mMoviesData).execute(getPreferredSortingOrder());
        //showToast("fetching data from internet");
    }


    private void handleOfflineState() {
        Log.e(LOG_TAG, "Network is not available");

        CharSequence text = getString(R.string.network_not_available_message);
        showToast(text.toString());
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
            mMoviesData  = new ArrayList<Movie>();
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

        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(mImageAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                Movie item = (Movie) mImageAdapter.getItem(position);
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
        if (!isNetworkAvailable()) {
            handleOfflineState();
        } else {
            String newSortingOrder = getPreferredSortingOrder();

            if (newSortingOrder != null && !newSortingOrder.equals(sortingOrder)) {
                Log.d(LOG_TAG, "updating movies via API call");
                sortingOrder = newSortingOrder;
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
}



