package cz.lukassladky.popularmovies;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 26.8.2015.
 */
public class Movie implements Parcelable{

    private String posterUrl;
    private String title;
    private String plotOverview;
    private String userRating;
    private String releaseYear;

    public Movie(String title, String posterUrl) {
        this.title = title;
        this.posterUrl = getCleanUrl(posterUrl);
    }
    public Movie(String title, String posterUrl, String plotOverview, String userRating, String releaseDate) {
        this.title = title;
        this.posterUrl = getCleanUrl(posterUrl);
        this.plotOverview = plotOverview;
        this.userRating = userRating;

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

    public String getPosterUrl() {
        return posterUrl;
    }
    public String getTitle() {
        return title;
    }
    public String getPlotOverview() {return plotOverview;}
    public String getYear() {return releaseYear;}
    public String getUserRating() {return userRating;}

    //parcelable interface part
    public Movie(Parcel in) {
        String[] values = new String[5];
        in.readStringArray(values);

        this.title          = values[0];
        this.posterUrl      = values[1];
        this.plotOverview   = values[2];
        this.userRating     = values[3];
        this.releaseYear    = values[4];
    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.title,
                this.posterUrl,
                this.plotOverview,
                this.userRating,
                this.releaseYear});
    }
}
