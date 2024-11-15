package com.sdk.tms.track123.model.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @date 2024/4/9 16:49
 */
@Data
@Builder
public class OceanCarrierInfo implements Serializable {


    /**
     * 承运商编码
     */
    private String carrierCode;

    /**
     * 承运商中文名
     */
    private String carrierNameCN;

    /**
     * 承运商英文名称
     */
    private String carrierNameEN;

    /**
     * 承运商网页地址
     */
    private String carrierHomePage;

}
