package com.sdk.oms.pdd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AccessTokenResponse extends PopBaseHttpResponse{
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private Integer expiresIn;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("scope")
    private List<String> scope;
    @JsonProperty("owner_id")
    private String ownerId;
    @JsonProperty("owner_name")
    private String ownerName;

    public AccessTokenResponse() {
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public Integer getExpiresIn() {
        return this.expiresIn;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public List<String> getScope() {
        return this.scope;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public String getOwnerName() {
        return this.ownerName;
    }
}
