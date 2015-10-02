package cz.lukassladky.popularmovies;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import cz.lukassladky.popularmovies.data.MoviesContract;

/**
 * Created by admin on 26.8.2015.
 */
public class Movie implements Parcelable{

    private String _id;
    private String api_id;
    private String posterUrl;
    private String title;
    private String plotOverview;
    private String userRating;
    private String releaseYear;
    private boolean inFavorites;

    public Movie(String api_id, String title, String posterUrl, String plotOverview, String userRating, String releaseDate) {
        this._id = null;
        this.api_id = api_id;
        this.title = title;
        this.posterUrl = getCleanUrl(posterUrl);
        this.plotOverview = plotOverview;
        this.userRating = userRating;
        this.inFavorites = false;

        String dateSeparated[] = releaseDate.split("-");
        this.releaseYear = dateSeparated[0]; //only year
    }

    public Movie(String _id, String api_id, String title, String posterUrl, String plotOverview, String userRating, String releaseDate, boolean inFavorites) {
        this._id = _id;
        this.api_id = api_id;
        this.title = title;
        this.posterUrl = getCleanUrl(posterUrl);
        this.plotOverview = plotOverview;
        this.userRating = userRating;
        this.inFavorites = inFavorites;

        String dateSeparated[] = releaseDate.split("-");
        this.releaseYear = dateSeparated[0]; //only year
    }

    private String getCleanUrl(String url) {
        //prevent app from crashing if url is empty
        Uri uri = Uri.parse(url);
        String newUrl = uri.toString();
        if (newUrl.isEmpty()) {
            return null;
        } else {
            return newUrl;
        }
    }
    public String get__Id() {return _id;}
    public String getApi_id() {return api_id;}
    public String getPosterUrl() {return posterUrl;}
    public String getTitle() {return title;}
    public String getPlotOverview() {return plotOverview;}
    public String getYear() {return releaseYear;}
    public String getUserRating() {return userRating;}

    public ContentValues getContentValues(boolean markFavourite) {
        ContentValues cv = new ContentValues();
        cv.put(MoviesContract.MoviesEntry.COLUMN_THEMOVIEDB_ID, this.api_id);
        cv.put(MoviesContract.MoviesEntry.COLUMN_TITLE,this.title);
        cv.put(MoviesContract.MoviesEntry.COLUMN_POSTER_URL,this.posterUrl);
        cv.put(MoviesContract.MoviesEntry.COLUMN_DESCRIPTION,this.plotOverview);
        cv.put(MoviesContract.MoviesEntry.COLUMN_RATING,this.userRating);
        cv.put(MoviesContract.MoviesEntry.COLUMN_RELEASE,this.releaseYear);
        cv.put(MoviesContract.MoviesEntry.COLUMN_IS_FAVOURITE, markFavourite);
        return cv;
    }

    //parcelable interface part

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this._id);
        dest.writeString(this.api_id);
        dest.writeString(this.posterUrl);
        dest.writeString(this.title);
        dest.writeString(this.plotOverview);
        dest.writeString(this.userRating);
        dest.writeString(this.releaseYear);
    }

    private Movie(Parcel in) {
        this._id = in.readString();
        this.api_id = in.readString();
        this.posterUrl = in.readString();
        this.title = in.readString();
        this.plotOverview = in.readString();
        this.userRating = in.readString();
        this.releaseYear = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
