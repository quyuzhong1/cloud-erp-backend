package com.erp.model.wms.dto;

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
 * 请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-12-12
*/
@Data
@NoArgsConstructor
public class FbaTransitCalculateDetailReportDTO implements Serializable {




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
        * 平台产品id（ASIN）
        */
        private String asin;

        /**
        * 平台sku（msku）
        */
        private String msku;

        /**
        * FNSKU
        */
        private String fnSku;

        /**
        * ERP的SKU
        */
        private String skuNo;

        /**
        * 申报数量
        */
        private Integer declareQty;

        /**
        * 收发差异
        */
        private Integer diffQty;

        /**
        * 收货数量
        */
        private Integer receiveQty;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 期初在途数量
        */
        private Integer initTransitQty;

        /**
        * 本期发货数量
        */
        private Integer currentDeliveryQty;

        /**
        * 本期签收数量
        */
        private Integer currentReceiveQty;

        /**
        * 期末在途数量
        */
        private Integer endPeriodTransitQty;

        /**
        * 期末在途调整数量
        */
        private Integer endPeriodTransitAdjustQty;

        /**
        * 期末在途（调整后）
        */
        private Integer afterEndPeriodTransitQty;

        /**
        * 调整原因
        */
        private String adjustReason;

        /**
        * 调整时间
        */
        private LocalDateTime adjustTime;

        /**
        * 调整人名称
        */
        private String adjustUserName;

        /**
        * 调整人id
        */
        private String adjustUserId;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 平台产品id（ASIN）
        */
        @NotBlank(message = "平台产品id（ASIN）不能为空")
        @Size(max = 64,message = "平台产品id（ASIN）最大长度不能超过64位")
        private String asin;

        /**
        * 平台sku（msku）
        */
        @NotBlank(message = "平台sku（msku）不能为空")
        @Size(max = 64,message = "平台sku（msku）最大长度不能超过64位")
        private String msku;

        /**
        * FNSKU
        */
        @NotBlank(message = "FNSKU不能为空")
        @Size(max = 64,message = "FNSKU最大长度不能超过64位")
        private String fnSku;

        /**
        * 申报数量
        */
        @NotNull(message = "申报数量不能为空")
        private Integer declareQty;

        /**
        * 收发差异
        */
        @NotNull(message = "收发差异不能为空")
        private Integer diffQty;

        /**
        * 收货数量
        */
        @NotNull(message = "收货数量不能为空")
        private Integer receiveQty;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 期初在途数量
        */
        @NotNull(message = "期初在途数量不能为空")
        private Integer initTransitQty;

        /**
        * 本期发货数量
        */
        @NotNull(message = "本期发货数量不能为空")
        private Integer currentDeliveryQty;

        /**
        * 本期签收数量
        */
        @NotNull(message = "本期签收数量不能为空")
        private Integer currentReceiveQty;

        /**
        * 期末在途数量
        */
        @NotNull(message = "期末在途数量不能为空")
        private Integer endPeriodTransitQty;

        /**
        * 期末在途调整数量
        */
        @NotNull(message = "期末在途调整数量不能为空")
        private Integer endPeriodTransitAdjustQty;

        /**
        * 期末在途（调整后）
        */
        @NotNull(message = "期末在途（调整后）不能为空")
        private Integer afterEndPeriodTransitQty;

        /**
        * 调整原因
        */
        @NotBlank(message = "调整原因不能为空")
        @Size(max = 255,message = "调整原因最大长度不能超过255位")
        private String adjustReason;

        /**
        * 调整时间
        */
        @NotNull(message = "调整时间不能为空")
        private LocalDateTime adjustTime;

        /**
        * 调整人名称
        */
        @NotBlank(message = "调整人名称不能为空")
        @Size(max = 255,message = "调整人名称最大长度不能超过255位")
        private String adjustUserName;

        /**
        * 调整人id
        */
        @NotBlank(message = "调整人id不能为空")
        @Size(max = 1,message = "调整人id最大长度不能超过1位")
        private String adjustUserId;


    }


}