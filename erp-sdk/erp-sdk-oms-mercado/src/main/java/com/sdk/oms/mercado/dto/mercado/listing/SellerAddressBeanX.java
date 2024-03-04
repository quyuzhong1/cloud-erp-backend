package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;

public class SellerAddressBeanX {
    /**
     * city : {"id":"SEstSEtLd2FpIFRzaW5n","name":"Kwai Tsing"}
     * state : {"id":"HK-HK","name":"Hong Kong"}
     * country : {"id":"HK","name":"Hong Kong"}
     * id : 1332219696
     */

    @SerializedName("city")
    private CityBean city;
    @SerializedName("state")
    private CityBean state;
    @SerializedName("country")
    private CityBean country;
    @SerializedName("id")
    private int id;

    public CityBean getCity() {
        return city;
    }

    public void setCity(CityBean city) {
        this.city = city;
    }

    public CityBean getState() {
        return state;
    }

    public void setState(CityBean state) {
        this.state = state;
    }

    public CityBean getCountry() {
        return country;
    }

    public void setCountry(CityBean country) {
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
