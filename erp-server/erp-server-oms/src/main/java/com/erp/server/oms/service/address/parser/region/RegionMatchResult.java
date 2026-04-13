package com.erp.server.oms.service.address.parser.region;

/**
 * Region parse result.
 */
public class RegionMatchResult {
    private String provinceId;
    private String province;
    private String cityId;
    private String city;
    private String districtId;
    private String district;
    private String matchedProvinceToken;
    private String matchedCityToken;
    private String matchedDistrictToken;

    public String getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getMatchedProvinceToken() {
        return matchedProvinceToken;
    }

    public void setMatchedProvinceToken(String matchedProvinceToken) {
        this.matchedProvinceToken = matchedProvinceToken;
    }

    public String getMatchedCityToken() {
        return matchedCityToken;
    }

    public void setMatchedCityToken(String matchedCityToken) {
        this.matchedCityToken = matchedCityToken;
    }

    public String getMatchedDistrictToken() {
        return matchedDistrictToken;
    }

    public void setMatchedDistrictToken(String matchedDistrictToken) {
        this.matchedDistrictToken = matchedDistrictToken;
    }
}
