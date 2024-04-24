package com.erp.model.oms.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

        /**
         * 是否禁用自动出库 false 未禁用
         */
        private Boolean disabled;

        /**
         * 预报状态
         */
        private String forcastStatus;

        /**
         * 是否拦截
         */
        private Boolean isIntercept;

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
        private BigDecimal weight;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 是否自动出库
         */
        @NotNull(message = "是否自动出库不能为空")
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
    }
}
