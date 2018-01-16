package com.example.marta.weatherapp.data;

import org.json.JSONObject;

/**
 * Created by Marta on 2017-12-12.
 */

public interface JSONPopulator<E> {
    void populate(E data);
}
