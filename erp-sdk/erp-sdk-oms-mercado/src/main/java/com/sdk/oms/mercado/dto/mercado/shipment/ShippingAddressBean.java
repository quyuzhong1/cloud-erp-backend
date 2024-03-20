package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingAddressBean {
    /**
     * address_id : 1331228739
     * address_line : XXXXXXX
     * street_name : XXXXXXX
     * street_number : XXXXXXX
     * comment : null
     * zip_code : XXXXXXX
     * city : {"id":"SEstSEtLd2FpIFRzaW5n","name":"Kwai Tsing"}
     * state : {"id":"HK-HK","name":"Hong Kong"}
     * country : {"id":"HK","name":"Hong Kong"}
     * neighborhood : {"id":null,"name":null}
     * municipality : {"id":null,"name":null}
     * agency : {"agency_id":null,"carrier_id":null,"description":null,"open_hours":null,"phone":null,"type":null}
     * types : ["billing","default_selling_address","shipping"]
     * latitude : 0
     * longitude : 0
     * geolocation_type : null
     * geolocation_last_updated : null
     * geolocation_source : null
     * delivery_preference :
     */

    @JsonProperty("address_id")
    private int addressId;
    @JsonProperty("address_line")
    private String addressLine;
    @JsonProperty("street_name")
    private String streetName;
    @JsonProperty("street_number")
    private String streetNumber;
    @JsonProperty("comment")
    private Object comment;
    @JsonProperty("zip_code")
    private String zipCode;
    @JsonProperty("city")
    private CityBean city;
    @JsonProperty("state")
    private StateBean state;
    @JsonProperty("country")
    private CountryBean country;
    @JsonProperty("neighborhood")
    private NeighborhoodBean neighborhood;
    @JsonProperty("municipality")
    private MunicipalityBean municipality;
    @JsonProperty("agency")
    private AgencyBean agency;
    @JsonProperty("latitude")
    private int latitude;
    @JsonProperty("longitude")
    private int longitude;
    @JsonProperty("geolocation_type")
    private Object geolocationType;
    @JsonProperty("geolocation_last_updated")
    private Object geolocationLastUpdated;
    @JsonProperty("geolocation_source")
    private Object geolocationSource;
    @JsonProperty("delivery_preference")
    private String deliveryPreference;
    @JsonProperty("types")
    private List<String> types;

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public Object getComment() {
        return comment;
    }

    public void setComment(Object comment) {
        this.comment = comment;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public CityBean getCity() {
        return city;
    }

    public void setCity(CityBean city) {
        this.city = city;
    }

    public StateBean getState() {
        return state;
    }

    public void setState(StateBean state) {
        this.state = state;
    }

    public CountryBean getCountry() {
        return country;
    }

    public void setCountry(CountryBean country) {
        this.country = country;
    }

    public NeighborhoodBean getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(NeighborhoodBean neighborhood) {
        this.neighborhood = neighborhood;
    }

    public MunicipalityBean getMunicipality() {
        return municipality;
    }

    public void setMunicipality(MunicipalityBean municipality) {
        this.municipality = municipality;
    }

    public AgencyBean getAgency() {
        return agency;
    }

    public void setAgency(AgencyBean agency) {
        this.agency = agency;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public Object getGeolocationType() {
        return geolocationType;
    }

    public void setGeolocationType(Object geolocationType) {
        this.geolocationType = geolocationType;
    }

    public Object getGeolocationLastUpdated() {
        return geolocationLastUpdated;
    }

    public void setGeolocationLastUpdated(Object geolocationLastUpdated) {
        this.geolocationLastUpdated = geolocationLastUpdated;
    }

    public Object getGeolocationSource() {
        return geolocationSource;
    }

    public void setGeolocationSource(Object geolocationSource) {
        this.geolocationSource = geolocationSource;
    }

    public String getDeliveryPreference() {
        return deliveryPreference;
    }

    public void setDeliveryPreference(String deliveryPreference) {
        this.deliveryPreference = deliveryPreference;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
