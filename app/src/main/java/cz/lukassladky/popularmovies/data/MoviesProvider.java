package cz.lukassladky.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by admin on 25.9.2015.
 */
public class MoviesProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int MOVIE_ID = 101;
    static final int MOVIE_WITH_OMDB_ID = 102;
    static final int REVIEWS = 200;
    static final int REVIEWS_WITH_MOVIE = 201;
    static final int TRAILERS = 300;
    static final int TRAILERS_WITH_MOVIE = 301;

    /**
     * Helper method that creates UriMatcher that match path with corresponding code
     * @return
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority,MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority,MoviesContract.PATH_MOVIES + "/id/#/", MOVIE_ID);
        matcher.addURI(authority,MoviesContract.PATH_MOVIES + "/#/", MOVIE_WITH_OMDB_ID);
        matcher.addURI(authority,MoviesContract.PATH_REVIEWS, REVIEWS);
        matcher.addURI(authority,MoviesContract.PATH_REVIEWS + "/#/", REVIEWS_WITH_MOVIE);
        matcher.addURI(authority,MoviesContract.PATH_TRAILERS, TRAILERS);
        matcher.addURI(authority,MoviesContract.PATH_TRAILERS + "/#/", TRAILERS_WITH_MOVIE);

        return matcher;
    }

    /**
     * Creates db helper
     * @return
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        long id = MoviesContract.MoviesEntry.getIdFromUri(uri);

        String selection = MoviesContract.MoviesEntry._ID + " = ?";
        String[] selectionArgs = new String[] {Long.toString(id)};

        return mOpenHelper.getReadableDatabase().query(
                MoviesContract.MoviesEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getMovieWithOMDBId(Uri uri, String[] projection, String sortOrder) {
        long id = MoviesContract.MoviesEntry.getIdFromUri(uri);

        String selection = MoviesContract.MoviesEntry.COLUMN_THEMOVIEDB_ID + " = ?";
        String[] selectionArgs = new String[] {Long.toString(id)};

        return mOpenHelper.getReadableDatabase().query(
                MoviesContract.MoviesEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getReviewsByMovieId(Uri uri, String[] projection, String sortOrder) {
        long movie_id = MoviesContract.ReviewsEntry.getMovieIdFromUri(uri);

        String selection = MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY + " = ?";
        String[] selectionArgs = new String[] {Long.toString(movie_id)};

        return mOpenHelper.getReadableDatabase().query(
                MoviesContract.ReviewsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }



    private Cursor getTrailersByMovieId(Uri uri, String[] projection, String sortOrder) {
        long movie_id = MoviesContract.TrailersEntry.getMovieIdFromUri(uri);

        String selection = MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY + " = ?";
        String[] selectionArgs = new String[] {Long.toString(movie_id)};

        return mOpenHelper.getReadableDatabase().query(
                MoviesContract.TrailersEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case MOVIE_WITH_OMDB_ID:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case REVIEWS:
            case REVIEWS_WITH_MOVIE:
                return MoviesContract.ReviewsEntry.CONTENT_TYPE;
            case TRAILERS:
            case TRAILERS_WITH_MOVIE:
                return MoviesContract.TrailersEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor returnedCursor;
        switch (sUriMatcher.match(uri)) {
                // "movies"
            case MOVIES:
                returnedCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // "movies/id/#"
            case MOVIE_ID:
                returnedCursor = getMovieById(uri, projection, sortOrder);
                break;
            // "movies/#"
            case MOVIE_WITH_OMDB_ID:
                returnedCursor = getMovieWithOMDBId(uri, projection, sortOrder);
                break;
            // "reviews"
            case REVIEWS:
                returnedCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.ReviewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // "reviews/#"
            case REVIEWS_WITH_MOVIE:
                returnedCursor = getReviewsByMovieId(uri,projection,sortOrder);
                break;

            // "trailers"
            case TRAILERS:
                returnedCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.ReviewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // "trailers/#"
            case TRAILERS_WITH_MOVIE:
                returnedCursor = getTrailersByMovieId(uri,projection,sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        returnedCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnedCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.MoviesEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS: {
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.ReviewsEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILERS: {
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.TrailersEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(
                        MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                rowsDeleted = db.delete(
                        MoviesContract.ReviewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILERS:
                rowsDeleted = db.delete(
                        MoviesContract.TrailersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(
                        MoviesContract.MoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEWS:
                rowsUpdated = db.update(
                        MoviesContract.ReviewsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRAILERS:
                rowsUpdated = db.update(
                        MoviesContract.TrailersEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
