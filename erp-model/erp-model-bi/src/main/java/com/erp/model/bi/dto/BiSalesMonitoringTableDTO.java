package com.erp.model.bi.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/3 17:45
 */
@Data
@NoArgsConstructor
public class BiSalesMonitoringTableDTO {

    /**
     * SKU
     */
    private String skuNo;

    /**
     * 品名
     */
    private String itemName;

    /**
     * 品牌id
     */
    private String brandId;

    /**
     * 品牌
     */
    private String brandName;

    /**
     * 部门id
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 平台名称
     */
    private String sourcePlatform;

    /**
     * 国家名称
     */
    private String countryNameCn;

    /**
     * 品类id
     */
    private String categoryId;

    /**
     * 品类
     */
    private String category;

    /**
     * 负责人id
     */
    private String chargeId;

    /**
     * 负责人
     */
    private String chargeName;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品售价
     */
    private BigDecimal amountAfter;

    /**
     * 汇率
     */
    private BigDecimal currencyRate;

    /**
     * 新品标识 1为新品 0 为非新品
     */
    private Integer newSign;

    /**
     * 订单日期
     */
    private LocalDate platformCreateTime;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupViewDTO extends PermissionsDTO {

        /**
         * 分组数据
         */
        private String groupData;

        /**
         * 显示数据
         */
        private String viewData;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 类型名称 （部门、人员、店铺、品类、SKU）
         */
        private String typeId;
        /**
         * 类型名称 （部门、人员、店铺、品类、SKU）
         */
        private String typeName;

        /**
         * 月份
         */
        private Integer month;

        /**
         * 对应值
         */
        private BigDecimal value;
    }

}
