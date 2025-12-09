package com.erp.model.oms.dto;

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
 * B2C寄样申请单拆分单请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-04
*/
@Data
@NoArgsConstructor
public class KolSubB2cApplicationDTO implements Serializable {




    /**
    *
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 备注
        */
        private String remark;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 拆分单编码
        */
        private String code;

        /**
        * 平台销售单号
        */
        private String platformSoCode;

        /**
        * 平台id
        */
        private String dictPlatform;
        private String dictPlatformName;

        /**
        * 达人id
        */
        private String partnerId;

        /**
        * 达人昵称
        */
        private String nickname;

        /**
        * 发货状态：waitShipped=待发货,shipped=已发货,partialShipped=部分发货
        */
        private String deliveryStatus;
        private String deliveryStatusName;

        /**
        * 订单状态：not=未生成,notApprove=未审核,approve=已审核
        */
        private String orderStatus;
        private String orderStatusName;

        /**
        * 跟踪号
        */
        private String trackNo;

        /**
        * 平台订单id
        */
        private String platformOrderId;

        /**
        * 平台订单编码
        */
        private String platformOrderCode;

        /**
         * 主键id
         */
        private String detailId;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 平台明细id
         */
        private String platformDetailId;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU编码
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;

        /**
         * 申请数量
         */
        private Integer applyQty;

        /**
         * 明细备注
         */
        private String detailRemark;

        /**
         * 项目名称
         */
        private String projectTag;
        private String projectTagName;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 平台销售单号
        */
        @NotBlank(message = "平台销售单号不能为空")
        @Size(max = 32,message = "平台销售单号最大长度不能超过32位")
        private String platformSoCode;

        /**
        * 平台id
        */
        @NotBlank(message = "平台id不能为空")
        @Size(max = 100,message = "平台id最大长度不能超过100位")
        private String dictPlatform;

        /**
        * 达人id
        */
        @NotBlank(message = "达人id不能为空")
        @Size(max = 19,message = "达人id最大长度不能超过19位")
        private String partnerId;

        /**
        * 达人昵称
        */
        @NotBlank(message = "达人昵称不能为空")
        @Size(max = 200,message = "达人昵称最大长度不能超过200位")
        private String nickname;

        /**
        * 发货状态：waitShipped=待发货,shipped=已发货,partialShipped=部分发货
        */
        @NotBlank(message = "发货状态：waitShipped=待发货,shipped=已发货,partialShipped=部分发货不能为空")
        @Size(max = 32,message = "发货状态：waitShipped=待发货,shipped=已发货,partialShipped=部分发货最大长度不能超过32位")
        private String deliveryStatus;

        /**
        * 订单状态：not=未生成,notApprove=未审核,approve=已审核
        */
        @NotBlank(message = "订单状态：not=未生成,notApprove=未审核,approve=已审核不能为空")
        @Size(max = 32,message = "订单状态：not=未生成,notApprove=未审核,approve=已审核最大长度不能超过32位")
        private String orderStatus;

        /**
        * 跟踪号
        */
        @NotBlank(message = "跟踪号不能为空")
        @Size(max = 255,message = "跟踪号最大长度不能超过255位")
        private String trackNo;

        /**
        * 平台订单id
        */
        @NotBlank(message = "平台订单id不能为空")
        @Size(max = 255,message = "平台订单id最大长度不能超过255位")
        private String platformOrderId;

        /**
        * 平台订单编码
        */
        @NotBlank(message = "平台订单编码不能为空")
        @Size(max = 255,message = "平台订单编码最大长度不能超过255位")
        private String platformOrderCode;


    }


}