package cvv.udacity.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private final static int VIEW_TYPE_TODAY = 0;
    private final static int VIEW_TYPE_FUTURE_DAY = 1;
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    private static class ViewHolder {
        private final TextView dateTextview;
        private final TextView forecastTextview;
        private final TextView highTextview;
        private final TextView lowTextview;
        private final ImageView icon;

        public ViewHolder(View view) {
            dateTextview = (TextView) view.findViewById(R.id.list_item_date_textview);
            forecastTextview = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTextview = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTextview = (TextView) view.findViewById(R.id.list_item_low_textview);
            icon = (ImageView) view.findViewById(R.id.list_item_icon);
        }
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        } else if (viewType == VIEW_TYPE_FUTURE_DAY) {
            layoutId = R.layout.list_item_forecast;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        view.setTag(R.id.view_holder, new ViewHolder(view));
        return view;
    }

    /*
            This is where we fill-in the views with the contents of the cursor.
         */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder viewHolder = (ViewHolder) view.getTag(R.id.view_holder);
        int weatherId = cursor.getInt(COL_WEATHER_CONDITION_ID);
        int viewType = getItemViewType(cursor.getPosition()) ;
        if(viewType == VIEW_TYPE_TODAY ) {
            viewHolder.icon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        }else if(viewType == VIEW_TYPE_FUTURE_DAY){
            viewHolder.icon.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        }
        long date = cursor.getLong(COL_WEATHER_DATE);
        String prettyDate = Utility.getFriendlyDayString(mContext, date);
        viewHolder.dateTextview.setText(prettyDate);

        String desc = cursor.getString(COL_WEATHER_DESC);
        viewHolder.forecastTextview.setText(desc);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(COL_WEATHER_MAX_TEMP);
        viewHolder.highTextview.setText(Utility.formatTemperature(mContext, high, isMetric));
        // Read low temperature from cursor
        double low = cursor.getDouble(COL_WEATHER_MIN_TEMP);
        viewHolder.lowTextview.setText(Utility.formatTemperature(mContext, low, isMetric));
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}