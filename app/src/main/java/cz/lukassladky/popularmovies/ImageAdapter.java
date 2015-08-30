package cz.lukassladky.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by admin on 26.8.2015.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Movie> mMovies;

    public ImageAdapter(Context c, ArrayList<Movie> data) {
        this.mContext = c;
        this.mMovies = data;
    }

    public void add(Movie item) {
            mMovies.add(item);
    }

    public void clear() {
        mMovies.clear();
        notifyDataSetChanged();
    }

    public int getCount() {
        return mMovies.size();
    }

    public Movie getItem(int position) {
        return mMovies.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }

        //url is checked in Movie class
        String imageUrl = mMovies.get(position).getPosterUrl();

        Picasso.with(mContext)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(imageView);



        return imageView;
    }



};
