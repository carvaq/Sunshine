package cvv.udacity.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Carla on 11.06.2016.
 */
public class ForecastFragment extends Fragment {

    private static final String TAG = ForecastFragment.class.getSimpleName();
    private ArrayAdapter<String> mAdapter;
    private SharedPreferences mSharedPreferences;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        List<String> weekForecast = new ArrayList<>();

        ListView listView = (ListView) rootView.findViewById(R.id.list_forecast);

        mAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forcast, R.id.list_item_forecast_textview, weekForecast);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String text = mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            updateWeather();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateWeather() {
        new FetchWeatherTask().execute(
                mSharedPreferences.getString(getString(R.string.pref_location_key),
                        getString(R.string.pref_location_default)));
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String TAG = FetchWeatherTask.class.getSimpleName();

        private static final String API_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        private static final String QUERY_API_ID_KEY = "APPID";
        private static final String QUERY_PARAM_UNIT_KEY = "units";
        private static final String QUERY_PARAM_MODE_KEY = "mode";
        private static final String QUERY_PARAM_COUNT_KEY = "cnt";
        private static final String QUERY_PARAM_POSTAL_KEY = "q";

        private static final String QUERY_API_ID_VALUE = "f031283db3ca09aa1d2baa20718fa067";
        private static final String QUERY_PARAM_MODE_VALUE = "json";
        private static final String QUERY_PARAM_UNIT_VALUE = "metric";


        @Override
        protected String[] doInBackground(String... params) {
            if (params != null) {
                return getForecastJson(params[0]);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            if (s != null) {
                List<String> weekForecast = new ArrayList<>(Arrays.asList(s));
                mAdapter.clear();
                mAdapter.addAll(weekForecast);
            }
        }

        private String[] getForecastJson(String postalCode) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            int count = 7;

            try {

                String strUrl = buildUrl(postalCode, count);

                URL url = new URL(strUrl);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (urlConnection.getResponseCode() >= 300 || responseCode < 200) {
                    Log.e(TAG, "Request was unsuccessful. Error " + responseCode);
                    return null;
                }

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                if (builder.length() == 0) {
                    return null;
                }
                forecastJsonStr = builder.toString();
            } catch (IOException e) {
                Log.e(TAG, "getForecastJson: ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr, count);
            } catch (JSONException e) {
                Log.e(TAG, "getForecastJson: Parse error", e);
                return null;
            }
        }


        private String buildUrl(String postalCode, int count) {
            Uri uri = Uri.parse(API_URL).buildUpon()
                    .appendQueryParameter(QUERY_API_ID_KEY, QUERY_API_ID_VALUE)
                    .appendQueryParameter(QUERY_PARAM_MODE_KEY, QUERY_PARAM_MODE_VALUE)
                    .appendQueryParameter(QUERY_PARAM_COUNT_KEY, String.valueOf(count))
                    .appendQueryParameter(QUERY_PARAM_UNIT_KEY, QUERY_PARAM_UNIT_VALUE)
                    .appendQueryParameter(QUERY_PARAM_POSTAL_KEY, postalCode).build();
            return uri.toString();
        }


        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE dd MMM", Locale.getDefault());
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            String unit = mSharedPreferences.getString(getString(R.string.pref_units_label),
                    getString(R.string.pref_units_metric));
            if (getString(R.string.pref_units_imperial).equals(unit)) {
                high = transformCelsiusToFahrenheit(high);
                low = transformCelsiusToFahrenheit(low);
            }
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private double transformCelsiusToFahrenheit(double temperatureInCelsius) {
            //From: http://www.rapidtables.com/convert/temperature/how-celsius-to-fahrenheit.htm
            return temperatureInCelsius * 9 / 5 + 32;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Calendar dayTime = new GregorianCalendar();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = dayTime.get(Calendar.DAY_OF_MONTH);

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                // Cheating to convert this to UTC time, which is what we want anyhow
                dayTime.set(Calendar.DAY_OF_MONTH, julianStartDay + i);
                day = getReadableDateString(dayTime.getTimeInMillis());

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultStrs;

        }


    }

}
