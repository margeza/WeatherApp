package com.example.marta.weatherapp.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Marta on 2017-12-12.
 */

public class Forecast implements JSONPopulator<JSONArray> {

    private ArrayList<Integer> codeList = new ArrayList<Integer>();
    private ArrayList<Integer> tempHighList = new ArrayList<Integer>();
    private ArrayList<Integer> tempLowList = new ArrayList<Integer>();
    private ArrayList<String> descriptionList = new ArrayList<String>();
    private ArrayList<String> dateList = new ArrayList<String>();
    private ArrayList<String> dayList = new ArrayList<String>();
    private int dataLenght;

    public int getDataLenght() {
        return dataLenght;
    }

    public int getCode(int day) {
        return codeList.get(day);
    }

    public int getTempHigh(int day) {
        return tempHighList.get(day);
    }

    public int getTempLow(int day) {
        return tempLowList.get(day);
    }

    public String getDescription(int day) {
        return descriptionList.get(day);
    }

    public String getDate(int day) {
        return dateList.get(day);
    }

    public String getDay(int day) {
        return dayList.get(day);
    }



    @Override
    public void populate(JSONArray data) {

        dataLenght = data.length();

        for(int i = 0; i <data.length(); i++){
            JSONObject day;
            try {
                day = (JSONObject) data.get(i);
                codeList.add(day.optInt("code"));
                tempHighList.add(day.optInt("high"));
                tempLowList.add(day.optInt("low"));
                descriptionList.add(day.optString("text"));
                dateList.add(day.optString("date"));
                dayList.add(day.optString("day"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
