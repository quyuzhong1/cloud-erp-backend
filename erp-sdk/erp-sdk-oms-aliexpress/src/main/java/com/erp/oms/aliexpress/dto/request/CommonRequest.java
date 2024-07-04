package com.erp.oms.aliexpress.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;


/**
 * 普通请求参数
 */
@Data
@Builder
public class CommonRequest implements Serializable {


    private String clientId;

    private String clientSecret;

    private String baseUrl;

    private String apiName;

    private String token;
}
