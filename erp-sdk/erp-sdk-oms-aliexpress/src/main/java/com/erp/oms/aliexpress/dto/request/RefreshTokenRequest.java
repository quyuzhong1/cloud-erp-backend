package com.erp.oms.aliexpress.dto.request;

import lombok.Builder;
import lombok.Data;

/**
 * @author Lambda
 * @Classname RefreshTokenRequest
 * @Description 刷新token
 * @Date 2023-12-12 10:57
 * @Created by yl
 */
@Data
@Builder
public class RefreshTokenRequest {

    /**
     * 刷新token
     *
     */
    private String refreshToken;


    private String clientId;

    private String clientSecret;

    private String baseUrl;

    private String apiName;
}
