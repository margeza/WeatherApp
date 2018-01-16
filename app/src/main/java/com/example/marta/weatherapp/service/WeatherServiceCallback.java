package com.example.marta.weatherapp.service;

import com.example.marta.weatherapp.data.Channel;

/**
 * Created by Marta on 2017-12-12.
 */

public interface WeatherServiceCallback {
    void serviceSuccess(Channel channel);
    void serviceFailure(Exception exception);
}
