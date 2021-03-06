package com.example.android.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.example.android.sunshine.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback{

  private final String LOG_TAG = MainActivity.class.getSimpleName();
  private final String DETAILFRAGMENT_TAG = "DFTAG";

  private String mLocation;
  private boolean mTwoPane;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mLocation = Utility.getPreferredLocation(this);

    setContentView(R.layout.activity_main);
    if (findViewById(R.id.weather_detail_container) != null) {
      mTwoPane = true;

      if (savedInstanceState == null) {
        getSupportFragmentManager().beginTransaction()
            .add(R.id.weather_detail_container, new DetailFragment())
            .commit();
      }
    } else {
      mTwoPane = false;
      getSupportActionBar().setElevation(0f);
    }
    ForecastFragment forecastFragment = ((ForecastFragment) getSupportFragmentManager()
    .findFragmentById(R.id.fragment_forecast));
    forecastFragment.setUseTodayLayout(!mTwoPane);

    SunshineSyncAdapter.initializeSyncAdapter(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
    }
    if (id == R.id.action_map) {
      openPreferredLocationInMap();
    }

    return super.onOptionsItemSelected(item);
  }

  private void openPreferredLocationInMap() {
    String location = Utility.getPreferredLocation(this);

    Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                          .appendQueryParameter("q", location)
                          .build();

    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setData(geoLocation);
    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivity(intent);
    } else {
      Log.v(LOG_TAG, "Couldn't call " + location + ", no app available");
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    String location = Utility.getPreferredLocation(this);
    if (location != null && !location.equals(mLocation)) {
      ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
      if (ff != null) {
        ff.onLocationChanged();
      }
      DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
      if(df != null){
        df.onLocationChanged(location);
      }
      mLocation = location;
    }
  }

  // Launch fragment or start activity, depending on whether TwoPane or not
  @Override
  public void onItemSelected(Uri contentUri){
    if(mTwoPane){
      Bundle args = new Bundle();
      args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

      DetailFragment df = new DetailFragment();
      df.setArguments(args);

      getSupportFragmentManager().beginTransaction()
          .replace(R.id.weather_detail_container, df, DETAILFRAGMENT_TAG)
          .commit();
    } else {
      Intent intent = new Intent(this, DetailActivity.class).setData(contentUri);
      startActivity(intent);
    }
  }
}