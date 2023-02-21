package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 12:36
 */
@Data
@NoArgsConstructor
public class ProductPlanSaleDTO implements Serializable {

    /**
     * 销售信息id
     */
    private String id;

    /**
     * 产品规划ID
     */
    private String productPlanId;

    /**
     * 人民币定价
     */
    private String priceCny;

    /**
     * 美元定价
     */
    private String priceUsd;

    /**
     * 销售平台/渠道
     */
    private String salesPlatform;

    /**
     * 销售目标国家ID
     */
    private String salesTargetCountryId;

    /**
     * 销售目标国家
     */
    private String salesTargetCountry;

}
