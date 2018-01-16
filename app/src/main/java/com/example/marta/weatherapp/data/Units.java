package com.example.marta.weatherapp.data;

import org.json.JSONObject;

/**
 * Created by Marta on 2017-12-12.
 */

public class Units implements JSONPopulator<JSONObject> {
    private String temperature;

    public String getTemperature() {
        return temperature;
    }

    @Override
    public void populate(JSONObject data) {
        temperature = data.optString("temperature");
    }
}
