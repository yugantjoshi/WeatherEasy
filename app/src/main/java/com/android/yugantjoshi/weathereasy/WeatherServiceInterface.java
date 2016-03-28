package com.android.yugantjoshi.weathereasy;

import com.android.yugantjoshi.weathereasy.models.WeatherData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by yugantjoshi on 3/14/16.
 */
public interface WeatherServiceInterface
{
    @GET("weather?appid=37c55f8b0f66c94308d9635209bd7b4c")
    Call<WeatherData> getCurrentWeather(@Query("lat") double lat, @Query("lon") double lon);
}
