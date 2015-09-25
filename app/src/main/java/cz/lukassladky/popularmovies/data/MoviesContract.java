package cz.lukassladky.popularmovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by admin on 25.9.2015.
 */
public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "cz.lukassladky.pupularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES   = "movies";
    public static final String PATH_REVIEWS  = "reviews";
    public static final String PATH_TRAILERS = "trailers";

    public static class MoviesEntry implements BaseColumns {
        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_THEMOVIEDB_KEY = "db_api_key";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_KEY = "poster_key";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE = "released";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_IS_FAVOURITE = "is_favourite";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static Uri buildMovieUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath(Long.toString(id)).build();
        }

        public static long getIdFromUri(Uri uri) {
            return Long.parseLong(uri.getLastPathSegment());
        }

    }

    public static class ReviewsEntry implements BaseColumns {
        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_MOVIE_KEY = "movie_key";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        public static Uri buildReviewUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath(Long.toString(id)).build();
        }

        public static long getIdFromUri(Uri uri) {
            return Long.parseLong(uri.getLastPathSegment());
        }

        public static long getMovieIdFromUri(Uri uri) {
            return Long.parseLong(uri.getLastPathSegment());
        }

    }
    public static class TrailersEntry implements BaseColumns {
        public static final String TABLE_NAME = "trailers";

        public static final String COLUMN_MOVIE_KEY = "movie_key";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_URL = "url";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        public static Uri buildTrailerUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath(Long.toString(id)).build();
        }

        public static long getIdFromUri(Uri uri) {
            return Long.parseLong(uri.getLastPathSegment());
        }

        public static long getMovieIdFromUri(Uri uri) {
            return Long.parseLong(uri.getLastPathSegment());
        }

    }

}
