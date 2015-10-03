package cz.lukassladky.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by admin on 3.10.2015.
 */
public class ReviewsAdapter extends BaseAdapter {
    ArrayList<Review> mReviews;
    Context mContext;

    private class ViewHolder {
        TextView author_textview;
        TextView review_text_textview;
    }

    public ReviewsAdapter(Context context, ArrayList<Review> trailers) {
        mContext = context;
        mReviews = trailers;
    }

    @Override
    public int getCount() {
        return mReviews.size();
    }

    @Override
    public Object getItem(int position) {
        return mReviews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            if (mContext == null) return null;
            LayoutInflater inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.review_list_item,parent,false);

            holder = new ViewHolder();
            holder.author_textview = (TextView) convertView
                    .findViewById(R.id.review_author_textview);
            holder.review_text_textview = (TextView) convertView
                    .findViewById(R.id.review_text_textview);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.author_textview.setText(mReviews.get(position).title);
        holder.review_text_textview.setText(mReviews.get(position).content);

        return convertView;
    }
}
