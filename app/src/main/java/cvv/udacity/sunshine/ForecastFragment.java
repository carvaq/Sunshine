package cvv.udacity.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import cvv.udacity.sunshine.data.WeatherContract;
import cvv.udacity.sunshine.sync.SunshineSyncAdapter;

/**
 * Created by Caro Vaquero
 * Date: 11.06.2016
 * Project: Sunshine
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = ForecastFragment.class.getSimpleName();
    private static final int LOADER_ID = 13254;
    private static final String CURRENT_POSITION = "current_pos";
    private ForecastAdapter mAdapter;
    private ListView mListView;
    private TextView mEmptyView;
    private int mCurrentPosition;

    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;


    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    private boolean mUseTodayLayout;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (ListView) rootView.findViewById(R.id.list_forecast);
        mEmptyView = (TextView) rootView.findViewById(R.id.no_data);
        mListView.setEmptyView(mEmptyView);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(CURRENT_POSITION);
        }

        mAdapter = new ForecastAdapter(getActivity(), null, 0);
        mAdapter.setUseTodayLayout(mUseTodayLayout);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(ForecastAdapter.COL_WEATHER_DATE));
                    if (getActivity() instanceof Callback) {
                        ((Callback) getActivity()).onItemSelected(uri);
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    /*    if (item.getItemId() == R.id.action_refresh) {
            updateWeather();
            return true;
        } else */
        if (item.getItemId() == R.id.action_location) {
            openPreferredLocationInMap();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void openPreferredLocationInMap() {
        if (null != mAdapter) {
            Cursor c = mAdapter.getCursor();
            if (null != c && c.moveToNext()) {
                String posLat = c.getString(ForecastAdapter.COL_COORD_LAT);
                String posLong = c.getString(ForecastAdapter.COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(), weatherForLocationUri,
                FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            mAdapter.swapCursor(data);
            if (mCurrentPosition != ListView.INVALID_POSITION) {
                mListView.smoothScrollToPosition(mCurrentPosition);
            }
        }
    }

    private void updateEmptyView(@SunshineSyncAdapter.LocationStatus int locationStatus) {
        Log.d(TAG, "Updating empty view with " + locationStatus);
        if (!Utility.isConnected(getActivity())) {
            mEmptyView.setText(R.string.empty_forecast_list_no_connection);
        } else if (locationStatus == SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN) {
            mEmptyView.setText(R.string.empty_forecast_list_server_down);
        } else if (locationStatus == SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID) {
            mEmptyView.setText(R.string.empty_forecast_list_server_error);
        } else if (locationStatus == SunshineSyncAdapter.LOCATION_STATUS_INVALID) {
            mEmptyView.setText(R.string.empty_forecast_list_invalid_location);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_POSITION, mListView.getSelectedItemPosition());
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mAdapter != null) {
            mAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (getString(R.string.loc_status).equals(s)) {
            updateEmptyView(Utility.getLocationStatus(getActivity()));
        }
    }
}
