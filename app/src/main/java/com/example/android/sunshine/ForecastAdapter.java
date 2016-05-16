package com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract;
import org.w3c.dom.Text;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

  public ForecastAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
  }

  private final int VIEW_TYPE_TODAY = 0;
  private final int VIEW_TYPE_FUTURE_DAY = 1;
  private final int VIEW_TYPE_COUNT = 2;
  private boolean mUseTodayLayout;

  @Override
  public int getItemViewType(int position) {
    return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
  }

  @Override
  public int getViewTypeCount() {
    return VIEW_TYPE_COUNT;
  }

  public void setUseTodayLayout(boolean useTodayLayout){
    mUseTodayLayout = useTodayLayout;
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
    } else if(viewType == VIEW_TYPE_FUTURE_DAY){
      layoutId = R.layout.list_item_forecast;
    }

    View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
    ViewHolder viewHolder = new ViewHolder(view);
    view.setTag(viewHolder);
    return view;
  }

  /*
      This is where we fill-in the views with the contents of the cursor.
   */
  @Override
  public void bindView(View view, Context context, Cursor cursor) {

    ViewHolder viewHolder = (ViewHolder) view.getTag();

    int weatherID = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
    int viewType = getItemViewType(cursor.getPosition());
    if(viewType == VIEW_TYPE_TODAY){
      viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherID));
    } else if(viewType == VIEW_TYPE_FUTURE_DAY){
      viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherID));
    }

    long dateMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
    viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext, dateMillis));

    String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
    viewHolder.forecastView.setText(forecast);

    // Read user preference for metric or imperial temperature units
    boolean isMetric = Utility.isMetric(context);

    // Read high temperature from cursor
    double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
    viewHolder.highView.setText(Utility.formatTemperature(mContext, high));

    double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
    viewHolder.lowView.setText(Utility.formatTemperature(mContext, low));
  }

  public static class ViewHolder{
    public final ImageView iconView;
    public final TextView dateView;
    public final TextView forecastView;
    public final TextView highView;
    public final TextView lowView;

    public ViewHolder(View view){
      iconView = (ImageView) view.findViewById(R.id.list_item_icon);
      dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
      forecastView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
      highView = (TextView) view.findViewById(R.id.list_item_high_textview);
      lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
    }
  }
}