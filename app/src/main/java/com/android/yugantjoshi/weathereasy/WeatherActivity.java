package com.android.yugantjoshi.weathereasy;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yugantjoshi.weathereasy.models.WeatherData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    protected static final String TAG = "MainActivity";

    //TextViews
    @Bind(R.id.time_text)
    TextView time_text;
    @Bind(R.id.day_text)
    TextView day_text;
    @Bind(R.id.date_text)
    TextView date_text;
    @Bind(R.id.temp_text)
    TextView temp_text;
    @Bind(R.id.humidity_text)
    TextView humidity_text;
    @Bind(R.id.city_text)
    TextView city_text;
    @Bind(R.id.wind_text)
    TextView wind_text;
    //Icons
    @Bind(R.id.weather_icon)
    MaterialIconView weather_icon;

    double latitude, longitude;

    private static final int REQUEST_LOCATION = 1888;
    protected GoogleApiClient googleApiClient;
    protected Location lastLocation;

    BroadcastReceiver _broadcastReceiver;
    private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("H:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        permissionChecks();
        buildGoogleApiClient();

        ButterKnife.bind(this);
        setDate(day_text, date_text);

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        time_text.setText(dateFormat.format(new Date()).toString());

        setFont(day_text, "OpenSans-Light.ttf");
        setFont(time_text, "OpenSans-Light.ttf");
        setFont(date_text, "OpenSans-Light.ttf");
        setFont(temp_text, "OpenSans-Light.ttf");
        setFont(city_text, "OpenSans-Light.ttf");
        setFont(humidity_text, "OpenSans-Light.ttf");
        setFont(wind_text, "OpenSans-Light.ttf");
    }

    private void setDate(TextView currentDay, TextView fullDate)
    {
        Log.d("Time","Getting Current Time");
        TimeZone t = TimeZone.getDefault();
        Calendar c = Calendar.getInstance(t);
        Locale locale = Locale.getDefault();

        //Day and full Date
        currentDay.setText(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale));
        int year = c.get(Calendar.YEAR);
        String month = c.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
        int dayNumber = c.get(Calendar.DATE);
        fullDate.setText(month+" "+dayNumber+", "+year);
    }
    public void setFont(TextView textView, String font)
    {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/"+font);
        textView.setTypeface(typeface);
    }
    public void permissionChecks()
    {
        Log.d("Connected", "Check Permissions");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(WeatherActivity.this, "Need to access Location", Toast.LENGTH_LONG).show();
                }
                Log.d("Connected", "REQUEST PERMISSIONS");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        setLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setLocation();
            } else {
                Toast.makeText(WeatherActivity.this, "Permission not Granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


   //Loads weather icon based on conditions
    private void changeWeatherIcon(String conditions) {
        //weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_CLOUDY);
        Log.d("Weather Icon", conditions);
        if (conditions.equalsIgnoreCase("rain")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_POURING);
        } else if (conditions.equalsIgnoreCase("clouds")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_CLOUDY);
        } else if (conditions.equalsIgnoreCase("clear")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_SUNNY);
        } else if (conditions.equalsIgnoreCase("snow")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_SNOWY);
        }
        else if (conditions.equalsIgnoreCase("fog")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_FOG);
        }
        else if (conditions.equalsIgnoreCase("hail")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_HAIL);
        }
        else if (conditions.equalsIgnoreCase("lightning")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_LIGHTNING);
        }
        else if (conditions.equalsIgnoreCase("partly cloudy")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_PARTLYCLOUDY);
        }
        else if (conditions.equalsIgnoreCase("windy")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_WINDY);
        }
    }

    public void getWeatherUpdateCall(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherServiceInterface weatherService = retrofit.create(WeatherServiceInterface.class);
        Call<WeatherData> weatherCall = weatherService.getCurrentWeather(latitude, longitude);

        //Execute the call
        weatherCall.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {

                double temp = response.body().getMain().getTemp();
                //Convert temp to fahrenheit
                temp = temp * (9.0 / 5) - 459.67;
                String tempConv = String.format("%.0f", temp);
                double wind = response.body().getWind().getSpeed();
                String windConv = String.format("%.0f", wind);
                String conditions = response.body().getWeather().get(0).getMain();
                

                changeWeatherIcon(conditions);

                humidity_text.setText(String.valueOf(response.body().getMain().getHumidity()) + "%");
                wind_text.setText(String.valueOf(windConv) + " mph");
                temp_text.setText(String.valueOf(tempConv) + "˚");
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void setLocation() {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {

            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
            Log.d("Latitude", String.valueOf(latitude));
            Log.d("Longitude", String.valueOf(longitude));

            try {
                Geocoder geo = new Geocoder(WeatherActivity.this.getApplicationContext(), Locale.getDefault());
                List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
                if (addresses.isEmpty()) {
                    city_text.setText("Waiting for Location");
                } else {
                    if (addresses.size() > 0) {
                        city_text.setText(addresses.get(0).getLocality());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            getWeatherUpdateCall(latitude, longitude);

        } else {
            Toast.makeText(this, R.string.update_fail, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onStart() {

        super.onStart();
        googleApiClient.connect();
        _broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0)
                    time_text.setText(_sdfWatchTime.format(new Date()));
            }
        };

        registerReceiver(_broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

    }

    @Override
    public void onPause() {

        super.onPause();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
    @Override
    public void onStop()
    {
        super.onStop();
        if (_broadcastReceiver != null)
            unregisterReceiver(_broadcastReceiver);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(WeatherActivity.this, R.string.update_fail, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}