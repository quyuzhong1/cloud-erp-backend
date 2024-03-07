package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;

public class AgencyBean {
    /**
     * agency_id : null
     * carrier_id : null
     * description : null
     * open_hours : null
     * phone : null
     * type : null
     */

    @SerializedName("agency_id")
    private Object agencyId;
    @SerializedName("carrier_id")
    private Object carrierId;
    @SerializedName("description")
    private Object description;
    @SerializedName("open_hours")
    private Object openHours;
    @SerializedName("phone")
    private Object phone;
    @SerializedName("type")
    private Object type;

    public Object getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Object agencyId) {
        this.agencyId = agencyId;
    }

    public Object getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Object carrierId) {
        this.carrierId = carrierId;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Object getOpenHours() {
        return openHours;
    }

    public void setOpenHours(Object openHours) {
        this.openHours = openHours;
    }

    public Object getPhone() {
        return phone;
    }

    public void setPhone(Object phone) {
        this.phone = phone;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }
}
