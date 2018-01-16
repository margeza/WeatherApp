package com.example.marta.weatherapp.data;

import org.json.JSONObject;

/**
 * Created by Marta on 2017-12-12.
 */

public class Channel implements JSONPopulator<JSONObject> {

    private Units units;
    private Item item;

    public Units getUnits() {
        return units;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public void populate(JSONObject data) {
        units = new Units();
        units.populate(data.optJSONObject("units"));

        item = new Item();
        item.populate(data.optJSONObject("item"));
    }
}
