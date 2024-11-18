package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname SettingForecastDTO
 * @Date 2024-01-18 14:53
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SettingForecastDTO implements Serializable {


    /**
     * 列表线上的
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        private String id;

        /**
         * 物流商id
         */
        private String logisticsSupplierId;

        /**
         * 物流商
         */
        private String logisticsSupplierName;

        /**
         * 物流渠道id集合
         */
        private List<String> logisticsChannelIdList;

        /**
         * 物流渠道集合名称
         */
        private List<String> logisticsChannelNameList;

        /**
         * 是否强制组包 true 是
         */
        private Boolean isMustPackage;


        /**
         * 组包启用时间
         */
        private LocalDateTime enablePackageTime;


        /**
         * 是否强制中转  true 是
         */
        private Boolean isMustTransfer;

        /**
         * 中转时间启用时间
         */
        private LocalDateTime enableTransferTime;

        /**
         * 中转物流商id
         */
        private String transferLogisticsSupplierId;

        /**
         * 中转物流商名
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

        /**
         * 是否自动预报  true 是
         */
        private Boolean isAutoForecast;

    }


    @Data
    @NoArgsConstructor
    public static class SaveOrUpdateDTO {

        private String id;

        @NotBlank(message = "物流商不能为空")
        private String logisticsSupplierId;

        /**
         * 物流渠道id集合
         */
        @NotEmpty(message = "物流渠道不能为空")
        private List<String> logisticsChannelIdList;

        /**
         * 是否强制组包 不能为空
         */
        @NotNull(message = "是否强制组包不能为空")
        private Boolean isMustPackage;


        /**
         * 组包启用时间
         */
        private LocalDateTime enablePackageTime;


        @NotNull(message = "是否强制中转不能为空")
        private Boolean isMustTransfer;

        /**
         * 中转时间启用时间
         */
        private LocalDateTime enableTransferTime;

        /**
         * 中转物流商id
         */
        private String transferLogisticsSupplierId;


        /**
         * 中转商渠道id
         */
        private String transferLogisticsChannelId;

        /**
         * 是否自动预报  true 是
         */
        @NotNull(message = "是否自动预报不能为空")
        private Boolean isAutoForecast;

    }


    /**
     * 预报状态
     */
    @Data
    @NoArgsConstructor
    public static class ForecastStatusDTO {

        /**
         * 组包状态
         */
        private String packageStatus;


        /**
         * 中转状态
         */
        private String transferStatus;

        /**
         * 报关平台
         */
        private String declarePlatform;

        /**
         * 报关平台名
         */
        private String declarePlatformName;

        /**
         * 渠道名称
         */
        private String LogisticsChannelName;

        /**
         * 中转商渠道id
         */
        private String transferLogisticsChannelId;

        /**
         * 中转商渠道名
         */
        private String transferLogisticsChannelName;
    }

    @Data
    @NoArgsConstructor
    public static class FindSettingForecastDTO{

        private String logisticsChannelId;

        private LocalDateTime orderTime;

    }

    @Data
    @NoArgsConstructor
    public static class FindByLogisticsSupplierDTO{

        private String logisticsSupplierId;

        private LocalDateTime orderTime;

    }

    @Data
    @NoArgsConstructor
    public static class CheckRegistrationDTO{
        /**
         * 报关平台
         */
        @NotBlank(message = "报关平台不能为空")
        private String declarePlatform;


        /**
         * sku no
         */
        @NotNull(message = "SKU不能为空")
        @Size(min = 1,message = "至少需要一个SKU")
        private List<String> skuNoList;
    }


    @Data
    @NoArgsConstructor
    public static class CheckRegistrationResultDTO{

        /**
         * 渠道名称
         */
        private String logisticsChannelName;

        /**
         * 报关平台名
         */
        private String declarePlatform;

        /**
         * 报关平台名
         */
        private String declarePlatformName;



        /**
         * sku no
         */
        private List<String> notRegistrationSkuNoList;

        /**
         * 组包状态
         */
        private String packageStatus;


        /**
         * 中转状态
         */
        private String transferStatus;
    }

}
