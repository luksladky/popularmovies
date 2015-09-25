package cz.lukassladky.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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

import cz.lukassladky.popularmovies.utils.Constants;

/**
 * Created by admin on 25.9.2015.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

    private String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    Context mContext;
    ArrayList<Movie> mMoviesData;

    public FetchMoviesTask(Context context, ArrayList<Movie> movieData) {
        mContext = context;
        mMoviesData = movieData;
    }


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

        String sortBy  = params[0]+".desc" ;
        String minVoteCount = Integer.toString(Constants.minVoteCount);

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJSONStr = null;

        try {
            String BASE_URL         = "http://api.themoviedb.org/3/discover/movie";
            String API_KEY_PARAM    = "api_key";
            String SORT_PARAM       = "sort_by";
            String VOTE_COUNT_PARAM = "vote_count.gte";

            Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sortBy)
                    .appendQueryParameter(VOTE_COUNT_PARAM, minVoteCount)
                    .appendQueryParameter(API_KEY_PARAM, Constants.API_KEY)
                    .build();
            // Will contain the raw JSON response as a string.

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
        mMoviesData.clear();
        for (int i = 0; i < result.length; i++) {
            //Log.v(LOG_TAG, result[i].getTitle());
            mMoviesData.add(result[i]);
        }

        mImageAdapter.notifyDataSetChanged();
    }
}
