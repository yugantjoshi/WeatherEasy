package com.android.yugantjoshi.weathereasy;

import com.android.yugantjoshi.weathereasy.models.WeatherData;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by yugantjoshi on 3/14/16.
 */
public interface WeatherServiceInterface
{
    @GET("/data/2.5/weather?q=London,uk&appid=37c55f8b0f66c94308d9635209bd7b4c")
    Call<WeatherData> getCurrentWeather();
}
