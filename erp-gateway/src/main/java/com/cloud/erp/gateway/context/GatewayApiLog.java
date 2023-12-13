package com.cloud.erp.gateway.context;

import lombok.Data;

/**
 * GatewayLog对象
 * @Author Luo_WG
 * @Date 2023/12/6 14:44
 **/
@Data
public class GatewayApiLog {

    /**
     * 请求地址
     */
    private String requestUri;

    /**
     * GET请求查询参数
     */
    private String queryParams;

    /**
     * 请求参数
     */
    private String requestBody;

    /**
     * 返回码
     */
    private String responseCode;

    /**
     * 返回参数
     */
    private String responseBody;
    /**
     *开始时间
     */
    private Long startTime;
    /**
     * 结束时间
     */
    private Long endTime;
    /**
     * 耗费时长
     */
    private Long duration;

    /**
     * scheme：HTTP OR HTTPS
     */
    private String scheme;

    /**
     * method：POST OR GET
     */
    private String method;
    /**
     * 客户端HOST
     */
    private String clientHost;
    /**
     * 客户端IP
     */
    private String clientIp;

}
