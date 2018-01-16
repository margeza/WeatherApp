package com.example.marta.weatherapp.data;

import org.json.JSONObject;

/**
 * Created by Marta on 2017-12-12.
 */

public class Condition implements JSONPopulator<JSONObject> {

    private int code;
    private int temperature;
    private String description;
    private String date;

    public String getDate() {
        return date;
    }

    public int getCode() {
        return code;
    }

    public int getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void populate(JSONObject data) {
        code = data.optInt("code");
        temperature = data.optInt("temp");
        description = data.optString("text");
        date = data.optString("date");
    }
}
