package cz.lukassladky.popularmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import cz.lukassladky.popularmovies.R;

/**
 * Created by admin on 29.8.2015.
 */
public class Utility {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static String getOMDBApiKey(Context context) {
        return context.getResources().getString(R.string.omdb_api_key);
    }

}
