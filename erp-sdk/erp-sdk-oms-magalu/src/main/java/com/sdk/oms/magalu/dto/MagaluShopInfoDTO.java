package com.sdk.oms.magalu.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MagaluShopInfoDTO implements Serializable {

    private String id;

    private String name;

    private String clientId;

    private String clientSecret;

    private String baseUrl;

    private String apiBaseUrl;

    private String redirectUrl;

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private String scope;
}
