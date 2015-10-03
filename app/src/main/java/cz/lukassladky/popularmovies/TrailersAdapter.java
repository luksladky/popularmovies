package cz.lukassladky.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by admin on 2.10.2015.
 */
public class TrailersAdapter extends BaseAdapter {
    ArrayList<Trailer> mTrailers;
    Context mContext;

    private class ViewHolder {
        TextView title_text_view;
    }

    public TrailersAdapter(Context context, ArrayList<Trailer> trailers) {
        mContext = context;
        mTrailers = trailers;
    }

    @Override
    public int getCount() {
        return mTrailers.size();
    }

    @Override
    public Object getItem(int position) {

        return mTrailers.get(position);
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
            convertView = inflater.inflate(R.layout.trailer_list_item,parent,false);

            holder = new ViewHolder();
            holder.title_text_view = (TextView) convertView
                    .findViewById(R.id.trailer_name_textview);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title_text_view.setText(mTrailers.get(position).title);

        return convertView;
    }
}
