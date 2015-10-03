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

import cz.lukassladky.popularmovies.utils.TMDB;

/**
 * Created by admin on 25.9.2015.
 */
public class FetchReviewsTask extends AsyncTask<Void, Void, Review[]> {

    private String LOG_TAG = FetchReviewsTask.class.getSimpleName();
    FetchReviewsListener mResultListener;
    String mMovieId;

    interface
            FetchReviewsListener {
        void onReviewsFetched(ArrayList<Review> reviewsData);
    }

    public FetchReviewsTask(FetchReviewsListener context, String movieId) {
        mResultListener = context;
        mMovieId = movieId;
    }

    private String buildApiUrl(String movieId, String postfix) {
        String BASE_URL         = "http://api.themoviedb.org/3/movie/";
        String API_KEY_PARAM    = "api_key";
        Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendPath(postfix)
                .appendQueryParameter(API_KEY_PARAM, TMDB.API_KEY)
                .build();

        return buildUri.toString();
    }
    private Review[] getReviewDataFromJSON(String resultJSONStr) throws JSONException {
        if (resultJSONStr == null) {return new Review[] {};}

        final String TMDB_RESULTS = "results";
        final String TMDB_AUTHOR = "author";
        final String TMDB_CONTENT = "content";
        final String TMDB_URL =   "url";

        JSONObject resultJSON = new JSONObject(resultJSONStr);
        JSONArray contentJSONArray = resultJSON.getJSONArray(TMDB_RESULTS);

        Review[] moviesArray = new Review[contentJSONArray.length()];

        for (int i = 0; i < contentJSONArray.length(); i++) {
            JSONObject movieJSON = contentJSONArray.getJSONObject(i);

            String reviewAuthor = movieJSON.getString(TMDB_AUTHOR);
            String reviewContent = movieJSON.getString(TMDB_CONTENT);
            String reviewUrl = movieJSON.getString(TMDB_URL);

            moviesArray[i] = new Review(
                    reviewAuthor,
                    reviewContent,
                    reviewUrl
            );
        }

        return moviesArray;
    }
    @Override
    protected Review[] doInBackground(Void... params) {
        String contentJSONStr = null;

        // Will contain the raw JSON response as a string.

        OkHttpClient client = new OkHttpClient();

        String call_type = "reviews";

        String url = buildApiUrl(mMovieId,call_type);

        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);

        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                contentJSONStr = response.body().string();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
            // If the code didn't successfully get the data, there's no point in attemping
            // to parse it.
            return null;
        }

        try {
            return getReviewDataFromJSON(contentJSONStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getMessage(),e);
            e.printStackTrace();
        }

        //if the getting or parsing json data fails or unknown type
        return null;
    }

    @Override
    protected void onPostExecute(Review[] result) {
        super.onPostExecute(result);
        ArrayList<Review> data = new ArrayList<>();
        data.clear();
        for (int i = 0; i < result.length; i++) {
            //Log.v(LOG_TAG, result[i].getTitle());
            data.add(result[i]);
        }
        mResultListener.onReviewsFetched(data);
    }
}

