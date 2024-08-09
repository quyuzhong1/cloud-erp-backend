package com.erp.model.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lambda
 * @Classname PackageDTO
 * @Description TODO
 * @Date 2024-01-26 14:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PackageDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class WeightParamDTO {
        /**
         * 单号
         */
        @NotBlank(message = "单号不能为空")
        private String code;
    }

    /**
     * 查询重量
     * @author will
     * @date 2024/7/1 10:32
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeightDTO {

        /**
         * 重量
         */
        private BigDecimal weight;

        /**
         * 重量单位
         */
        private String weightUnit;
    }

    /**
     * 扫描结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanResultDTO {
        /**
         * 跟踪单号
         */
        private String trackNo;

        /**
         * 运输单号
         */
        private String transportNo;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单code
         */
        private String soCode;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 是否是组包的物流商 根据wms组包配置 判断是否是已配置组包物流商
         * true 发货物流商+中转物流商+中转渠道+店铺
         * false 发货物流商+中转物流商+中转渠道
         */
        private Boolean isPackageSupplier;
        /**
         * 唯一值拼接 用于 isPackageSupplier
         */
        private String uniqueId;
        /**
         * 包裹重量
         */
        private BigDecimal weight;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 物流渠道
         */
        private String logisticsChannelId;

        /**
         * b2c物流单Id
         */
        private String logisticsId;

        /**
         * 渠道id
         */
        private String logisticsChannelName;

        /**
         * 渠道物流商
         */
        private String logisticsSupplierId;
        /**
         * 渠道物流商名
         */
        private String logisticsSupplierName;
        /**
         * 物流商简称
         */
        private String logisticsSupplierShortName;

        /**
         * 组包状态
         */
        private String packageStatus;

        /**
         * 单据状态
         */
        private String billStatus;

        /**
         * 称重状态
         */
        private String weightStatus;

        /**
         * 中转物流商Id
         */
        private String transferLogisticsSupplierId;

        /**
         * 中转物流商中文名
         */
        private String transferLogisticsSupplierName;

        /**
         * 是否自动出库
         */
        private Boolean isAutoOut;

        private Boolean isIntercept;
        /**
         * 预报状态
         */
        private String transferStatus;

        /**
         * 中转商渠道id
         */
        private String transferLogisticsChannelId;

        /**
         * 中转商渠道名
         */
        private String transferLogisticsChannelName;

        /**
         * 销售平台
         */
        private String dictPlatform;
    }

    /**
     * 分页条件
     */
    @Data
    @NoArgsConstructor
    public static class ScanDTO{
        /**
         * 单号
         */
        @NotBlank(message = "单号不能为空")
        private String code;

        /**
         * 重量
         */
        @Digits(integer = 16,fraction = 4,message = "重量最大16位数，小数位不能大于4位数")
        private BigDecimal weight;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 是否自动出库
         */
        private Boolean isAutoOut;
    }

    /**
     * 分页条件
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO{

        /**
         * 物流渠道id 来源 http://172.16.100.11:3002/project/128/interface/api/25999
         */
        private List<String> logisticsChannelIdList;

        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;

        /**
         * 店铺id 集合  来源 http://172.16.100.11:3002/project/110/interface/api/24424
         */
        private List<String> shopIdList;


        /**
         * 不需要传参数
         */
        private List<String> billStatusList;

        /**
         * 不需要传参数
         */
        private List<String> packageStatusList;

    }

    /**
     * 分页的
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingViewDTO{
        /**
         * 跟踪单号
         */
        private String trackNo;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单code
         */
        private String soCode;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 是否是组包的物流商 根据wms组包配置 判断是否是已配置组包物流商
         * true 发货物流商+中转物流商+中转渠道+店铺
         * false 发货物流商+中转物流商+中转渠道
         */
        private Boolean isPackageSupplier;
        /**
         * 唯一值拼接 用于 isPackageSupplier
         */
        private String uniqueId;
        /**
         * 包裹重量
         */
        private BigDecimal weight;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 物流渠道
         */
        private String logisticsChannelId;


        /**
         * 渠道id
         */
        private String logisticsChannelName;

        /**
         * 渠道物流商
         */
        private String logisticsSupplierId;
        /**
         * 渠道物流商名
         */
        private String logisticsSupplierName;

        /**
         * 物流商简称
         */
        private String logisticsSupplierShortName;

        /**
         * 收件人
         */
        private String receiverName;

        /**
         * 国家
         */
        private String country;

        /**
         * 国家名
         */
        private String countryName;

        /**
         * 中转物流商Id
         */
        private String transferLogisticsSupplierId;

        /**
         * 中转物流商中文名
         */
        private String transferLogisticsSupplierName;

        /**
         * 中转商渠道id
         */
        private String transferLogisticsChannelId;

        /**
         * 中转商渠道名
         */
        private String transferLogisticsChannelName;
    }


    /**
     * 组包预报
     */
    @Data
    @NoArgsConstructor
    public static class MergePackageDTO{
        /**
         * 单据id
         */
        private List<String> ids;

        /**
         * 是否自动出库
         */
        private Boolean isAutoOut;
    }
}
