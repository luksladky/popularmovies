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
import cz.lukassladky.popularmovies.utils.MovieDB;

/**
 * Created by admin on 25.9.2015.
 */
public class FetchDetailsContentTask extends AsyncTask<Void, Void, TitleContentContainer[]> {
    public static final int TYPE_REVIEW = 0;
    public static final int TYPE_TRAILER = 1;


    private String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    FetchDetailContentsListener mResultListener;
    ArrayList<TitleContentContainer> mData;
    int mContentType;
    String mMovieId;

    interface FetchDetailContentsListener {
        void onReviewsFetched();
        void onTrailersFetched();
    }

    public FetchDetailsContentTask(FetchDetailContentsListener context, String movieId, ArrayList<TitleContentContainer> dataArrayList, int contentType) {
        mResultListener = context;
        mData = dataArrayList;
        mContentType = contentType;
        mMovieId = movieId;
    }

    private String buildApiUrl(String movieId, String postfix) {
        String BASE_URL         = "http://api.themoviedb.org/3/movie/";
        String API_KEY_PARAM    = "api_key";
        Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendPath(postfix)
                .appendQueryParameter(API_KEY_PARAM, MovieDB.API_KEY)
                        .build();

        return buildUri.toString();
    }
    private TitleContentContainer[] getTrailerDataFromJSON(String resultJSONStr) throws JSONException {

        final String OMDB_RESULTS = "results";
        final String OMDB_SITE = "site";
        final String OMDB_TITLE = "name";
        final String OMDB_URL_KEY = "key";
        final String VIDEO_BASE_URL = "http://youtube.com/watch?v";

        JSONObject resultJSON = new JSONObject(resultJSONStr);
        JSONArray contentJSONArray = resultJSON.getJSONArray(OMDB_RESULTS);

        TitleContentContainer[] moviesArray = new TitleContentContainer[contentJSONArray.length()];

        for (int i = 0; i < contentJSONArray.length(); i++) {
            JSONObject movieJSON = contentJSONArray.getJSONObject(i);

            String trailerSite = movieJSON.getString(OMDB_SITE);
            if (!trailerSite.equals(Constants.TRAILER_VIDEO_SITE_NAME)) continue;
            String trailerTitle = movieJSON.getString(OMDB_TITLE);
            String trailerVideoKey = movieJSON.getString(OMDB_URL_KEY);
            String trailerUrl = VIDEO_BASE_URL + trailerVideoKey;


            moviesArray[i] = new TitleContentContainer(
                    FetchDetailsContentTask.TYPE_TRAILER,
                    trailerTitle,
                    trailerUrl
                    );
        }

        return moviesArray;
    }
    private TitleContentContainer[] getReviewDataFromJSON(String resultJSONStr) throws JSONException {

        final String OMDB_RESULTS = "results";
        final String OMDB_AUTHOR = "author";
        final String OMDB_CONTENT = "content";

        JSONObject resultJSON = new JSONObject(resultJSONStr);
        JSONArray contentJSONArray = resultJSON.getJSONArray(OMDB_RESULTS);

        TitleContentContainer[] moviesArray = new TitleContentContainer[contentJSONArray.length()];

        for (int i = 0; i < contentJSONArray.length(); i++) {
            JSONObject movieJSON = contentJSONArray.getJSONObject(i);

            String reviewAuthor = movieJSON.getString(OMDB_AUTHOR);
            String reviewContent = movieJSON.getString(OMDB_CONTENT);



            moviesArray[i] = new TitleContentContainer(
                    FetchDetailsContentTask.TYPE_REVIEW,
                    reviewAuthor,
                    reviewContent
            );
        }

        return moviesArray;
    }
    @Override
    protected TitleContentContainer[] doInBackground(Void... params) {
        String contentJSONStr = null;

        // Will contain the raw JSON response as a string.

        OkHttpClient client = new OkHttpClient();

        String call_type = "";
        if (mContentType == FetchDetailsContentTask.TYPE_REVIEW) {
            call_type = "review";
        } else if (mContentType == FetchDetailsContentTask.TYPE_TRAILER) {
            call_type = "trailer";
        }
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
            switch (mContentType) {
                case FetchDetailsContentTask.TYPE_REVIEW:
                    return getReviewDataFromJSON(contentJSONStr);
                case FetchDetailsContentTask.TYPE_TRAILER:
                    return getTrailerDataFromJSON(contentJSONStr);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getMessage(),e);
            e.printStackTrace();
        }

        //if the getting or parsing json data fails or unknown type
        return null;
    }

    @Override
    protected void onPostExecute(TitleContentContainer[] result) {
        super.onPostExecute(result);
        mData.clear();
        for (int i = 0; i < result.length; i++) {
            //Log.v(LOG_TAG, result[i].getTitle());
            mData.add(result[i]);
        }
        switch (mContentType) {
            case FetchDetailsContentTask.TYPE_REVIEW:
                mResultListener.onReviewsFetched();
                break;
            case FetchDetailsContentTask.TYPE_TRAILER:
                mResultListener.onTrailersFetched();
                break;
        }
    }
}

