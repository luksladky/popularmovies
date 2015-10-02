package cz.lukassladky.popularmovies;

import android.content.Intent;
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
public class DetailActivityFragment extends Fragment implements FetchDetailsContentTask.FetchDetailContentsListener{
    Movie mMovie;
    ArrayList<TitleContentContainer> mReviewsData;
    ArrayList<TitleContentContainer> mTrailersData;

    Boolean markedFavorite;

    public DetailActivityFragment() {
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
        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        if (intent != null) {
            mMovie = intent.getParcelableExtra(Constants.parcMovieObjKey);
            movie_title_textview.setText(mMovie.getTitle());
            release_year_textview.setText(mMovie.getYear());
            user_rating_textview.setText(mMovie.getUserRating() + "/10");
            overview_textview.setText(mMovie.getPlotOverview());
            Picasso.with(rootView.getContext())
                    .load(mMovie.getPosterUrl())
                    .error(R.drawable.error)
                    .placeholder(R.drawable.placeholder)
                    .into(detail_poster_imageview);

            new FavoriteCheck().execute();


            mark_favourite_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (markedFavorite) {
                        getActivity().getContentResolver().delete(
                                MoviesContract.MoviesEntry.CONTENT_URI,
                                MoviesContract.MoviesEntry.COLUMN_THEMOVIEDB_ID + " = ?",
                                new String[] {mMovie.getApi_id()}
                        );
                    } else {
                        getActivity().getContentResolver().insert(
                                MoviesContract.MoviesEntry.CONTENT_URI,
                                mMovie.getContentValues(true));
                    }
                    markedFavorite = !markedFavorite;
                    setFavoriteButtonStyle();
                }
            });


            if (Utility.isNetworkAvailable(getActivity())) {
                mReviewsData = new ArrayList<>();
                mTrailersData = new ArrayList<>();
                new FetchDetailsContentTask(this,
                        mMovie.getApi_id(),
                        mReviewsData,
                        Constants.TYPE_REVIEW)
                    .execute();
                new FetchDetailsContentTask(this,
                        mMovie.getApi_id(),
                        mTrailersData,
                        Constants.TYPE_TRAILER)
                    .execute();
            }

        }
        return rootView;
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
