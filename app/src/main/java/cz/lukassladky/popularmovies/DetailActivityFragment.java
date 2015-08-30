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

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if (intent != null) {
            //String mMovieTitle = intent.getStringExtra(Intent.EXTRA_TITLE);
            Movie movie = intent.getParcelableExtra(Constants.parcMovieObjKey);
            ((TextView) rootView.findViewById(R.id.detail_movie_title)).setText(movie.getTitle());
            ((TextView) rootView.findViewById(R.id.detail_release_year)).setText(movie.getYear());
            ((TextView) rootView.findViewById(R.id.detail_user_rating)).setText(movie.getUserRating()+"/10");
            ((TextView) rootView.findViewById(R.id.detail_overview)).setText(movie.getPlotOverview());


            ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_poster_image);
            Picasso.with(rootView.getContext())
                    .load(movie.getPosterUrl())
                    .error(R.drawable.error)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
        }


        return rootView;
    }
}
