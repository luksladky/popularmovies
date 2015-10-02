package cz.lukassladky.popularmovies;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.lukassladky.popularmovies.data.MoviesContract;
import cz.lukassladky.popularmovies.utils.Constants;
import cz.lukassladky.popularmovies.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements FetchDetailsContentTask.FetchDetailContentsListener{
    Movie mMovie;
    ArrayList<TitleContentContainer> mReviewsData;
    ArrayList<TitleContentContainer> mTrailersData;

    Boolean markedFavorite;

    public static final String DETAIL_MOVIE = "MOVIE_D";

    public DetailFragment() {
    }

    @Bind(R.id.detail_movie_title) TextView movie_title_textview;
    @Bind(R.id.detail_release_year) TextView release_year_textview;
    @Bind(R.id.detail_user_rating) TextView user_rating_textview;
    @Bind(R.id.detail_overview) TextView overview_textview;
    @Bind(R.id.detail_poster_image) ImageView detail_poster_imageview;
    @Bind(R.id.mark_favorite_button) Button mark_favourite_button;


    private void setFavoriteButtonStyle() {
        if (markedFavorite) {
            mark_favourite_button.setText(R.string.detail_favorite_button_marked_text);
        } else {
            mark_favourite_button.setText(R.string.detail_favorite_button_unmarked_text);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        markedFavorite = false;
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovie = arguments.getParcelable(Constants.PARC_MOVIES_KEY);
            new FavoriteCheck().execute();

            //set movie details
            if (mMovie != null) {
                movie_title_textview.setText(mMovie.getTitle());
                release_year_textview.setText(mMovie.getYear());
                user_rating_textview.setText(mMovie.getUserRating() + "/10");
                overview_textview.setText(mMovie.getPlotOverview());
                Picasso.with(rootView.getContext())
                        .load(mMovie.getPosterUrl())
                        .error(R.drawable.error)
                        .placeholder(R.drawable.placeholder)
                        .into(detail_poster_imageview);
            }
        }

        mark_favourite_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markedFavorite) {
                    //clicked remove from favs
                    //delete favorite movie from database
                    getActivity().getContentResolver().delete(
                            MoviesContract.MoviesEntry.CONTENT_URI,
                            MoviesContract.MoviesEntry.COLUMN_THEMOVIEDB_ID + " = ?",
                            new String[] {mMovie.getApi_id()}
                    );
                } else {
                    //clicked add to favs
                    //add new fav movie to db
                    getActivity().getContentResolver().insert(
                            MoviesContract.MoviesEntry.CONTENT_URI,
                            mMovie.getContentValues(true));
                }
                markedFavorite = !markedFavorite;
                setFavoriteButtonStyle();
            }
        });

        //fetch reviews and trailers
        if (Utility.isNetworkAvailable(getActivity())) {
            fetchNetworkContent();
        }
        return rootView;
    }
        /**
     * Helper method used to fetch reviews and trailers
     */
    private void fetchNetworkContent() {

          /*  mReviewsData = new ArrayList<>();
            mTrailersData = new ArrayList<>();
            new FetchDetailsContentTask(this,
                    mMovie.getApi_id(),
                    mReviewsData,
                    FetchDetailsContentTask.TYPE_REVIEW)
                    .execute();
            new FetchDetailsContentTask(this,
                    mMovie.getApi_id(),
                    mTrailersData,
                    FetchDetailsContentTask.TYPE_TRAILER)
                    .execute();*/

    }

    @Override
    public void onReviewsFetched() {
        Toast.makeText(getActivity(), "reviews fetched", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTrailersFetched() {
        Toast.makeText(getActivity(), "trailers fetched", Toast.LENGTH_SHORT).show();
    }

    public class FavoriteCheck extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            String[] projection = new String[] {MoviesContract.MoviesEntry.COLUMN_IS_FAVOURITE};
            Cursor cursor = getActivity().getContentResolver().query(
                    MoviesContract.MoviesEntry.buildMovieWithOMDBIdUri(mMovie.getApi_id()),
                    projection,
                    null,
                    null,
                    null);
            Boolean isFavorite = false;
            if (cursor.moveToFirst()) {
                 isFavorite = cursor.getInt(0) > 0;
            }
            return isFavorite;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            markedFavorite = aBoolean;
            setFavoriteButtonStyle();
        }
    }

}
