package com.cloud.erp.gateway.context;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @Author Luo_WG
 * @Date 2023/12/6 14:00
 **/
@Getter
@Setter
@ToString
public class GatewayContext<T> {

    public static final String CACHE_GATEWAY_CONTEXT = "cacheGatewayContext";
    /**
     * whether read request data
     */
    protected Boolean readRequestData = false;
    /**
     * whether read response data
     */
    protected Boolean readResponseData = false;
    /**
     * cache json body
     */
    protected String requestBody;
    /**
     * cache Response Body
     */
    protected Object responseBody;
    /**
     * request headers
     */
    protected HttpHeaders requestHeaders;
    /**
     * cache form data
     */
    protected MultiValueMap<String, String> formData;
    /**
     * cache all request data include:form data and query param
     */
    protected MultiValueMap<String, String> allRequestData = new LinkedMultiValueMap<>(0);

    /**
     * Gateway Extra Data
     */
    protected GatewayContextExtraData<T> gatewayContextExtraData;

}