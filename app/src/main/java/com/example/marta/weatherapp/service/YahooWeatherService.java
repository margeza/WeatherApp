package com.example.marta.weatherapp.service;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.marta.weatherapp.data.Channel;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Marta on 2017-12-12.
 */

public class YahooWeatherService {
    private WeatherServiceCallback callback;
    private String location;
    private Exception error;
    private String temperatureUnit = "C";

    public String getLocation() {
        return location;
    }

    public YahooWeatherService(WeatherServiceCallback callback){
        this.callback = callback;
    }

    private String getTemperatureUnit() {
        return temperatureUnit;
    }

    public void refreshWeather(final String l){
        this.location = l;
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                String unit = getTemperatureUnit().equalsIgnoreCase("f") ? "f" : "c";
                String YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\") and u='" + unit + "'", location);
                String endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL));

                try {
                    URL url = new URL(endpoint);

                    URLConnection connection = url.openConnection();

                    InputStream inputStream = connection.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        result.append(line);
                    }
                    return result.toString();
                }catch (Exception e) {
                    error = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {

                if(s == null && error != null){
                    callback.serviceFailure(error);
                    return;
                }
                try {
                    JSONObject data = new JSONObject(s);

                    JSONObject queryResults = data.optJSONObject("query");

                    int count = queryResults.optInt("count");

                    if (count == 0){
                        callback.serviceFailure(new LocationWeatherException("No weather information found for"+location));
                        return;
                    }

                    Channel channel = new Channel();
                    channel.populate(queryResults.optJSONObject("results").optJSONObject("channel"));

                    callback.serviceSuccess(channel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }.execute(location);
    }

    public  class LocationWeatherException extends Exception{
        public LocationWeatherException(String datailMessage){
            super((datailMessage));
        }
    }
}
