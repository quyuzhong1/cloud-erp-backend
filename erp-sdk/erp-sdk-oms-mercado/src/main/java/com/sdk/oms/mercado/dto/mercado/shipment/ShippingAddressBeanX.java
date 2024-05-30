package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingAddressBeanX {
    /**
     * address_id : 271976606
     * address_line : 2 Poniente 196
     * street_name : 2 Poniente
     * street_number : 196
     * comment : M23L7 Referencia: M23L7
     * zip_code : 77760
     * city : {"id":"TVgtUk9PVHVsdW0","name":"Tulum"}
     * state : {"id":"MX-ROO","name":"Quintana Roo"}
     * country : {"id":"MX","name":"Mexico"}
     * neighborhood : {"id":null,"name":"Guerra de Castas"}
     * municipality : {"id":null,"name":"Tulum"}
     * agency : {"agency_id":null,"carrier_id":null,"description":null,"open_hours":null,"phone":null,"type":null}
     * types : ["billing","default_buying_address","default_return_address","shipping"]
     * latitude : 20.21588
     * longitude : -87.455037
     * geolocation_type : ROOFTOP
     * geolocation_last_updated : 2023-10-09T22:22:49.854Z
     * geolocation_source : map-verified
     * delivery_preference : business
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
    private String comment;
    @JsonProperty("zip_code")
    private String zipCode;
    @JsonProperty("city")
    private CityBeanX city;
    @JsonProperty("state")
    private StateBeanX state;
    @JsonProperty("country")
    private CountryBeanX country;
    @JsonProperty("neighborhood")
    private NeighborhoodBeanX neighborhood;
    @JsonProperty("municipality")
    private MunicipalityBeanX municipality;
    @JsonProperty("agency")
    private AgencyBeanX agency;
    @JsonProperty("latitude")
    private double latitude;
    @JsonProperty("longitude")
    private double longitude;
    @JsonProperty("geolocation_type")
    private String geolocationType;
    @JsonProperty("geolocation_last_updated")
    private String geolocationLastUpdated;
    @JsonProperty("geolocation_source")
    private String geolocationSource;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public CityBeanX getCity() {
        return city;
    }

    public void setCity(CityBeanX city) {
        this.city = city;
    }

    public StateBeanX getState() {
        return state;
    }

    public void setState(StateBeanX state) {
        this.state = state;
    }

    public CountryBeanX getCountry() {
        return country;
    }

    public void setCountry(CountryBeanX country) {
        this.country = country;
    }

    public NeighborhoodBeanX getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(NeighborhoodBeanX neighborhood) {
        this.neighborhood = neighborhood;
    }

    public MunicipalityBeanX getMunicipality() {
        return municipality;
    }

    public void setMunicipality(MunicipalityBeanX municipality) {
        this.municipality = municipality;
    }

    public AgencyBeanX getAgency() {
        return agency;
    }

    public void setAgency(AgencyBeanX agency) {
        this.agency = agency;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getGeolocationType() {
        return geolocationType;
    }

    public void setGeolocationType(String geolocationType) {
        this.geolocationType = geolocationType;
    }

    public String getGeolocationLastUpdated() {
        return geolocationLastUpdated;
    }

    public void setGeolocationLastUpdated(String geolocationLastUpdated) {
        this.geolocationLastUpdated = geolocationLastUpdated;
    }

    public String getGeolocationSource() {
        return geolocationSource;
    }

    public void setGeolocationSource(String geolocationSource) {
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
