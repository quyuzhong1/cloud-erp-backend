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
        private String returnMode;

        /**
         * 退货人组织id
         */
        private String returnOrgId;

        /**
         * 退货原因
         */
        private String receiceRemark;

        /**
         * 退货日期
         */
        private LocalDate billTime;

        /**
         * 退货来源 （质检单，签收单，采购订单）
         */
        private String sourceType;

        /**
         * 来源id
         */
        private String sourceId;

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
         * 退货人组织id
         */
        private String returnOrgId;

        /**
         * 退货原因
         */
        private String receiceRemark;

        /**
         * 退货日期
         */
        private LocalDate billTime;
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
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单编号
         */
        private String purchaseOrderCode;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 退货人id
         */
        private String returnUserId;

        /**
         * 退货人名称
         */
        private String returnUserName;

        /**
         * 退货方式
         */
        private String returnMode;

        /**
         * 退货人组织id
         */
        private String returnOrgId;

        /**
         * 退货人组织名称
         */
        private String returnOrgName;

        /**
         * 退货原因
         */
        private String receiceRemark;

        /**
         * 退货日期
         */
        private LocalDate billTime;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

        /**
         * 作废时间
         */
        private LocalDateTime invalidTime;

        /**
         * 审核人id
         */
        private String approveUserId;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 退货来源
         */
        private String sourceType;

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
         * 审核状态名称
         */
        private String approveStatusName;

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
        private LocalDate billTime;

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
        private String receiceRemark;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 退货方式
         */
        private String returnMode;

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
        private List<LocalDate> createTime;

        /**
         * 计划交货时间
         */
        private List<LocalDate> planReceiveTime;

        /**
         * 审核时间
         */
        private List<LocalDate> approveTime;

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
