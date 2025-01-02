package com.erp.model.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * sku成本信息
 * @Author Luo_WG
 * @Date 2023/9/13 18:45
 **/
@Data
public class SkuCostDTO {
    /**
     * id
     */
    private String id;

    /**
     * skuId
     */
    private String skuId;

    /**
    * sku编号
    */
    private String skuNo;

    /**
    * 成本日期
    */
    private LocalDate costDate;

    /**
    * 备注
    */
    private String remark;

    /**
    * 有效状态
    */
    private Boolean status;

    /**
    * 成本价格（含税）
    * 成本价格
    */
    private BigDecimal costPrice;

    /**
     * 成本价格（不含税）
     */
    private BigDecimal notTaxCostPrice;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 币别
     */
    private String currency;

    /**
    * 最近的采购日期3个月前的日期
    */
    private LocalDate threeMonthsAgoDate;

    /**
    * 最近的采购日期
    */
    private LocalDate latestPurchaseDate;


    @Data
    @NoArgsConstructor
    public static class ParamDTO {

        /**
         * skuId集合
         */
        private List<String> skuIdList;

        /**
         * sku编号集合
         */
        private List<String> skuNoList;
        //采购订单
        private List<String> purchaseOrderIds;
        //供应商id
        private List<String> supplierIds;

    }

    @Data
    @NoArgsConstructor
    public static class SendWarnMsgDTO {

        /**
         * 日期
         */
        private String date;

        /**
         * 币别
         */
        private String currency;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryPurchaseDTO{
        //日期
        private List<LocalDate> localDateList;
        //采购订单id
        private List<String> purchaseOrderIds;
        //供应商id
        private List<String> supplierIds;
    }
}