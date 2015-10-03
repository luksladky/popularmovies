package cz.lukassladky.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.lukassladky.popularmovies.data.MoviesContract;
import cz.lukassladky.popularmovies.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements FetchTrailersTask.FetchTrailersListener, FetchReviewsTask.FetchReviewsListener{
    Movie mMovie;
    ArrayList<Review> mReviewsData;
    ArrayList<Trailer> mTrailersData;

    TrailersAdapter mTrailersAdapter;
    ReviewsAdapter mReviewsAdapter;
    ShareActionProvider mShareActionProvider;
    FavoriteCheckTask mFavoriteCheckTask;
    FetchTrailersTask mFetchTrailersTask;
    FetchReviewsTask mFetchReviewsTask;

    Boolean markedFavorite;

    public static final String DETAIL_MOVIE = "MOVIE_D";

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Bind(R.id.detail_movie_title) TextView movie_title_textview;
    @Bind(R.id.detail_release_year) TextView release_year_textview;
    @Bind(R.id.detail_user_rating) TextView user_rating_textview;
    @Bind(R.id.detail_overview) TextView overview_textview;
    @Bind(R.id.detail_poster_image) ImageView detail_poster_imageview;
    @Bind(R.id.mark_favorite_button) Button mark_favourite_button;
    @Bind(R.id.list_view_trailers) ListView trailers_listview;
    @Bind(R.id.list_view_reviews) ListView reviews_listview;


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
            mMovie = arguments.getParcelable(DetailFragment.DETAIL_MOVIE);

            //set movie details
            if (mMovie != null) {
                mFavoriteCheckTask = new FavoriteCheckTask();
                mFavoriteCheckTask.execute();

                movie_title_textview.setText(mMovie.getTitle());
                release_year_textview.setText(mMovie.getYear());
                user_rating_textview.setText(mMovie.getUserRating());
                overview_textview.setText(mMovie.getPlotOverview());
                Picasso.with(rootView.getContext())
                        .load(mMovie.getPosterUrl())
                        .error(R.drawable.error)
                        .placeholder(R.drawable.placeholder)
                        .into(detail_poster_imageview);
                //fetch reviews and trailers
                if (Utility.isNetworkAvailable(getActivity())) {
                    fetchNetworkContent();
                }
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
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mTrailersData != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    private Intent createShareTrailerIntent() {
        if ((mMovie != null) && mTrailersData.size() > 0) {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, mMovie.getTitle() + " trailer");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + mTrailersData.get(0).video_key);
            return shareIntent;
        } else {
            return null;
        }
    }

    /**
     * Helper method used to fetch reviews and trailers
     */
    private void fetchNetworkContent() {

        mReviewsData = new ArrayList<>();
        mTrailersData = new ArrayList<>();
        mFetchTrailersTask = new FetchTrailersTask(this,
                mMovie.getApi_id());
        mFetchReviewsTask = new FetchReviewsTask(this,
                mMovie.getApi_id());
        mFetchTrailersTask.execute();
        mFetchReviewsTask.execute();


    }

    @Override
    public void onReviewsFetched(ArrayList<Review> reviewsData) {
        mReviewsData = reviewsData;
        mReviewsAdapter = new ReviewsAdapter(getActivity(),mReviewsData);
        reviews_listview.setAdapter(mReviewsAdapter);
        reviews_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Review item = (Review) mReviewsAdapter.getItem(position);
                if (item != null) {
                    Intent intent=new Intent(Intent.ACTION_VIEW,
                            Uri.parse(item.url));
                    startActivity(intent);
                }
            }
        });
        Utility.justifyListViewHeightBasedOnChildren(reviews_listview);
    }

    @Override
    public void onTrailersFetched(ArrayList<Trailer> trailersData) {
        mTrailersData = trailersData;
        mTrailersAdapter = new TrailersAdapter(getActivity(),mTrailersData);
        trailers_listview.setAdapter(mTrailersAdapter);
        trailers_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trailer trailer = (Trailer) mTrailersAdapter.getItem(position);
                if (trailer != null) {
                    watchYoutubeVideo(trailer.video_key);
                }
            }
        });
        Utility.justifyListViewHeightBasedOnChildren(trailers_listview);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }


    private void watchYoutubeVideo(String id){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent=new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v="+id));
            startActivity(intent);
        }
    }

    public class FavoriteCheckTask extends AsyncTask<Void, Void, Boolean> {

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
            if (cursor != null && cursor.moveToFirst()) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFavoriteCheckTask != null) {
            mFavoriteCheckTask.cancel(true);
        }
        if (mFetchReviewsTask != null) {
            mFetchReviewsTask.cancel(true);
        }
        if (mFetchTrailersTask != null) {
            mFetchTrailersTask.cancel(true);
        }
    }
}
