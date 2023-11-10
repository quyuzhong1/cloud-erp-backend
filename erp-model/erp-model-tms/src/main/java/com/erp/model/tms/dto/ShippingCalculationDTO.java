package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 运费计算DTO
 * @date 2023/11/9 17:47
 */
@Data
public class ShippingCalculationDTO {


    /**
     * 查询条件
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 发货方式
         */
        private String shipmentMethod;

        /**
         * 起始地
         */
        private String fromCountry;

        /**
         * 目的地
         */
        private String toCountry;

        /**
         * 目的仓库
         */
        private String toWarehouse;

        /**
         * 城市
         */
        private String city;

        /**
         * 物流渠道id集合
         */
        private List<String> channelIdList;

        /**
         * 重量
         */
        private BigDecimal weight;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 长
         */
        private String length;

        /**
         * 宽
         */
        private String width;

        /**
         * 高
         */
        private String height;
    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 物流商名称
         */
        private String logisticsName;

        /**
         * 物流渠道名称名称
         */
        private String channelName;

        /**
         * 目的仓库
         */
        private String toWarehouseName;

        /**
         * 目的地
         */
        private String toCountry;

        /**
         * 分区
         */
        private String region;

        /**
         * 有效期
         */
        private String effectivePeriod ;

        /**
         * 运费
         */
        private BigDecimal shippingCost;

        /**
         * 挂号费
         */
        private BigDecimal registrationCost;

        /**
         * 操作费
         */
        private BigDecimal operatingCost;

        /**
         * 其他费
         */
        private BigDecimal otherCost;

        /**
         * 总金额
         */
        private BigDecimal totalAmount;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 运费
         */
        private BigDecimal shippingCost;

        /**
         * 操作费
         */
        private BigDecimal operatingCost;

        /**
         * 挂号费
         */
        private BigDecimal registrationCost;

        /**
         * 折扣费
         */
        private BigDecimal discountCost;

        /**
         * 签名费
         */
        private BigDecimal signatureCost;

        /**
         * 保险费
         */
        private BigDecimal premiumCost;

        /**
         * 超尺寸附加费
         */
        private BigDecimal oversizeSurchargeCost;

        /**
         * 燃油附加费
         */
        private BigDecimal fuelSurchargeCost;

        /**
         * 最终运费
         */
        private BigDecimal totalShippingCost;
    }


}
