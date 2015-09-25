package cz.lukassladky.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.lukassladky.popularmovies.utils.Constants;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {


    public DetailActivityFragment() {
    }

    @Bind(R.id.detail_movie_title) TextView movie_title_textview;
    @Bind(R.id.detail_release_year) TextView release_year_textview;
    @Bind(R.id.detail_user_rating) TextView user_rating_textview;
    @Bind(R.id.detail_overview) TextView overview_textview;
    @Bind(R.id.detail_poster_image) ImageView detail_poster_imageview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        if (intent != null) {
            Movie movie = intent.getParcelableExtra(Constants.parcMovieObjKey);
            movie_title_textview.setText(movie.getTitle());
            release_year_textview.setText(movie.getYear());
            user_rating_textview.setText(movie.getUserRating() + "/10");
            overview_textview.setText(movie.getPlotOverview());
            Picasso.with(rootView.getContext())
                    .load(movie.getPosterUrl())
                    .error(R.drawable.error)
                    .placeholder(R.drawable.placeholder)
                    .into(detail_poster_imageview);
        }


        return rootView;
    }
}
