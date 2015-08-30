package cz.lukassladky.popularmovies;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */

public class PostersFragment extends Fragment {


    private String LOG_TAG = PostersFragment.class.getSimpleName();

    private String sortingOrder;
    private ImageAdapter mImageAdapter;
    private Movie[] mMoviesData;
    public PostersFragment() {
    }

    private void updateMovies() {
        new FetchMoviesTask().execute(getPreferredSortingOrder());
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public String getPreferredSortingOrder() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_posters_sort_popularity));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sortingOrder = getPreferredSortingOrder(); //TODO check if this is okay
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View rootView = inflater.inflate(R.layout.fragment_posters, container, false);

        if(savedInstanceState != null){
            //get back your data and populate the adapter here
            //ArrayList<Result> result_saved =  savedInstanceState.getParcelableArrayList(MOVIES_KEY);
        }else{
            updateMovies();
            // updateMovies();
        }


        mImageAdapter = new ImageAdapter(
                rootView.getContext(),
                new ArrayList<Movie>());

        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(mImageAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                Movie item = (Movie) mImageAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("movieObj", item);
                //Toast.makeText(rootView.getContext(),item.getTitle(),Toast.LENGTH_SHORT).show();

                startActivity(intent);

            }
        });

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        //TODO saveInstanceState

    }

    @Override
    public void onResume() {
        super.onResume(); // Always call the superclass method first
        // Test Network
        if (!isNetworkAvailable()) {
            Log.e(LOG_TAG, "Network is not available");

            CharSequence text = getString(R.string.network_not_available_message);
            Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
            toast.show();
        } else {
            String newSortingOrder = getPreferredSortingOrder();

            if (newSortingOrder != null && !newSortingOrder.equals(sortingOrder)) {
                Log.d(LOG_TAG, "updating movies via API call");
                sortingOrder = newSortingOrder;
                updateMovies();
            }


        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


    }

    //@SuppressWarnings("InfiniteLoopStatement")
    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private Movie[] getMoviesDataFromJSON(String resultJSONStr) throws JSONException {

            final String OMDB_RESULTS = "results";
            final String OMDB_TITLE = "title";
            final String OMDB_POSTER = "poster_path";
            final String OMDB_OVERVIEW = "overview";
            final String OMDB_RATING = "vote_average";
            final String OMDB_RELEASE_DATE = "release_date";

            JSONObject resultJSON = new JSONObject(resultJSONStr);
            JSONArray moviesJSONArray = resultJSON.getJSONArray(OMDB_RESULTS);

            Movie[] moviesArray = new Movie[moviesJSONArray.length()];

            for (int i = 0; i < moviesJSONArray.length(); i++) {
                JSONObject movieJSON = moviesJSONArray.getJSONObject(i);

                String movieTitle = movieJSON.getString(OMDB_TITLE);
                String movieOverview = movieJSON.getString(OMDB_OVERVIEW);
                String movieRating = movieJSON.getString(OMDB_RATING);
                String movieReleaseDate = movieJSON.getString(OMDB_RELEASE_DATE);

                //get poster thumbnail full url
                String moviePosterHash = movieJSON.getString(OMDB_POSTER);
                String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
                String IMAGE_SIZE_TAG = "w185";
                String posterImageUrl = IMAGE_BASE_URL + IMAGE_SIZE_TAG + moviePosterHash;


                moviesArray[i] = new Movie(movieTitle,posterImageUrl, movieOverview, movieRating, movieReleaseDate);
            }


            return moviesArray;
        }



        @Override
        protected Movie[] doInBackground(String... params) {


            String api_key = ""; //TODO add api key
            String sortBy  = params[0]+".desc" ;
            int voteCountGte = 500;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJSONStr = null;

            try {
                String BASE_URL         = "http://api.themoviedb.org/3/discover/movie";
                String API_KEY_PARAM    = "api_key";
                String SORT_PARAM       = "sort_by";
                String VOTE_COUNT_PARAM = "vote_count.gte";

                Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sortBy)
                        .appendQueryParameter(VOTE_COUNT_PARAM, Integer.toString(voteCountGte))
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();
                //URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=0db781196aa1b1144bdd2346e1ae4631");
                URL url = new URL(buildUri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJSONStr = buffer.toString();


                //Log.v(LOG_TAG, forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesDataFromJSON(moviesJSONStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG,e.getMessage(),e);
                e.printStackTrace();
            }

            //if the getting or parsing json data fails
            return null;
        }

        @Override
        protected void onPostExecute(Movie[] result) {
            super.onPostExecute(result);
            mImageAdapter.clear();
            for (int i = 0; i < result.length; i++) {
                //Log.v(LOG_TAG, result[i].getTitle());
                mImageAdapter.add(result[i]);
            }
            mImageAdapter.notifyDataSetChanged();

        }
    }
}



