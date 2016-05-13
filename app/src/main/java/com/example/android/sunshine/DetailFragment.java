package com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Created by Lancer521 on 5/13/2016.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String LOG_TAG = DetailFragment.class.getSimpleName();

  private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

  private ShareActionProvider mShareActionProvider;
  private String mForecastStr;

  private static final int DETAIL_LOADER = 0;

  private static final String[] DETAIL_COLUMNS = {
      WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
      WeatherEntry.COLUMN_DATE,
      WeatherEntry.COLUMN_SHORT_DESC,
      WeatherEntry.COLUMN_MAX_TEMP,
      WeatherEntry.COLUMN_MIN_TEMP,
      WeatherEntry.COLUMN_HUMIDITY,
      WeatherEntry.COLUMN_PRESSURE,
      WeatherEntry.COLUMN_WIND_SPEED,
      WeatherEntry.COLUMN_DEGREES,
      WeatherEntry.COLUMN_WEATHER_ID,
      WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
  };

  public static final int COL_WEATHER_ID = 0;
  public static final int COL_WEATHER_DATE = 1;
  public static final int COL_WEATHER_DESC = 2;
  public static final int COL_WEATHER_MAX_TEMP = 3;
  public static final int COL_WEATHER_MIN_TEMP = 4;
  public static final int COL_WEATHER_HUMIDITY = 5;
  public static final int COL_WEATHER_PRESSURE = 6;
  public static final int COL_WEATHER_WIND_SPEED = 7;
  public static final int COL_WEATHER_DEGREES = 8;
  public static final int COL_WEATHER_CONDITION_ID = 9;

  private ImageView mIconView;
  private TextView mFriendlyDateView;
  private TextView mDateView;
  private TextView mDescriptionView;
  private TextView mHighTempView;
  private TextView mLowTempView;
  private TextView mHumidityView;
  private TextView mWindView;
  private TextView mPressureView;

  public DetailFragment() {
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
    mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
    mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
    mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
    mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
    mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
    mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
    mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity);
    mWindView = (TextView) rootView.findViewById(R.id.detail_wind);
    mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure);
    return rootView;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.detailfragment, menu);

    MenuItem menuItem = menu.findItem(R.id.action_share);

    mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

    if (mForecastStr != null) {
      mShareActionProvider.setShareIntent(createShareForecastIntent());
    }
  }

  private Intent createShareForecastIntent() {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
    return shareIntent;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Intent intent = getActivity().getIntent();
    if (intent == null || intent.getData() == null) {
      return null;
    }
    return new CursorLoader(
                               getActivity(),
                               intent.getData(),
                               DETAIL_COLUMNS,
                               null,
                               null,
                               null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    if (data != null && data.moveToFirst()) {
      int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
      int artResource = Utility.getArtResourceForWeatherCondition(weatherId);

      mIconView.setImageResource(artResource);

      long date = data.getLong(COL_WEATHER_DATE);
      mFriendlyDateView.setText(Utility.getDayName(getActivity(), date));
      mDateView.setText(Utility.getFormattedMonthDay(getActivity(), date));

      String weatherDescription = data.getString(COL_WEATHER_DESC);
      mDescriptionView.setText(weatherDescription);

      boolean isMetric = Utility.isMetric(getActivity());

      String high = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
      mHighTempView.setText(high);

      String low = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
      mLowTempView.setText(low);

      float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
      mHumidityView.setText(getString(R.string.format_humidity, humidity));

      String wind = Utility.getFormattedWind(getActivity(), data.getFloat(COL_WEATHER_WIND_SPEED), data.getFloat(COL_WEATHER_DEGREES));
      mWindView.setText(wind);

      float pressure = data.getFloat(COL_WEATHER_PRESSURE);
      mPressureView.setText(getString(R.string.format_pressure, pressure));

      mForecastStr = String.format("%s - %s - %s/%s", date, weatherDescription, high, low);

      if (mShareActionProvider != null) {
        mShareActionProvider.setShareIntent(createShareForecastIntent());
      }
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
  }
}