package com.sdk.oms.tiktok.dto.tiktok.channel.warehouses;

import com.google.gson.annotations.SerializedName;

public class AddressBean {
    /**
     * city : South Jakarta City
     * contact_person : TikTok Shop Partner Center
     * distict : Setiabudi
     * full_address : Jl. Jenderal Sudirman No.Kav. 25
     * phone_number : (+44)07153419266
     * postal_code : 12920
     * region : Republic of Indonesia
     * region_code : ID
     * state : Jakarta Province
     * town : Karet
     */

    @SerializedName("city")
    private String city;
    @SerializedName("contact_person")
    private String contactPerson;
    @SerializedName("distict")
    private String distict;
    @SerializedName("full_address")
    private String fullAddress;
    @SerializedName("phone_number")
    private String phoneNumber;
    @SerializedName("postal_code")
    private String postalCode;
    @SerializedName("region")
    private String region;
    @SerializedName("region_code")
    private String regionCode;
    @SerializedName("state")
    private String state;
    @SerializedName("town")
    private String town;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getDistict() {
        return distict;
    }

    public void setDistict(String distict) {
        this.distict = distict;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }
}
