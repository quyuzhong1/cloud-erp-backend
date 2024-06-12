package com.erp.oms.aliexpress.dto.request;

import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname OrderRequest
 * @Description
 * @Date 2023-11-29 14:32
 * @Created by yl
 */
@Data
@Builder
public class OrderRequest implements Serializable {


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

    public static OrderRequest builderByShopInfo(String apiName, AliExpressShopInfoDTO shopInfoDTO) {
        return OrderRequest.builder().
                clientId(shopInfoDTO.getClientId()).
                clientSecret(shopInfoDTO.getClientSecret()).
                baseUrl(shopInfoDTO.getBaseUrl()).
                apiName(apiName).
                currentPage(1).
                token(shopInfoDTO.getToken()).build();
    }
}
