package com.erp.model.tms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Lambda
 * @Classname SettingForecastDTO
 * @Description TODO
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

    }


    @Data
    @NoArgsConstructor
    public static class SaveOrUpdateDTO {

        private String id;

        @NotBlank(message = "物流商不能为空")
        private String logisticsSupplierId;

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

}
