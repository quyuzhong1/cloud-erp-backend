package com.erp.oms.aliexpress.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname ProductRequest
 * @Description 商品请求参数
 * @Date 2023-11-30 12:17
 * @Created by yl
 */
@Data
@Builder
public class ProductRequest implements Serializable {

    private String clientId;

    private String clientSecret;

    private String baseUrl;

    private String apiName;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    private String token;

    /**
     * 当前页
     */
    private Integer currentPage;
}
