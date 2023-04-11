package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.wms.dto.WarehouseReceiveDetailDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 退货单DTO
 * @Author Luo_WG
 * @Date 2023/4/7 10:52
 **/
@Data
@NoArgsConstructor
public class PurchaseReturnOrderDTO {

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 退货人id
         */
        private String returnUserId;

        /**
         * 退货方式
         */
        private String returnWay;

        /**
         * 退货来源
         */
        private String returnSource;

        /**
         * 退货人组织id
         */
        private String returnOrgId;

        /**
         * 退货原因
         */
        private String receiceReason;

        /**
         * 退货费用
         */
        private BigDecimal receiceCost;

        /**
         * 退货运费
         */
        private BigDecimal receiceShippingCost;

        /**
         * 其它费用
         */
        private BigDecimal otherCost;

        /**
         * 退货日期
         */
        private LocalDate returnTime;

        /**
         * 报价明细
         */
        @Valid
        private List<PurchaseReturnOrderDetailDTO.AddDTO> purchasePriceDetailList;

    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 退货单id
         */
        private String id;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 退货人id
         */
        private String returnUserId;

        /**
         * 退货方式
         */
        private String returnWay;

        /**
         * 退货来源
         */
        private String returnSource;

        /**
         * 退货人组织id
         */
        private String returnOrgId;

        /**
         * 退货原因
         */
        private String receiceReason;

        /**
         * 退货费用
         */
        private BigDecimal receiceCost;

        /**
         * 退货运费
         */
        private BigDecimal receiceShippingCost;

        /**
         * 其它费用
         */
        private BigDecimal otherCost;

        /**
         * 退货日期
         */
        private LocalDate returnTime;

        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 签收单明细
         */
        @Valid
        private List<PurchaseReturnOrderDetailDTO.UpdateDTO> purchasePriceDetailList;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * 审核状态 waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核
         */
        @TableField("approve_status")
        private String approveStatus;

        /**
         * 单据编号
         */
        @TableField("code")
        private String code;

        /**
         * 采购订单id
         */
        @TableField("purchase_order_id")
        private String purchaseOrderId;

        /**
         * 采购订单编号
         */
        @TableField("purchase_order_code")
        private String purchaseOrderCode;

        /**
         * 供应商id
         */
        @TableField("supplier_id")
        private String supplierId;

        /**
         * 供应商名称
         */
        @TableField("supplier_name")
        private String supplierName;

        /**
         * 退货人id
         */
        @TableField("return_user_id")
        private String returnUserId;

        /**
         * 退货人名称
         */
        @TableField("return_user_name")
        private String returnUserName;

        /**
         * 退货方式
         */
        @TableField("return_way")
        private String returnWay;

        /**
         * 退货来源
         */
        @TableField("return_source")
        private String returnSource;

        /**
         * 退货人组织id
         */
        @TableField("return_org_id")
        private String returnOrgId;

        /**
         * 退货人组织名称
         */
        @TableField("return_org_name")
        private String returnOrgName;

        /**
         * 退货原因
         */
        @TableField("receice_reason")
        private String receiceReason;

        /**
         * 退货费用
         */
        @TableField("receice_cost")
        private BigDecimal receiceCost;

        /**
         * 退货运费
         */
        @TableField("receice_shipping_cost")
        private BigDecimal receiceShippingCost;

        /**
         * 其它费用
         */
        @TableField("other_cost")
        private BigDecimal otherCost;

        /**
         * 退货日期
         */
        @TableField("return_time")
        private LocalDate returnTime;

        /**
         * 作废状态（0未作废，1已作废）
         */
        @TableField("invalid_status")
        private String invalidStatus;

        /**
         * 作废时间
         */
        @TableField("invalid_time")
        private LocalDateTime invalidTime;

        /**
         * 审核人id
         */
        @TableField("approve_user_id")
        private String approveUserId;

        /**
         * 审核人名称
         */
        @TableField("approve_user_name")
        private String approveUserName;

        /**
         * 审核时间
         */
        @TableField("approve_time")
        private LocalDateTime approveTime;

        /**
         * 报价明细
         */
        @Valid
        private List<PurchaseReturnOrderDetailDTO.ViewDTO> purchasePriceDetailList;

    }


    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 表id
         */
        private String id;

        /**
         * 退货单号
         */
        private String code;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * 单据状态
         */
        private String approveStatus;

        /**
         * 作废状态
         */
        private String invalidStatus;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 退货日期
         */
        private LocalDate returnTime;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;

        /**
         * 退货数量
         */
        private Integer returnQty;

        /**
         * 退货原因
         */
        private String receiceReason;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 退货方式
         */
        private String returnWay;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 退货人
         */
        private String returnUserName;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 单据状态
         */
        private List<String> approveStatusList;

        /**
         * 创建开始时间
         */
        private LocalDate createStartTime;

        /**
         * 创建结束时间
         */
        private LocalDate createEndTime;

        /**
         * 计划交货时间开始时间
         */
        private LocalDate planReceiveStartTime;

        /**
         * 计划交货时间结束时间
         * @return
         */
        private LocalDate planReceiveEndTime;

        /**
         * 审核开始时间
         */
        private LocalDate approveStartTime;

        /**
         * 审核结束时间
         * @return
         */
        private LocalDate approveEndTime;

        /**
         * skuNo集合
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 采购订单编号
         */
        private String purchaseOrderCode;

        /**
         * 交货仓库id
         */
        private List<String> deliveryWarehouseIdList;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;
    }
}
