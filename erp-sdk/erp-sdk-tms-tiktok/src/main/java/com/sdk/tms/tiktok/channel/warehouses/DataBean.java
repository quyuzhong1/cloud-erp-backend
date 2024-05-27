package com.sdk.tms.tiktok.channel.warehouses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataBean {
    @SerializedName("warehouses")
    private List<WarehousesBean> warehouses;

    public List<WarehousesBean> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<WarehousesBean> warehouses) {
        this.warehouses = warehouses;
    }
}
