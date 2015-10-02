package cz.lukassladky.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.lukassladky.popularmovies.utils.Constants;
import cz.lukassladky.popularmovies.utils.TMDB;

/**
 * Created by admin on 25.9.2015.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

    private String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    FetchMovieListener mResultListener;
    ArrayList<Movie> mMoviesData;

    interface FetchMovieListener {
        void onDataFetched();
    }

    public FetchMoviesTask(FetchMovieListener context, ArrayList<Movie> movieData) {
        mResultListener = context;
        mMoviesData = movieData;
    }


    private Movie[] getMoviesDataFromJSON(String resultJSONStr) throws JSONException {

        final String TMDB_RESULTS = "results";
        final String TMDB_ID = "id";
        final String TMDB_TITLE = "title";
        final String TMDB_POSTER = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RATING = "vote_average";
        final String TMDB_RELEASE_DATE = "release_date";

        JSONObject resultJSON = new JSONObject(resultJSONStr);
        JSONArray moviesJSONArray = resultJSON.getJSONArray(TMDB_RESULTS);

        Movie[] moviesArray = new Movie[moviesJSONArray.length()];

        for (int i = 0; i < moviesJSONArray.length(); i++) {
            JSONObject movieJSON = moviesJSONArray.getJSONObject(i);

            String movieId = movieJSON.getString(TMDB_ID);
            String movieTitle = movieJSON.getString(TMDB_TITLE);
            String movieOverview = movieJSON.getString(TMDB_OVERVIEW);
            String movieRating = movieJSON.getString(TMDB_RATING);
            String movieReleaseDate = movieJSON.getString(TMDB_RELEASE_DATE);

            //get poster thumbnail full url
            String moviePosterHash = movieJSON.getString(TMDB_POSTER);
            String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
            String IMAGE_SIZE_TAG = "w185";
            String posterImageUrl = IMAGE_BASE_URL + IMAGE_SIZE_TAG + moviePosterHash;


            moviesArray[i] = new Movie(
                    movieId,
                    movieTitle,
                    posterImageUrl,
                    movieOverview,
                    movieRating,
                    movieReleaseDate);
        }

        return moviesArray;
    }

    @Override
    protected Movie[] doInBackground(String... params) {

        String sortBy  = params[0]+".desc" ;
        String minVoteCount = Integer.toString(Constants.minVoteCount);

        String BASE_URL         = "http://api.themoviedb.org/3/discover/movie";
        String API_KEY_PARAM    = "api_key";
        String SORT_PARAM       = "sort_by";
        String VOTE_COUNT_PARAM = "vote_count.gte";

        String moviesJSONStr = null;


        Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(SORT_PARAM, sortBy)
                .appendQueryParameter(VOTE_COUNT_PARAM, minVoteCount)
                .appendQueryParameter(API_KEY_PARAM, TMDB.API_KEY)
                .build();
        // Will contain the raw JSON response as a string.

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(buildUri.toString()).build();
        Call call = client.newCall(request);

        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                moviesJSONStr = response.body().string();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
            // If the code didn't successfully get the data, there's no point in attemping
            // to parse it.
            return null;
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

        mResultListener.onDataFetched();
    }
}
