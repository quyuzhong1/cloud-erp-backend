package com.erp.oms.aliexpress.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname AddressRequest
 * @Description TODO
 * @Date 2024-01-10 14:51
 * @Created by yl
 */
@Data
@Builder
public class AddressRequest implements Serializable {

    private String clientId;

    private String clientSecret;

    private String baseUrl;

    private String token;

    private String orderId;

    private String oaid;
}
