package cz.lukassladky.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cz.lukassladky.popularmovies.data.MoviesContract.MoviesEntry;
import cz.lukassladky.popularmovies.data.MoviesContract.ReviewsEntry;
import cz.lukassladky.popularmovies.data.MoviesContract.TrailersEntry;

/**
 * Created by admin on 25.9.2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies.db";



    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME +  " (" +

                MoviesEntry._ID + " INTEGER PRIMARY KEY, " +
                MoviesEntry.COLUMN_THEMOVIEDB_ID + " STRING UNIQUE NOT NULL, " +
                MoviesEntry.COLUMN_TITLE + " STRING NOT NULL, " +
                MoviesEntry.COLUMN_POSTER_URL + " STRING, " +
                MoviesEntry.COLUMN_DESCRIPTION + " STRING, " +
                MoviesEntry.COLUMN_RATING + " STRING, " +
                MoviesEntry.COLUMN_RELEASE + " STRING, " +
                MoviesEntry.COLUMN_IS_FAVOURITE + " BOOLEAN, " +

                "UNIQUE (" + MoviesEntry.COLUMN_THEMOVIEDB_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewsEntry.TABLE_NAME +  " (" +

                ReviewsEntry._ID + " INTEGER PRIMARY KEY, " +
                ReviewsEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                ReviewsEntry.COLUMN_AUTHOR + " STRING NOT NULL, " +
                ReviewsEntry.COLUMN_CONTENT + " STRING, " +

                "FOREIGN KEY (" + ReviewsEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MoviesEntry.TABLE_NAME + " (" + MoviesEntry._ID + ") " +
                ");";
        final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + TrailersEntry.TABLE_NAME +  " (" +

                TrailersEntry._ID + " INTEGER PRIMARY KEY, " +
                TrailersEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                TrailersEntry.COLUMN_URL + " STRING NOT NULL, " +
                TrailersEntry.COLUMN_TITLE + " STRING, " +

                "FOREIGN KEY (" + TrailersEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MoviesEntry.TABLE_NAME + " (" + MoviesEntry._ID + ") " +
                ");";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_REVIEWS_TABLE);
        db.execSQL(SQL_CREATE_TRAILERS_TABLE);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TrailersEntry.TABLE_NAME);
        onCreate(db);
    }
}
