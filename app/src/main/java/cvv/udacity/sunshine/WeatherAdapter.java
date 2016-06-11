package cvv.udacity.sunshine;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Carla on 05.06.2016.
 */

public class WeatherAdapter extends ArrayAdapter<String> {

    private List<String> mListItems;
    private LayoutInflater mLayoutInflater;


    public WeatherAdapter(Context context, @LayoutRes int resource, @IdRes int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);

        mLayoutInflater = LayoutInflater.from(context);
        mListItems = objects;
    }

    @Override
    public int getCount() {
        return mListItems.size();
    }

    @Override
    public String getItem(int position) {
        return mListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }


    private static class ViewHolder {
        private TextView mTextView;

        ViewHolder(View itemView) {
            mTextView = (TextView) itemView.findViewById(R.id.list_item_forecast_textview);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag(R.id.view_holder);
        } else {
            convertView = mLayoutInflater.inflate(R.layout.list_item_forcast, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(R.id.view_holder, convertView);
        }

        String item = mListItems.get(position);

        viewHolder.mTextView.setTag(item);
        return convertView;
    }
}
