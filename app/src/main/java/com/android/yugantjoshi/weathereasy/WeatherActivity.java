package com.android.yugantjoshi.weathereasy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yugantjoshi.weathereasy.models.WeatherData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    TextView latitude_text, longitude_text, temp_text, humidity_text;
    private static final int REQUEST_LOCATION = 1888;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    Location lastLocation;
    String lat, lon;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        buildGoogleApiClient();

        latitude_text = (TextView) findViewById(R.id.lat_text);
        longitude_text = (TextView) findViewById(R.id.lon_text);
        temp_text = (TextView)findViewById(R.id.temp_text);
        humidity_text =(TextView)findViewById(R.id.humidity_text);

    }

    public void getWeatherUpdateCall(double latitude, double longitude)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherServiceInterface weatherService = retrofit.create(WeatherServiceInterface.class);

        Call<WeatherData> weatherCall = weatherService.getCurrentWeather(latitude,longitude);

        //Execute the call
        weatherCall.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {

                String humidity = String.valueOf(response.body().getMain().getHumidity());
                String temperature = String.valueOf(response.body().getMain().getTemp());

                temp_text.setText("Current Temperature: "+temperature);
                humidity_text.setText("Humidity: " + humidity);
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Toast.makeText(WeatherActivity.this, R.string.update_fail, Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    protected synchronized void buildGoogleApiClient()
    {
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
    public void onConnected(Bundle bundle)
    {
        Log.d("Connected", "Connected to API");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("Connected","IF STATEMENT");



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    Toast.makeText(WeatherActivity.this, "Need to access Location",Toast.LENGTH_LONG).show();
                }
                Log.d("Connected","REQUEST PERMISSIONS");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }
        }

        Log.d("Connected","AFTER IF STATEMENT");
        Log.d("Connected","GETTING LOCATION");
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        Log.d("Connected","GOT LOCATION");
        if (lastLocation != null) {
            Log.d("Connected","GETTING LOCATION");
            lat = String.valueOf(lastLocation.getLatitude());
            lon = String.valueOf(lastLocation.getLongitude());
            Log.d("Connected","WE HAVE COORDINATES!");

            latitude = Double.valueOf(lat);
            longitude = Double.valueOf(lon);

            getWeatherUpdateCall(latitude,longitude);

            Log.d("LATITUDE: ",lat);
            Log.d("LONGITUDE: ",lon);

            latitude_text.setText(lat);
            longitude_text.setText(lon);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(requestCode==REQUEST_LOCATION)
        {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {

            }
            else
            {
                Toast.makeText(WeatherActivity.this, "Permission not Granted",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions,grantResults);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    @Override
    public void onStart()
    {

        super.onStart();
        googleApiClient.connect();
    }
    @Override
    public void onPause()
    {

        super.onPause();
        googleApiClient.disconnect();
    }

}
