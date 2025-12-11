package com.common.business.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class PddSoOutStockDTO {

    private String nextLevelId;

    /**
     * 销售订单单号
     */
    private String soCode;

    /**
     * 来源单号
     */
    private String sourceCode;

    /**
     * 运输单号
     */
    private String trackNo;
    /**
     * 承运商 名称
     */
    private String carrierName;
    /**
     * 平台店铺
     */
    private String platformShop;

    /**
     * 平台仓库
     */
    private String platformWarehouse;

    /**
     * 出库日期
     */
    private LocalDate billDate;

    /**
     * 第三方单据编号
     */
    private String thirdCode;

    /**
     * 来源系统
     */
    private String sourceSystem;

    /**
     * 物流公司代码
     */
    private String logisticsCompanyCode;

    /**
     * 物流公司名称
     */
    private String logisticsCompanyName;

    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;

    /**
     * 明细
     */
    private List<PddSoOutStockDetailDTO> detailList;

    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class PddSoOutStockDetailDTO {

        /**
         * 平台订单号
         */
        private String platformCode;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 实发数量
         */
        private Integer actualQty;
    }
}
