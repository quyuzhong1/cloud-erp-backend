package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 头程虚拟仓订单跟踪明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-02-25
*/
@Data
@NoArgsConstructor
public class FirstMileProcessingDetailDTO implements Serializable {


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * indexId
         */
        private String indexId;
        /**
         * 主键id
         */
        private String id;
        /**
         * 主表id
         */
        private String mainId;

        /**
         * 头程发货单id
         */
        private String firstMileDeliveryId;
        /**
         * 头程发货单编码
         */
        private String firstMileDeliveryCode;
        /**
         * 头程发货单审核状态
         */
        private String deliveryApproveStatus;
        /**
         * 头程发货单审核状态名称
         */
        private String deliveryApproveStatusName;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 剩余冻结数量
         */
        private Integer frozenQty;
        /**
         * 冻结时长（天）
         */
        private Integer frozenDays;
        /**
         * 出库单据id
         */
        private String outstockOrderId;
        /**
         * 出库单据编码
         */
        private String outstockOrderCode;
        /**
         * 出库单据类型（同sourceType）
         */
        private String outstockOrderType;
        /**
         * 出库单据类型名称（同sourceType）
         */
        private String outstockOrderTypeName;
        /**
         * 出库单据状态
         */
        private String outstockOrderStatus;
        /**
         * 出库单据状态名称
         */
        private String outstockOrderStatusName;
        /**
         * 出库单据时间
         */
        private LocalDateTime outstockOrderTime;
        /**
         * 出库数量
         */
        private Integer outstockQty;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 标签,outstock出库,frozen发货冻结,unShipped七日未发
         */
        private List<String> labelList;
    }

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
        * 头程发货单id
        */
        private String firstMileDeliveryId;

        /**
        * 头程发货单编码
        */
        private String firstMileDeliveryCode;

        /**
        * 头程发货明细id
        */
        private String firstMileDeliveryDetailId;

        /**
        * 头程发货单审核状态
        */
        private String deliveryApproveStatus;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 出库单据id
        */
        private String outstockOrderId;

        /**
        * 出库单据编码
        */
        private String outstockOrderCode;

        /**
        * 出库单据类型（同sourceType）
        */
        private String outstockOrderType;

        /**
        * 出库单据状态
        */
        private String outstockOrderStatus;

        /**
        * 出库单据时间
        */
        private LocalDateTime outstockOrderTime;

        /**
        * 出库数量
        */
        private Integer outstockQty;

        /**
        * 主表id
        */
        private String mainId;


    }


    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 头程发货单id
        */
        @NotBlank(message = "头程发货单id不能为空")
        @Size(max = 19,message = "头程发货单id最大长度不能超过19位")
        private String firstMileDeliveryId;

        /**
        * 头程发货单编码
        */
        @NotBlank(message = "头程发货单编码不能为空")
        @Size(max = 32,message = "头程发货单编码最大长度不能超过32位")
        private String firstMileDeliveryCode;

        /**
        * 头程发货明细id
        */
        @NotBlank(message = "头程发货明细id不能为空")
        @Size(max = 32,message = "头程发货明细id最大长度不能超过32位")
        private String firstMileDeliveryDetailId;

        /**
        * 头程发货单审核状态
        */
        @NotBlank(message = "头程发货单审核状态不能为空")
        @Size(max = 32,message = "头程发货单审核状态最大长度不能超过32位")
        private String deliveryApproveStatus;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 出库单据id
        */
        @NotBlank(message = "出库单据id不能为空")
        private String outstockOrderId;

        /**
        * 出库单据编码
        */
        @NotBlank(message = "出库单据编码不能为空")
        @Size(max = 32,message = "出库单据编码最大长度不能超过32位")
        private String outstockOrderCode;

        /**
        * 出库单据类型（同sourceType）
        */
        @NotBlank(message = "出库单据类型（同sourceType）不能为空")
        @Size(max = 32,message = "出库单据类型（同sourceType）最大长度不能超过32位")
        private String outstockOrderType;

        /**
        * 出库单据状态
        */
        @NotBlank(message = "出库单据状态不能为空")
        @Size(max = 32,message = "出库单据状态最大长度不能超过32位")
        private String outstockOrderStatus;

        /**
        * 出库单据时间
        */
        private LocalDateTime outstockOrderTime;

        /**
        * 出库数量
        */
        @NotNull(message = "出库数量不能为空")
        private Integer outstockQty;

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;


    }


}