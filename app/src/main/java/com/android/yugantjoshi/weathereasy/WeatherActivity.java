package com.android.yugantjoshi.weathereasy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yugantjoshi.weathereasy.models.Weather;
import com.android.yugantjoshi.weathereasy.models.WeatherData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

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

    @Bind(R.id.temp_text)
    TextView temp_text;
    @Bind(R.id.humidity_text)
    TextView humidity_text;
    @Bind(R.id.city_text)
    TextView city_text;
    @Bind(R.id.wind_text)
    TextView wind_text;
    @Bind(R.id.weather_icon)
    MaterialIconView weather_icon;
    @Bind(R.id.water_icon)
    MaterialIconView water_icon;
    // @Bind(R.id.rain_percentage) TextView rain_percentage;

    Typeface lightFont;
    double latitude, longitude;

    private static final int REQUEST_LOCATION = 1888;
    protected GoogleApiClient googleApiClient;
    protected Location lastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        ButterKnife.bind(this);
        lightFont = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf");

        temp_text.setTypeface(lightFont);
        city_text.setTypeface(lightFont);


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

        buildGoogleApiClient();

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


    /*
    Loads the proper weather icon based on description
     */
    private void changeWeatherIcon(String conditions) {
        //weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_CLOUDY);
        Log.d("Weather Icon", conditions);
        if (conditions.equalsIgnoreCase("rain")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_RAINY);
        } else if (conditions.equalsIgnoreCase("clouds")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_CLOUDY);
        } else if (conditions.equalsIgnoreCase("clear")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_SUNNY);
        } else if (conditions.equalsIgnoreCase("snow")) {
            weather_icon.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_SNOWY);
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
                String tempConv = String.format("%.1f", temp);
                double wind = response.body().getWind().getSpeed();
                String windConv = String.format("%.1f", wind);
                String conditions = response.body().getWeather().get(0).getMain();

                changeWeatherIcon(conditions);

                humidity_text.setText(String.valueOf(response.body().getMain().getHumidity()) + "%");
                wind_text.setText(String.valueOf(windConv) + " mph");
                temp_text.setText(String.valueOf(tempConv) + "˚");
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Toast.makeText(WeatherActivity.this, R.string.update_fail, Toast.LENGTH_LONG).show();
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
    }

    @Override
    public void onPause() {

        super.onPause();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(WeatherActivity.this, R.string.update_fail, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


}
