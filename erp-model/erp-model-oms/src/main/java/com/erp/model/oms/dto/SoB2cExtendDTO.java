package com.erp.model.oms.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 销售订单-tiktok全托管属性表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-03-24
*/
@Data
@NoArgsConstructor
public class SoB2cExtendDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 要求发货时间
        */
        private LocalDateTime requiredDeliveryTime;

        /**
        * 要求收货时间
        */
        private LocalDateTime requiredReceiveTime;

        /**
        * 发货预警时间
        */
        private LocalDateTime deliveryWarningTime;

        /**
        * 平台订单来源:PLATFORM=平台备货,MERCHANT=自主备货,ABNORMAL_REDELIVERY=异常补货
        */
        private String orderSourceType;
        /**
         * 军区id
         */
        private String partitionId;
        /**
         * 军区编码
         */
        private String partitionCode;
        /**
         * 军区名称
         */
        private String partitionName;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 要求发货时间
        */
        private LocalDateTime requiredDeliveryTime;

        /**
        * 要求收货时间
        */
        private LocalDateTime requiredReceiveTime;

        /**
        * 发货预警时间
        */
        private LocalDateTime deliveryWarningTime;

        /**
        * 平台订单来源:PLATFORM=平台备货,MERCHANT=自主备货,ABNORMAL_REDELIVERY=异常补货
         * SoB2cExtendOrderSourceTypeEnum
         *
        */
        @NotBlank(message = "订单来源不能为空")
        private String orderSourceType;


    }


}