package com.example.android.sunshine;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.SunshineSyncAdapter;

// Sample API Request: http://api.openweathermap.org/data/2.5/forecast/daily?zip=84321&mode=json&units=Metric&cnt=7&APPID=c2c3aecddb1e2520c223304316bc232b

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final int FORECAST_LOADER = 0;
  private ForecastAdapter mForecastAdapter;

  private ListView mListView;
  private int mPosition = ListView.INVALID_POSITION;
  private boolean mUseTodayLayout;

  private static final String SELECTED_KEY = "selected_position";

  private static final String[] FORECAST_COLUMNS = {
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

  public ForecastFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.forecastfragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_refresh) {
      updateWeather();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void setUseTodayLayout(boolean useTodayLayout){
    mUseTodayLayout = useTodayLayout;
    if(mForecastAdapter != null){
      mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
    mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
    mListView.setAdapter(mForecastAdapter);
    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        if (cursor != null) {
          String locationSetting = Utility.getPreferredLocation(getActivity());
          ((Callback) getActivity())
              .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                  locationSetting, cursor.getLong(COL_WEATHER_DATE)
              ));
        }
        mPosition = position;
      }
    });

    if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
      mPosition = savedInstanceState.getInt(SELECTED_KEY);
    }

    return rootView;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    super.onActivityCreated(savedInstanceState);
  }

  private void updateWeather() {
    SunshineSyncAdapter.syncImmediately(getActivity());
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

    String locationSetting = Utility.getPreferredLocation(getActivity());
    Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
        locationSetting, System.currentTimeMillis());

    return new CursorLoader(getActivity(),
                            weatherForLocationUri,
                            FORECAST_COLUMNS,
                            null,
                            null,
                            sortOrder);

  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mForecastAdapter.swapCursor(data);
    if(mPosition != ListView.INVALID_POSITION){
      mListView.smoothScrollToPosition(mPosition);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mForecastAdapter.swapCursor(null);
  }

  public void onLocationChanged(){
    updateWeather();
    getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
  }

  public interface Callback {
    /**
     * DetailFragmentCallback for when an item has been selected.
     */
    public void onItemSelected(Uri dateUri);
  }

  @Override
  public void onSaveInstanceState(Bundle outState){
    if(mPosition != ListView.INVALID_POSITION){
      outState.putInt(SELECTED_KEY, mPosition);
    }
    super.onSaveInstanceState(outState);
  }
}