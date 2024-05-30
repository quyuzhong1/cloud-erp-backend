package com.sdk.oms.tiktok.dto.tiktok.channel.warehouses;

import com.google.gson.annotations.SerializedName;

public class WarehousesBean {
    /**
     * address : {"city":"South Jakarta City","contact_person":"TikTok Shop Partner Center","distict":"Setiabudi","full_address":"Jl. Jenderal Sudirman No.Kav. 25","phone_number":"(+44)07153419266","postal_code":"12920","region":"Republic of Indonesia","region_code":"ID","state":"Jakarta Province","town":"Karet"}
     * effect_status : ENABLED
     * id : 7354364871201720069
     * is_default : true
     * name : TikTok Shop Sandbox ID Local Sales warehouse
     * sub_type : DOMESTIC_WAREHOUSE
     * type : SALES_WAREHOUSE
     */

    @SerializedName("address")
    private AddressBean address;
    @SerializedName("effect_status")
    private String effectStatus;
    @SerializedName("id")
    private String id;
    @SerializedName("is_default")
    private boolean isDefault;
    @SerializedName("name")
    private String name;
    @SerializedName("sub_type")
    private String subType;
    @SerializedName("type")
    private String type;

    public AddressBean getAddress() {
        return address;
    }

    public void setAddress(AddressBean address) {
        this.address = address;
    }

    public String getEffectStatus() {
        return effectStatus;
    }

    public void setEffectStatus(String effectStatus) {
        this.effectStatus = effectStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
