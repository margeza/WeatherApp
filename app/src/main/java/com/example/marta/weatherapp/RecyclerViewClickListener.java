package com.example.marta.weatherapp;

import android.view.View;

import com.example.marta.weatherapp.data.Forecast;

/**
 * Created by Marta on 2018-01-11.
 */

public interface RecyclerViewClickListener {
    public void recyclerViewListClicked(View v, int position, Forecast forecast);
}
