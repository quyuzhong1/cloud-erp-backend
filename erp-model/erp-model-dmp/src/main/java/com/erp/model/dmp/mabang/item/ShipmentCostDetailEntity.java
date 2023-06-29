package com.erp.model.dmp.mabang.item;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @CreateTime: 2023-06-29  16:14
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@ToString
public class ShipmentCostDetailEntity {

    /**
     * 费用名称
     */
    private String name;

    /**
     * 费用名称编码
     */
    private String nameCode;

    /**
     * 费用币种
     */
    private String currency;

    /**
     * 原始金额
     */
    private String originalFee;

    /**
     * 费用金额
     */
    private String money;

    /**
     * 分摊方式
     */
    private String type;

    /**
     * 物流供应商编码
     */
    private String logisticsCode;

}