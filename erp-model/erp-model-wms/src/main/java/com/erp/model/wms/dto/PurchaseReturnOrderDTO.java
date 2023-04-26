package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
         * 退货方式
         */
        private String returnMode;

        /**
         * 退货人组织id
         */
        private String returnOrgId;

        /**
         * 退货仓库id
         */
        private String returnWarehouseId;

        /**
         * 退货来源
         */
        private String sourceType;

        /**
         * 退货人id
         */
        private String returnUserId;

        /**
         * 退货原因
         */
        private String returnRemark;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商联系人id
         */
        private String supplierContactId;

        /**
         * 采购员id
         */
        private String purchaseUserId;

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
         * 采购订单编号
         */
        private String purchaseOrderCode;

        /**
         * 退货来源
         */
        private String sourceType;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 退货方式 退货扣款 退货补货
         */
        private String returnMode;

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
        private String returnRemark;

        /**
         * 退货仓库id
         */
        private String returnWarehouseId;

        /**
         * 退货仓库名称
         */
        private String returnWarehouseName;

        /**
         * 供应商联系人id
         */
        private String supplierContactId;

        /**
         * 采购用户id
         */
        private String purchaseUserId;

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
         * 退货方式
         */
        private String returnMode;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 供应商联系人id
         */
        private String supplierContactId;

        /**
         * 供应商联系人名称
         */
        private String supplierContactName;

        /**
         * 供应商地址
         */
        private String supplierAddress;

        /**
         * 退货人id
         */
        private String returnUserId;

        /**
         * 退货人名称
         */
        private String returnUserName;

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
        private String returnRemark;

        /**
         * 退货日期
         */
        private LocalDate billDate;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * 作废时间
         */
        private LocalDateTime invalidTime;

        /**
         * 作废备注
         */
        private String invalidRemark;

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
         * 退货来源名称
         */
        private String sourceTypeName;

        /**
         * 退货仓库id
         */
        private String returnWarehouseId;

        /**
         * 退货仓库名称
         */
        private String returnWarehouseName;

        /**
         * 采购用户id
         */
        private String purchaseUserId;

        /**
         * 采购用户名称
         */
        private String purchaseUserName;

        /**
         * 采购部门id
         */
        private String purchaseUserDeptId;

        /**
         * 采购部门名称
         */
        private String purchaseUserDeptName;

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
         * 供应商名称
         */
        private String supplierName;

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
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * skuId
         */
        private String skuId;

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
        private LocalDate billDate;

        /**
         * 退货仓库
         */
        private String returnWarehouseName;

        /**
         * 退货数量
         */
        private Integer returnQty;

        /**
         * 退货原因
         */
        private String returnRemark;

        /**
         * 退货方式
         */
        private String returnMode;

        /**
         * 退货方式名称
         */
        private String returnModeName;

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

        /**
         * 备注
         */
        private String remark;
    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 订单编号
         */
        private String code;

        /**
         * ids
         */
        private List<String> ids;

        /**
         * skuNo集合
         */
        private List<String> skuNoList;

        /**
         * 供应商id集合
         */
        private List<String> supplierIdList;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * 采购员id集合
         */
        private List<String> purchaseUserIdList;

        /**
         * 单据审核状态集合
         */
        private List<String> approveStatusList;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

        /**
         * 退货方式
         */
        private String returnMode;

        /**
         * 退货日期
         */
        private List<LocalDate> billDateList;

        /**
         * 退货创库id集合
         */
        private List<String> returnWarehouseIdList;

        /**
         * 创建人id集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

    }

    /**
     * 采购订单关联退货单的查询实体
     */
    @Data
    @NoArgsConstructor
    public static class OrderRefReceiveDTO {
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
         * 供应商名称
         */
        private String supplierName;

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
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * skuId
         */
        private String skuId;

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
        private LocalDate billDate;

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
        private String returnRemark;

        /**
         * 退货方式
         */
        private String returnMode;

        /**
         * 退货方式名称
         */
        private String returnModeName;

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

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;
    }

    @Data
    @NoArgsConstructor
    public static class ReturnOrderCountDTO {

        /**
         * 类型(waitSubmit 待提交，approveIng 审核中，reject 审核不通过，approve 已审核)
         */
        private String type;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 获取签收数量
     */
    @Data
    @NoArgsConstructor
    public static class GetReturnQtyDTO {
        /**
         * 采购单id
         */
        private String purchaseOrderId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 退货数量
         */
        private Integer returnQtyDTO;

    }

    @Data
    @NoArgsConstructor
    public static class ViewGeneratePurchaseReturnOrderDTO {

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单单号
         */
        private String purchaseOrderCode;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 供应商名称
         */
        private String supplierId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * skuId
         */
        private String skuId;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 交货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 交货仓库id
         */
        private String deliveryWarehouseId;

        /**
         * 入库数量
         */
        private Integer stockInQty;

        /**
         * 库位名称
         */
        private String warehouseLocationName;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 实退数量
         */
        private Integer returnQty;


        /**
         * 扣款数量
         */
        private Integer deductAmountQty;

        /**
         * 退款单价
         */
        private BigDecimal taxPrice;
    }

    @Data
    @NoArgsConstructor
    public static class UpdatePurchaseOrderAmount {
        /**
         * 采购单详情表id
         */
        private String purchaseOrderDetailId;

        /**
         * 采购金额
         */
        private BigDecimal purchaseAmount; 
    }
}
