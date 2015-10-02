package cz.lukassladky.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

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

    public static String getPreferredSortingOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_posters_sort_popularity));

    }
}
