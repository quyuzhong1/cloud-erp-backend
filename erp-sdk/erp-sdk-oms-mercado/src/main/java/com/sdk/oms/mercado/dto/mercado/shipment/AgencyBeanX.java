package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AgencyBeanX {
    /**
     * agency_id : null
     * carrier_id : null
     * description : null
     * open_hours : null
     * phone : null
     * type : null
     */

    @JsonProperty("agency_id")
    private Object agencyId;
    @JsonProperty("carrier_id")
    private Object carrierId;
    @JsonProperty("description")
    private Object description;
    @JsonProperty("open_hours")
    private Object openHours;
    @JsonProperty("phone")
    private Object phone;
    @JsonProperty("type")
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
