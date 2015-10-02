package cz.lukassladky.popularmovies;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by admin on 26.9.2015.
 */
public class Popularmovies extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}


